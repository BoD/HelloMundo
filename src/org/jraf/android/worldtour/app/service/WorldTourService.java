package org.jraf.android.worldtour.app.service;

import java.io.IOException;
import java.io.InputStream;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

    public WorldTourService() {
        super("WorldTourService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Config.LOGD) Log.d(TAG, "onHandleIntent intent=" + intent);

        // Check if the service is enabled, if not, we stop right now and cancel the alarm.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = sharedPreferences.getBoolean(Constants.PREF_SERVICE_ENABLED, Constants.PREF_SERVICE_ENABLED_DEFAULT);
        if (Config.LOGD) Log.d(TAG, "onHandleIntent enabled=" + enabled);
        if (!enabled) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(getServicePendingIntent(this));
            return;
        }

        String webcamId = sharedPreferences.getString(Constants.PREF_WEBCAM_PUBLIC_ID, Constants.PREF_WEBCAM_PUBLIC_ID_DEFAULT);
        if (Constants.PREF_WEBCAM_PUBLIC_ID_RANDOM.equals(webcamId)) {
            // TODO 
        }

        String[] projection = { WebcamColumns.URL, WebcamColumns.HTTP_REFERER };
        String selection = WebcamColumns.PUBLIC_ID + "=?";
        String[] selectionArgs = { webcamId };
        Cursor cursor = getContentResolver().query(WebcamColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        String url = null;
        String httpReferer = null;
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Log.w(TAG, "onHandleIntent Could not find webcam with webcamId=" + webcamId);
                // TODO: do something!
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
            return;
        }
        try {
            WallpaperManager.getInstance(this).setStream(inputStream);
        } catch (IOException e) {
            Log.w(TAG, "onHandleIntent Problem while calling WallpaperManager.setStream with webcamId=" + webcamId, e);
        } finally {
            IoUtil.close(inputStream);
        }
    }

    public static PendingIntent getServicePendingIntent(Context context) {
        return PendingIntent.getService(context, 0, new Intent(context, WorldTourService.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
