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
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.HttpUtil;
import org.jraf.android.util.HttpUtil.Options;
import org.jraf.android.util.IoUtil;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.appwidget.WebcamConfigureActivity;
import org.jraf.android.worldtour.model.AppwidgetManager;
import org.jraf.android.worldtour.model.WebcamManager;
import org.jraf.android.worldtour.provider.AppwidgetColumns;
import org.jraf.android.worldtour.provider.WebcamColumns;

import ca.rmen.sunrisesunset.SunriseSunset;

public class WorldTourService extends IntentService {
    private static final String TAG = Constants.TAG + WorldTourService.class.getSimpleName();

    private static final String PREFIX = WorldTourService.class.getName() + ".";

    public static final String ACTION_UPDATE_WALLPAPER = PREFIX + "ACTION_UPDATE_WALLPAPER";
    public static final String ACTION_UPDATE_WIDGETS = PREFIX + "ACTION_UPDATE_WIDGETS";
    public static final String ACTION_UPDATE_ALL = PREFIX + "ACTION_UPDATE_ALL";

    public static final String ACTION_UPDATE_WALLPAPER_START = PREFIX + "ACTION_UPDATE_WALLPAPER_START";
    public static final String ACTION_UPDATE_WALLPAPER_END_SUCCESS = PREFIX + "ACTION_UPDATE_WALLPAPER_END_SUCCESS";
    public static final String ACTION_UPDATE_WALLPAPER_END_FAILURE = PREFIX + "ACTION_UPDATE_WALLPAPER_END_FAILURE";

    private static final String EXTRA_FROM_ALARM = "EXTRA_FROM_ALARM";

    private static final int THREE_DAYS = 3 * 24 * 60 * 60 * 1000;

    private static enum Mode {
        WALLPAPER, APPWIDGET,
    }

    public WorldTourService() {
        super("WorldTourService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Config.LOGD) Log.d(TAG, "onHandleIntent intent=" + intent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        refreshDatabaseFromNetworkIfNeeded(sharedPreferences);

        boolean avoidNight = sharedPreferences.getBoolean(Constants.PREF_AVOID_NIGHT, Constants.PREF_AVOID_NIGHT_DEFAULT);

        String action = intent.getAction();
        if (ACTION_UPDATE_ALL.equals(action) || ACTION_UPDATE_WALLPAPER.equals(action)) {
            updateWallpaper(intent, sharedPreferences, avoidNight);
        }
        if (ACTION_UPDATE_ALL.equals(action) || ACTION_UPDATE_WIDGETS.equals(action)) {
            updateWidgets(intent, sharedPreferences, avoidNight);
        }
    }


    /*
     * Wallpaper.
     */

    private void updateWallpaper(Intent intent, SharedPreferences sharedPreferences, boolean avoidNight) {
        if (Config.LOGD) Log.d(TAG, "updateWallpaper");
        long webcamId = sharedPreferences.getLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
        boolean isRandom = false;
        if (webcamId == Constants.WEBCAM_ID_RANDOM) {
            isRandom = true;
            Long randomWebcamId = getRandomWebcamId(avoidNight);
            if (randomWebcamId == null) {
                Log.w(TAG, "updateWallpaper Could not get random webcam id");
                sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
                return;
            }
            webcamId = randomWebcamId;
            if (Config.LOGD) Log.d(TAG, "updateWallpaper Random cam: " + webcamId);
        }

        sharedPreferences.edit().putLong(Constants.PREF_CURRENT_WEBCAM_ID, webcamId).commit();

        sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_START));

        WebcamInfo webcamInfo = getWebcamInfo(webcamId, sharedPreferences, Mode.WALLPAPER, -1);
        if (webcamInfo == null) return;

        // Download the wallpaper into a file
        boolean ok = downloadImage(webcamId, webcamInfo, sharedPreferences, Mode.WALLPAPER, -1);
        if (!ok) return;

        // If the dimmed setting or the 'show webcam info' setting is enabled create an edited version of the image
        boolean wantDimmed = sharedPreferences.getBoolean(Constants.PREF_DIMMED, Constants.PREF_DIMMED_DEFAULT);
        String showInfoPref = sharedPreferences.getString(Constants.PREF_SHOW_INFO, Constants.PREF_SHOW_INFO_DEFAULT);
        boolean wantShowInfo = Constants.PREF_SHOW_INFO_ALWAYS.equals(showInfoPref) || (isRandom && Constants.PREF_SHOW_INFO_ONLY_RANDOM.equals(showInfoPref));
        if (wantDimmed || wantShowInfo) {
            ok = saveEditedVersion(wantDimmed, wantShowInfo, webcamInfo);
            if (!ok) return;
        }

