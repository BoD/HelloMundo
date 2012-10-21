/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright 2012 Benoit 'BoD' Lubek (BoD@JRAF.org).  All Rights Reserved.
 */
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
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jraf.android.util.HttpUtil;
import org.jraf.android.util.HttpUtil.Options;
import org.jraf.android.util.IoUtil;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.model.WebcamManager;
import org.jraf.android.worldtour.provider.WebcamColumns;

public class WorldTourService extends IntentService {
    private static final String TAG = Constants.TAG + WorldTourService.class.getSimpleName();

    private static final String PREFIX = WorldTourService.class.getName() + ".";
    public static final String ACTION_UPDATE_START = PREFIX + "ACTION_UPDATE_START";
    public static final String ACTION_UPDATE_END_SUCCESS = PREFIX + "ACTION_UPDATE_END_SUCCESS";
    public static final String ACTION_UPDATE_END_FAILURE = PREFIX + "ACTION_UPDATE_END_FAILURE";

    private static final String EXTRA_FROM_ALARM = "EXTRA_FROM_ALARM";

    private static final int THREE_DAYS = 3 * 24 * 60 * 60 * 1000;

    public WorldTourService() {
        super("WorldTourService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Config.LOGD) Log.d(TAG, "onHandleIntent intent=" + intent);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        refreshDatabaseFromNetworkIfNeeded(sharedPreferences);

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

                // The currently selected webcam doesn't exist.
                // This could happen after a database refresh from network (a non-working cam has been deleted).
                // Default to the Eiffel Tower.
                sharedPreferences.edit().putLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT).commit();

                sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
                return;
            }
            url = cursor.getString(0);
            httpReferer = cursor.getString(1);
        } finally {
            if (cursor != null) cursor.close();
        }

        // Download the wallpaper into a file
        boolean ok = downloadWallPaper(url, httpReferer, webcamId);
        if (!ok) return;

        // If the dimmed setting is enabled, create a dimmed version of the image
        boolean wantDimmed = sharedPreferences.getBoolean(Constants.PREF_DIMMED, Constants.PREF_DIMMED_DEFAULT);
        if (wantDimmed) {
            ok = saveDimmedVersion();
            if (!ok) return;
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

            // Set wallpaper
            FileInputStream imageInputStream = null;
            try {
                imageInputStream = openFileInput(wantDimmed ? Constants.FILE_IMAGE_DIMMED : Constants.FILE_IMAGE);
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

    private boolean downloadWallPaper(String url, String httpReferer, long webcamId) {
        if (Config.LOGD) Log.d(TAG, "downloadWallPaper url=" + url + " httpReferer=" + httpReferer + " webcamId=" + webcamId);
        Options options = new Options();
        options.referer = httpReferer;
        InputStream inputStream;
        try {
            inputStream = HttpUtil.getAsStream(url);
        } catch (IOException e) {
            Log.w(TAG, "downloadWallPaper Could not download webcam with webcamId=" + webcamId, e);
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return false;
        }

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(Constants.FILE_IMAGE, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            // Should never happen
            Log.e(TAG, "downloadWallPaper Could not open a file", e);
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return false;
        }
        try {
            IoUtil.copy(inputStream, outputStream);
        } catch (IOException e) {
            Log.w(TAG, "downloadWallPaper Could not download webcam with webcamId=" + webcamId, e);
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return false;
        } finally {
            IoUtil.close(inputStream, outputStream);
        }
        return true;
    }

    private boolean saveDimmedVersion() {
        if (Config.LOGD) Log.d(TAG, "saveDimmedVersion");
        Bitmap bitmap;
        FileInputStream input = null;
        try {
            input = openFileInput(Constants.FILE_IMAGE);
            bitmap = BitmapFactory.decodeStream(input);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "saveDimmedVersion Could not read saved image", e);
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return false;
        } finally {
            IoUtil.close(input);
        }

        if (bitmap == null) {
            Log.w(TAG, "saveDimmedVersion Could not decode saved image as a bitmap");
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return false;
        }

        // Make a mutable version of the bitmap
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            // Fix for gif images
            config = Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(config, true);

        // Draw a 70% gray color layer on top of the bitmap
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0x4D000000);

        // Now save the bitmap to a file
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(Constants.FILE_IMAGE_DIMMED, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            // Should never happen
            Log.e(TAG, "saveDimmedVersion Could not open a file", e);
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return false;
        }
        boolean ok = bitmap.compress(CompressFormat.JPEG, 90, outputStream);
        IoUtil.close(outputStream);
        if (!ok) {
            Log.w(TAG, "saveDimmedVersion Could not encode dimmed image");
            sendBroadcast(new Intent(ACTION_UPDATE_END_FAILURE));
            return false;
        }
        return true;
    }

    private void refreshDatabaseFromNetworkIfNeeded(SharedPreferences sharedPreferences) {
        long lastDatabaseUpdate = sharedPreferences.getLong(Constants.PREF_DATABASE_LAST_DOWNLOAD, 0);
        if (System.currentTimeMillis() - lastDatabaseUpdate > THREE_DAYS) {
            if (Config.LOGD) Log.d(TAG, "refreshDatabaseFromNetworkIfNeeded Last update was more than 3 days ago: refreshing database from network");
            try {
                WebcamManager.get().refreshDatabaseFromNetwork(this);
            } catch (IOException e) {
                Log.w(TAG, "refreshDatabaseFromNetworkIfNeeded Could not refresh database from network.  Will try next time.", e);
            }
        }
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
