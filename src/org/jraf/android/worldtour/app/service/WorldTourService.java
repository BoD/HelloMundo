package org.jraf.android.worldtour.app.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jraf.android.util.HttpUtil;
import org.jraf.android.util.HttpUtil.Options;
import org.jraf.android.util.IoUtil;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.provider.WebcamColumns;

public class WorldTourService extends IntentService {
    private static final String TAG = Constants.TAG + WorldTourService.class.getSimpleName();

    private static final String PREFIX = WorldTourService.class.getName() + ".";
    public static final String ACTION_UPDATE_START = PREFIX + "ACTION_UPDATE_START";
    public static final String ACTION_UPDATE_END_SUCCESS = PREFIX + "ACTION_UPDATE_END_SUCCESS";
    public static final String ACTION_UPDATE_END_FAILURE = PREFIX + "ACTION_UPDATE_END_FAILURE";

    private static final String EXTRA_FROM_ALARM = "EXTRA_FROM_ALARM";

    public WorldTourService() {
        super("WorldTourService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Config.LOGD) Log.d(TAG, "onHandleIntent intent=" + intent);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        long webcamId = sharedPreferences.getLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
        if (webcamId == Constants.WEBCAM_ID_RANDOM) {
            Long randomWebcamId = getRandomWebcamId();
            if (randomWebcamId == null) {
                Log.w(TAG, "onHandleIntent Could not get random webcam id");
                sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
                return;
            }
            webcamId = randomWebcamId;
            if (Config.LOGD) Log.d(TAG, "onHandleIntent Random cam: " + webcamId);
        }

        sharedPreferences.edit().putLong(Constants.PREF_CURRENT_WEBCAM_ID, webcamId).commit();

        sendBroadcast(new Intent(ACTION_UPDATE_START));

        String[] projection = { WebcamColumns.URL, WebcamColumns.HTTP_REFERER };
        Uri webcamUri = ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, webcamId);
        Cursor cursor = getContentResolver().query(webcamUri, projection, null, null, null);
        String url = null;
        String httpReferer = null;
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Log.w(TAG, "onHandleIntent Could not find webcam with webcamId=" + webcamId);
                sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
                return;
            }
            url = cursor.getString(0);
            httpReferer = cursor.getString(1);
        } finally {
            if (cursor != null) cursor.close();
        }
        Options options = new Options();
        options.referer = httpReferer;
        InputStream inputStream;
        try {
            inputStream = HttpUtil.getAsStream(url);
        } catch (IOException e) {
            Log.w(TAG, "onHandleIntent Could not download webcam with webcamId=" + webcamId, e);
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return;
        }

        // Download the wallpaper into a file
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(Constants.FILE_IMAGE, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            // Should never happen
            Log.e(TAG, "Could not open a file", e);
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return;
        }
        try {
            IoUtil.copy(inputStream, outputStream);
        } catch (IOException e) {
            Log.w(TAG, "onHandleIntent Could not download webcam with webcamId=" + webcamId, e);
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return;
        } finally {
            IoUtil.close(inputStream, outputStream);
        }

        // If enabled, update the wallpaper with the contents of the file
        boolean enabled = sharedPreferences.getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT);
        if (Config.LOGD) Log.d(TAG, "onHandleIntent enabled=" + enabled);
        if (enabled) {
            // If this is triggered by an alarm, we first check if the current wallpaper is a live wallpaper.
            // This would mean the current wallpaper has been manually changed by the user, and so we should disable ourself.
            if (intent.getBooleanExtra(WorldTourService.EXTRA_FROM_ALARM, false)) {
                if (WallpaperManager.getInstance(this).getWallpaperInfo() != null) {
                    if (Config.LOGD) Log.d(TAG, "onHandleIntent Current wallpaper is a live wallpaper: disabling service and alarm");
                    // Disable setting
                    sharedPreferences.edit().putBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, false).commit();

                    // Disable alarm
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    PendingIntent pendingIntent = getAlarmPendingIntent(this);
                    alarmManager.cancel(pendingIntent);
                    sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
                    return;
                }
            }

            // Update flag
            sharedPreferences.edit().putBoolean(Constants.PREF_WALLPAPER_CHANGED_INTERNAL, true).commit();

            FileInputStream imageInputStream = null;
            try {
                imageInputStream = openFileInput(Constants.FILE_IMAGE);
                WallpaperManager.getInstance(this).setStream(imageInputStream);
            } catch (IOException e) {
                Log.w(TAG, "onHandleIntent Problem while calling WallpaperManager.setStream with webcamId=" + webcamId, e);
                sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
                return;
            } finally {
                IoUtil.close(imageInputStream);
            }
        }

        sendBroadcast(new Intent(ACTION_UPDATE_END_SUCCESS));
    }

    private Long getRandomWebcamId() {
        String[] projection = { WebcamColumns._ID };
        String selection = WebcamColumns.EXCLUDE_RANDOM + " is null or " + WebcamColumns.EXCLUDE_RANDOM + "=0";
        Cursor cursor = getContentResolver().query(WebcamColumns.CONTENT_URI, projection, selection, null, null);
        try {
            if (cursor == null) {
                Log.w(TAG, "getRandomWebcamId Could not find random webcamId");
                return null;
            }
            int count = cursor.getCount();
            int randomIndex = new Random().nextInt(count);
            cursor.moveToPosition(randomIndex);
            return cursor.getLong(0);
        } finally {
            if (cursor != null) cursor.close();
        }

    }

    public static PendingIntent getAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, WorldTourService.class);
        intent.putExtra(WorldTourService.EXTRA_FROM_ALARM, true);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