        // If enabled, update the wallpaper with the contents of the file
        boolean enabled = sharedPreferences.getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT);
        if (Config.LOGD) Log.d(TAG, "updateWallpaper enabled=" + enabled);
        if (enabled) {
            // If this is triggered by an alarm, we first check if the current wallpaper is a live wallpaper.
            // This would mean the current wallpaper has been manually changed by the user, and so we should disable ourself.
            if (intent.getBooleanExtra(WorldTourService.EXTRA_FROM_ALARM, false)) {
                if (WallpaperManager.getInstance(this).getWallpaperInfo() != null) {
                    if (Config.LOGD) Log.d(TAG, "updateWallpaper Current wallpaper is a live wallpaper: disabling service and alarm");
                    // Disable setting
                    sharedPreferences.edit().putBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, false).commit();

                    // Disable alarm
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    PendingIntent pendingIntent = getWallpaperAlarmPendingIntent(this);
                    alarmManager.cancel(pendingIntent);
                    sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
                    return;
                }
            }

            // Update flag
            sharedPreferences.edit().putBoolean(Constants.PREF_WALLPAPER_CHANGED_INTERNAL, true).commit();

            // Set wallpaper
            FileInputStream imageInputStream = null;
            try {
                imageInputStream = openFileInput(wantDimmed || wantShowInfo ? Constants.FILE_IMAGE_WALLPAPER_EDITED : Constants.FILE_IMAGE_WALLPAPER);
                WallpaperManager.getInstance(this).setStream(imageInputStream);
            } catch (IOException e) {
                Log.w(TAG, "updateWallpaper Problem while calling WallpaperManager.setStream with webcamId=" + webcamId, e);
                sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
                return;
            } finally {
                IoUtil.close(imageInputStream);
            }
        }

        sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_SUCCESS));
    }


    /*
     * Widgets.
     */

    private void updateWidgets(Intent intent, final SharedPreferences sharedPreferences, final boolean avoidNight) {
        if (Config.LOGD) Log.d(TAG, "updateWidgets");
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        String[] projection = { AppwidgetColumns.APPWIDGET_ID, AppwidgetColumns.WEBCAM_ID };
        final Cursor cursor = getContentResolver().query(AppwidgetColumns.CONTENT_URI, projection, null, null, null);
        try {
            int count = cursor.getCount();
            if (Config.LOGD) Log.d(TAG, "updateWidgets count=" + count);
            if (count == 0) return;
            ExecutorService threadPool = Executors.newFixedThreadPool(count);
            while (cursor.moveToNext()) {
                final int appwidgetId = cursor.getInt(0);
                final long finalWebcamId = cursor.getLong(1);
                if (Config.LOGD) Log.d(TAG, "updateWidgets Submitting runnable");
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        long webcamId = finalWebcamId;
                        if (Config.LOGD) Log.d(TAG, "updateWidgets appwidgetId=" + appwidgetId + " webcamId=" + webcamId);

                        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appwidgetId);
                        if (info == null) {
                            // This widget has been deleted: remove it from the db
                            AppwidgetManager.get().delete(WorldTourService.this, appwidgetId);
                            return;
                        }

                        if (webcamId == Constants.WEBCAM_ID_RANDOM) {
                            Long randomWebcamId = getRandomWebcamId(avoidNight);
                            if (randomWebcamId == null) {
                                Log.w(TAG, "updateWidgets Could not get random webcam id");
                                return;
                            }
                            webcamId = randomWebcamId;
                            if (Config.LOGD) Log.d(TAG, "updateWidgets Random cam: " + webcamId);
                        }

                        WebcamInfo webcamInfo = getWebcamInfo(webcamId, sharedPreferences, Mode.APPWIDGET, appwidgetId);
                        if (webcamInfo == null) return;

                        // Download the wallpaper into a file
                        boolean ok = downloadImage(webcamId, webcamInfo, sharedPreferences, Mode.APPWIDGET, appwidgetId);
                        if (Config.LOGD) Log.d(TAG, "updateWidgets ok=" + ok);
                        if (!ok) return;

                        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.appwidget_webcam);
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        // We don't need transparency
                        opts.inPreferredConfig = Bitmap.Config.RGB_565;
                        // The bitmap must be < 1M because appWidgetManager.updateAppWidget() results in an IPC transaction
                        // (see https://groups.google.com/forum/?fromgroups=#!topic/android-developers/3jSq5cEWbEA)
                        opts.inSampleSize = 2;
                        Bitmap bitmap = BitmapFactory.decodeFile(getFileStreamPath(Constants.FILE_IMAGE_APPWIDGET + "_" + appwidgetId).getPath(), opts);
                        logBitmapSize(bitmap);
                        remoteViews.setImageViewBitmap(R.id.imgPreview, bitmap);
                        remoteViews.setViewVisibility(R.id.pgbLoading, View.GONE);
                        remoteViews.setViewVisibility(R.id.imgPreviewFrame, View.VISIBLE);

                        // onClickListener to change the selected webcam
                        Intent onClickIntent = new Intent(WorldTourService.this, WebcamConfigureActivity.class);
                        onClickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appwidgetId);
                        onClickIntent.setData(Uri.parse("custom:" + System.currentTimeMillis())); // Need a unique data so the system doesn't try to recycle the pending intent
                        onClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(WorldTourService.this, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        remoteViews.setOnClickPendingIntent(R.id.imgPreviewFrame, pendingIntent);

                        appWidgetManager.updateAppWidget(new int[] { appwidgetId }, remoteViews);
                        if (Config.LOGD) Log.d(TAG, "updateWidgets updateAppWidget has been called");
                    }
                });
            }
            threadPool.shutdown();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void logBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) return;
        if (Config.LOGD) Log.d(TAG, "updateWidgets bitmap.getByteCount()=" + bitmap.getByteCount());
    }

    private static class WebcamInfo {
        public String url;
        public String httpReferer;
        public String name;
        public String location;
    }

    private WebcamInfo getWebcamInfo(long webcamId, SharedPreferences sharedPreferences, Mode mode, int appWidgetId) {
        String[] projection = { WebcamColumns.URL, WebcamColumns.HTTP_REFERER, WebcamColumns.NAME, WebcamColumns.LOCATION };
        Uri webcamUri = ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, webcamId);
        Cursor cursor = getContentResolver().query(webcamUri, projection, null, null, null);
        WebcamInfo res = new WebcamInfo();
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Log.w(TAG, "getDownloadInfo Could not find webcam with webcamId=" + webcamId);

                // The currently selected webcam doesn't exist.
                // This could happen after a database refresh from network (a non-working cam has been deleted).
                // Default to the Eiffel Tower.
                if (mode == Mode.WALLPAPER) {
                    sharedPreferences.edit().putLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT).commit();
                    sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
                } else {
                    AppwidgetManager.get().insertOrUpdate(this, appWidgetId, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
                }
                return null;
            }
            res.url = cursor.getString(0);
            res.httpReferer = cursor.getString(1);
            res.name = cursor.getString(2);
            res.location = cursor.getString(3);
        } finally {
            if (cursor != null) cursor.close();
        }
        return res;
    }

    private boolean downloadImage(long webcamId, WebcamInfo webcamInfo, SharedPreferences sharedPreferences, Mode mode, int appWidgetId) {
        if (Config.LOGD) Log.d(TAG, "downloadImage webcamId=" + webcamId);

        Options options = new Options();
        options.referer = webcamInfo.httpReferer;
        InputStream inputStream;
        try {
            inputStream = HttpUtil.getAsStream(webcamInfo.url);
        } catch (IOException e) {
            Log.w(TAG, "downloadImage Could not download webcam with webcamId=" + webcamId, e);
            if (mode == Mode.WALLPAPER) sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }

        FileOutputStream outputStream;
        try {
            if (mode == Mode.WALLPAPER) {
                outputStream = openFileOutput(Constants.FILE_IMAGE_WALLPAPER, MODE_PRIVATE);
            } else {
                outputStream = openFileOutput(Constants.FILE_IMAGE_APPWIDGET + "_" + appWidgetId, MODE_PRIVATE);
            }
        } catch (FileNotFoundException e) {
            // Should never happen
            Log.e(TAG, "downloadImage Could not open a file", e);
            if (mode == Mode.WALLPAPER) sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }
        try {
            IoUtil.copy(inputStream, outputStream);
        } catch (IOException e) {
            Log.w(TAG, "downloadImage Could not download webcam with webcamId=" + webcamId, e);
            if (mode == Mode.WALLPAPER) sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        } finally {
            IoUtil.close(inputStream, outputStream);
        }
        return true;
    }

    private boolean saveEditedVersion(boolean dimmed, boolean showInfo, WebcamInfo webcamInfo) {
        if (Config.LOGD) Log.d(TAG, "saveEditedVersion dimmed=" + dimmed + " showInfo=" + showInfo);
        Bitmap bitmap;
        FileInputStream input = null;
        try {
            input = openFileInput(Constants.FILE_IMAGE_WALLPAPER);
            bitmap = BitmapFactory.decodeStream(input);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "saveEditedVersion Could not read saved image", e);
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        } finally {
            IoUtil.close(input);
        }

        if (bitmap == null) {
            Log.w(TAG, "saveEditedVersion Could not decode saved image as a bitmap");
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }

        // Make a mutable version of the bitmap
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            // Fix for gif images
            config = Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(config, true);

        Canvas canvas = new Canvas(bitmap);
        if (dimmed) {
            // Draw a 70% gray color layer on top of the bitmap
            canvas.drawColor(0x4D000000);
        }

        if (showInfo) {
            Paint paint = new Paint();
            String text = webcamInfo.name + ", " + webcamInfo.location;
            float textWidth = paint.measureText(text);
            paint.setColor(getResources().getColor(R.color.wallpaper_showInfo_text));
            paint.setShadowLayer(1, 0, 0, 0xff000000);
            canvas.drawText(text, bitmap.getWidth() / 2 - textWidth / 2, 38, paint);
        }

        // Now save the bitmap to a file
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(Constants.FILE_IMAGE_WALLPAPER_EDITED, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            // Should never happen
            Log.e(TAG, "saveEditedVersion Could not open a file", e);
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }
        boolean ok = bitmap.compress(CompressFormat.JPEG, 90, outputStream);
        IoUtil.close(outputStream);
        if (!ok) {
            Log.w(TAG, "saveEditedVersion Could not encode dimmed image");
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
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

    private Long getRandomWebcamId(boolean avoidNight) {
        if (Config.LOGD) Log.d(TAG, "getRandomWebcamId avoidNight=" + avoidNight);
        String[] projection = { WebcamColumns._ID, WebcamColumns.COORDINATES, WebcamColumns.PUBLIC_ID, WebcamColumns.VISIBILITY_BEGIN_HOUR,
                WebcamColumns.VISIBILITY_BEGIN_MIN, WebcamColumns.VISIBILITY_END_HOUR, WebcamColumns.VISIBILITY_END_MIN };
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
            long res = cursor.getLong(0);
            String publicId = cursor.getString(2);
            if (Config.LOGD) Log.d(TAG, "getRandomWebcamId res=" + res + " publicId=" + publicId);
            if (avoidNight) {
                if (!cursor.isNull(3)) {
                    boolean isNight = isNight(cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6));
                    if (Config.LOGD) Log.d(TAG, "getRandomWebcamId isNight=" + isNight);
                    if (isNight) {
                        // Recurse
                        return getRandomWebcamId(avoidNight);
                    }
                } else {
                    String coordinates = cursor.getString(1);
                    boolean isNight = SunriseSunset.isNight(coordinates);
                    if (Config.LOGD) Log.d(TAG, "getRandomWebcamId isNight=" + isNight);
                    if (isNight) {
                        // Recurse
                        return getRandomWebcamId(avoidNight);
                    }
                }
            }
            return res;
        } finally {
            if (cursor != null) cursor.close();
        }

    }

    private boolean isNight(int startHour, int startMinute, int endHour, int endMinute) {
        if (Config.LOGD) Log.d(TAG, "isNight startHour=" + startHour + " startMinute=" + startMinute + " endHour=" + endHour + " endMinute=" + endMinute);
        Calendar now = Calendar.getInstance();
        TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        Calendar start = Calendar.getInstance(gmtTimeZone);
        start.set(Calendar.HOUR_OF_DAY, startHour);
        start.set(Calendar.MINUTE, startMinute);
        Calendar end = Calendar.getInstance(gmtTimeZone);
        end.set(Calendar.HOUR_OF_DAY, endHour);
        end.set(Calendar.MINUTE, endMinute);
        boolean isLight = start.before(now) && now.before(end);
        return !isLight;
    }


    /*
     * Convenience methods.
     */

    public static PendingIntent getWallpaperAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, WorldTourService.class);
        intent.setAction(ACTION_UPDATE_WALLPAPER);
        intent.putExtra(WorldTourService.EXTRA_FROM_ALARM, true);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getWidgetsAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, WorldTourService.class);
        intent.setAction(ACTION_UPDATE_WIDGETS);
        intent.putExtra(WorldTourService.EXTRA_FROM_ALARM, true);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void refreshWallpaperNow(Context context) {
        Intent intent = new Intent(context, WorldTourService.class);
        intent.setAction(ACTION_UPDATE_WALLPAPER);
        context.startService(intent);
    }

    public static void refreshWidgetsNow(Context context) {
        Intent intent = new Intent(context, WorldTourService.class);
        intent.setAction(ACTION_UPDATE_WIDGETS);
        context.startService(intent);
    }


    public static void refreshAllNow(Context context) {
        Intent intent = new Intent(context, WorldTourService.class);
        intent.setAction(ACTION_UPDATE_ALL);
        context.startService(intent);
    }

}
