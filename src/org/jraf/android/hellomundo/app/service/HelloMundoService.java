/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2009-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.hellomundo.app.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
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
import android.view.View;
import android.widget.RemoteViews;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.analytics.AnalyticsHelper;
import org.jraf.android.hellomundo.app.appwidget.webcam.WebcamAppWidgetActionsActivity;
import org.jraf.android.hellomundo.model.AppwidgetManager;
import org.jraf.android.hellomundo.model.WebcamInfo;
import org.jraf.android.hellomundo.model.WebcamManager;
import org.jraf.android.hellomundo.provider.appwidget.AppwidgetColumns;
import org.jraf.android.hellomundo.provider.appwidget.AppwidgetCursor;
import org.jraf.android.hellomundo.provider.webcam.WebcamColumns;
import org.jraf.android.hellomundo.provider.webcam.WebcamCursor;
import org.jraf.android.hellomundo.provider.webcam.WebcamSelection;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.http.HttpUtil;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

import ca.rmen.sunrisesunset.SunriseSunset;

import com.github.kevinsawicki.http.HttpRequest;

public class HelloMundoService extends IntentService {
    private static final String TAG = HelloMundoService.class.getSimpleName();

    private static final String PREFIX = HelloMundoService.class.getName() + ".";

    public static final String ACTION_UPDATE_WALLPAPER = PREFIX + "ACTION_UPDATE_WALLPAPER";
    public static final String ACTION_UPDATE_WIDGETS = PREFIX + "ACTION_UPDATE_WIDGETS";
    public static final String ACTION_UPDATE_ALL = PREFIX + "ACTION_UPDATE_ALL";

    public static final String ACTION_UPDATE_START = PREFIX + "ACTION_UPDATE_START";
    public static final String ACTION_UPDATE_WALLPAPER_START = PREFIX + "ACTION_UPDATE_WALLPAPER_START";
    public static final String ACTION_UPDATE_WALLPAPER_END_SUCCESS = PREFIX + "ACTION_UPDATE_WALLPAPER_END_SUCCESS";
    public static final String ACTION_UPDATE_WALLPAPER_END_FAILURE = PREFIX + "ACTION_UPDATE_WALLPAPER_END_FAILURE";
    public static final String ACTION_UPDATE_END = PREFIX + "ACTION_UPDATE_END";

    private static final String EXTRA_FROM_ALARM = "EXTRA_FROM_ALARM";

    private static final int THREE_DAYS = 3 * 24 * 60 * 60 * 1000;

    private static enum Mode {
        WALLPAPER, APPWIDGET,
    }

    public HelloMundoService() {
        super("HelloMundoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("intent=" + StringUtil.toString(intent));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        refreshDatabaseFromNetworkIfNeeded(sharedPreferences);

        boolean avoidNight = sharedPreferences.getBoolean(Constants.PREF_AVOID_NIGHT, Constants.PREF_AVOID_NIGHT_DEFAULT);

        // Notify start of update
        sendBroadcast(new Intent(ACTION_UPDATE_START));

        String action = intent.getAction();
        if (ACTION_UPDATE_ALL.equals(action) || ACTION_UPDATE_WALLPAPER.equals(action)) {
            updateWallpaper(intent, sharedPreferences, avoidNight);
        }
        if (ACTION_UPDATE_ALL.equals(action) || ACTION_UPDATE_WIDGETS.equals(action)) {
            updateWidgets(intent, sharedPreferences, avoidNight);
        }

        // If this is a wallpaper only update, notify end of update here.
        // Otherwise the end of update notification is down inside updateWidgets().
        if (ACTION_UPDATE_WALLPAPER.equals(action)) {
            sendBroadcast(new Intent(ACTION_UPDATE_END));
        }
    }


    /*
     * Wallpaper.
     */

    private void updateWallpaper(Intent intent, SharedPreferences sharedPreferences, boolean avoidNight) {
        Log.d();
        long webcamId = sharedPreferences.getLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);

        // Analytics
        AnalyticsHelper.get().sendEvent(TAG, "updateWallpaper", WebcamManager.get().getPublicId(this, webcamId));

        boolean isRandom = false;
        if (webcamId == Constants.WEBCAM_ID_RANDOM) {
            isRandom = true;
            Long randomWebcamId = getRandomWebcamId(avoidNight);
            if (randomWebcamId == null) {
                Log.w("Could not get random webcam id");
                sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
                return;
            }
            webcamId = randomWebcamId;
            Log.d("Random cam: " + webcamId);
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
        boolean wantShowInfo = Constants.PREF_SHOW_INFO_ALWAYS.equals(showInfoPref) || isRandom && Constants.PREF_SHOW_INFO_ONLY_RANDOM.equals(showInfoPref);
        if (wantDimmed || wantShowInfo) {
            ok = saveEditedVersion(wantDimmed, wantShowInfo, webcamInfo);
            if (!ok) return;
        }

        // If enabled, update the wallpaper with the contents of the file
        boolean enabled = sharedPreferences.getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT);
        Log.d("enabled=" + enabled);
        if (enabled) {
            // If this is triggered by an alarm, we first check if the current wallpaper is a live wallpaper.
            // This would mean the current wallpaper has been manually changed by the user, and so we should disable ourself.
            if (intent.getBooleanExtra(HelloMundoService.EXTRA_FROM_ALARM, false)) {
                if (WallpaperManager.getInstance(this).getWallpaperInfo() != null) {
                    Log.d("Current wallpaper is a live wallpaper: disabling service and alarm");
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
                Log.w("Problem while calling WallpaperManager.setStream with webcamId=" + webcamId, e);
                sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
                return;
            } catch (SecurityException e) {
                // This happens, for instance, on a ALCATEL ONE TOUCH 997D
                Log.w("Problem while calling WallpaperManager.setStream with webcamId=" + webcamId, e);
                sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
                return;
            } finally {
                IoUtil.closeSilently(imageInputStream);
            }
        }

        sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_SUCCESS));
    }


    /*
     * Widgets.
     */

    private void updateWidgets(Intent intent, final SharedPreferences sharedPreferences, final boolean avoidNight) {
        Log.d();
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        String[] projection = { AppwidgetColumns.APPWIDGET_ID, AppwidgetColumns.WEBCAM_ID };
        final Cursor c = getContentResolver().query(AppwidgetColumns.CONTENT_URI, projection, null, null, null);
        final AppwidgetCursor cursor = new AppwidgetCursor(c);
        try {
            int count = cursor.getCount();
            Log.d("count=" + count);
            if (count == 0) {
                // Inform listeners that all the widgets have been updated
                sendBroadcast(new Intent(ACTION_UPDATE_END));
                return;
            }
            ExecutorService threadPool = Executors.newFixedThreadPool(count);
            ArrayList<Future<?>> futureList = new ArrayList<Future<?>>(count);
            while (cursor.moveToNext()) {
                final int appwidgetId = cursor.getAppwidgetId();
                final long webcamId = cursor.getWebcamId();
                Log.d("Submitting runnable");
                Future<?> future = threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        long currentWebcamId = webcamId;
                        Log.d("appwidgetId=" + appwidgetId + " currentWebcamId=" + currentWebcamId);

                        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appwidgetId);
                        if (info == null) {
                            // This widget has been deleted: remove it from the db
                            AppwidgetManager.get().delete(HelloMundoService.this, appwidgetId);
                            return;
                        }

                        // Analytics
                        AnalyticsHelper.get().sendEvent(TAG, "updateWidget", WebcamManager.get().getPublicId(HelloMundoService.this, currentWebcamId));

                        if (currentWebcamId == Constants.WEBCAM_ID_RANDOM) {
                            Long randomWebcamId = getRandomWebcamId(avoidNight);
                            if (randomWebcamId == null) {
                                Log.d("Could not get random webcam id");
                                return;
                            }
                            currentWebcamId = randomWebcamId;
                            Log.d("Random cam: " + currentWebcamId);
                        }

                        WebcamInfo webcamInfo = getWebcamInfo(currentWebcamId, sharedPreferences, Mode.APPWIDGET, appwidgetId);
                        if (webcamInfo == null) return;

                        // Download the wallpaper into a file
                        boolean ok = downloadImage(currentWebcamId, webcamInfo, sharedPreferences, Mode.APPWIDGET, appwidgetId);
                        Log.d("ok=" + ok);
                        if (!ok) return;

                        // Save current image to db
                        AppwidgetManager.get().insertOrUpdate(HelloMundoService.this, appwidgetId, webcamId, currentWebcamId);

                        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.appwidget_webcam);
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        // We don't need transparency
                        opts.inPreferredConfig = Bitmap.Config.RGB_565;
                        // The bitmap must be < 1M because appWidgetManager.updateAppWidget() results in an IPC transaction
                        // (see https://groups.google.com/forum/?fromgroups=#!topic/android-developers/3jSq5cEWbEA)
                        opts.inSampleSize = 2;
                        // This makes the bitmap "bigger" but still uses the same amount of memory
                        opts.inTargetDensity = 1;
                        Bitmap bitmap = BitmapFactory.decodeFile(getFileStreamPath(AppwidgetManager.get().getFileName(appwidgetId)).getPath(), opts);
                        logBitmapSize(bitmap);
                        remoteViews.setImageViewBitmap(R.id.imgPreview, bitmap);
                        remoteViews.setViewVisibility(R.id.pgbLoading, View.GONE);
                        remoteViews.setViewVisibility(R.id.imgPreviewFrame, View.VISIBLE);

                        // onClickListener to change the selected webcam
                        Intent onClickIntent = new Intent(HelloMundoService.this, WebcamAppWidgetActionsActivity.class);
                        //                        onClickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appwidgetId);
                        onClickIntent.setData(Uri.parse("custom:" + System.currentTimeMillis())); // Need a unique data so the system doesn't try to recycle the pending intent
                        onClickIntent.putExtra(WebcamAppWidgetActionsActivity.EXTRA_WEBCAM_ID, webcamId);
                        onClickIntent.putExtra(WebcamAppWidgetActionsActivity.EXTRA_CURRENT_WEBCAM_ID, currentWebcamId);
                        onClickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(HelloMundoService.this, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        remoteViews.setOnClickPendingIntent(R.id.imgPreviewFrame, pendingIntent);

                        appWidgetManager.updateAppWidget(new int[] { appwidgetId }, remoteViews);
                        Log.d("updateAppWidget has been called");
                    }
                });

                futureList.add(future);
            }
            // Wait for all tasks to complete (blocking)
            for (Future<?> future : futureList) {
                try {
                    future.get();
                } catch (Exception e) {
                    Log.w("future.get() threw an exception", e);
                }
            }
            threadPool.shutdown();
            // Inform listeners that all the widgets have been updated
            sendBroadcast(new Intent(ACTION_UPDATE_END));
        } finally {
            cursor.close();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void logBitmapSize(Bitmap bitmap) {
        if (bitmap == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) return;
        Log.d("bitmap.getByteCount()=" + bitmap.getByteCount());
    }

    private WebcamInfo getWebcamInfo(long webcamId, SharedPreferences sharedPreferences, Mode mode, int appWidgetId) {
        String[] projection = { WebcamColumns.URL, WebcamColumns.HTTP_REFERER, WebcamColumns.NAME, WebcamColumns.LOCATION };
        WebcamSelection where = new WebcamSelection().id(webcamId);
        WebcamCursor cursor = where.query(getContentResolver(), projection);
        WebcamInfo res = new WebcamInfo();
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Log.w("Could not find webcam with webcamId=" + webcamId);

                // The currently selected webcam doesn't exist.
                // This could happen after a database refresh from network (a non-working cam has been deleted).
                // Default to the Eiffel Tower.
                if (mode == Mode.WALLPAPER) {
                    sharedPreferences.edit().putLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT).commit();
                    sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
                } else {
                    AppwidgetManager.get().insertOrUpdate(this, appWidgetId, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT,
                            Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
                }
                return null;
            }
            res.url = cursor.getUrl();
            res.httpReferer = cursor.getHttpReferer();
            res.name = cursor.getName();
            res.location = cursor.getLocation();
        } finally {
            if (cursor != null) cursor.close();
        }
        return res;
    }

    private boolean downloadImage(long webcamId, WebcamInfo webcamInfo, SharedPreferences sharedPreferences, Mode mode, int appWidgetId) {
        Log.d("webcamId=" + webcamId);

        InputStream inputStream;
        try {
            HttpRequest httpRequest = HttpUtil.get(webcamInfo.url);
            if (webcamInfo.httpReferer != null) httpRequest.referer(webcamInfo.httpReferer);
            inputStream = httpRequest.stream();
        } catch (IOException e) {
            Log.w("Could not download webcam with webcamId=" + webcamId, e);
            if (mode == Mode.WALLPAPER) sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }

        FileOutputStream outputStream;
        try {
            if (mode == Mode.WALLPAPER) {
                outputStream = openFileOutput(Constants.FILE_IMAGE_WALLPAPER, MODE_PRIVATE);
            } else {
                outputStream = openFileOutput(AppwidgetManager.get().getFileName(appWidgetId), MODE_PRIVATE);
            }
        } catch (FileNotFoundException e) {
            // Should never happen
            Log.e("Could not open a file", e);
            if (mode == Mode.WALLPAPER) sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }
        try {
            IoUtil.copy(inputStream, outputStream);
        } catch (IOException e) {
            Log.w("Could not download webcam with webcamId=" + webcamId, e);
            if (mode == Mode.WALLPAPER) sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        } finally {
            IoUtil.closeSilently(inputStream, outputStream);
        }
        return true;
    }

    private boolean saveEditedVersion(boolean dimmed, boolean showInfo, WebcamInfo webcamInfo) {
        Log.d("dimmed=" + dimmed + " showInfo=" + showInfo);
        Bitmap bitmap;
        FileInputStream input = null;
        try {
            input = openFileInput(Constants.FILE_IMAGE_WALLPAPER);
            bitmap = BitmapFactory.decodeStream(input);
        } catch (FileNotFoundException e) {
            Log.w("Could not read saved image", e);
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        } catch (OutOfMemoryError e) {
            Log.w("Could not decode saved image", e);
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        } finally {
            IoUtil.closeSilently(input);
        }

        if (bitmap == null) {
            Log.w("Could not decode saved image as a bitmap");
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }

        // Make a mutable version of the bitmap
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            // Fix for gif images
            config = Bitmap.Config.ARGB_8888;
        }
        try {
            bitmap = bitmap.copy(config, true);
        } catch (OutOfMemoryError e) {
            Log.w("Could not copy bitmap");
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }


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
            paint.setShadowLayer(2, 0, 0, 0x88000000);
            canvas.drawText(text, bitmap.getWidth() / 2 - textWidth / 2, 38, paint);
        }

        // Now save the bitmap to a file
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(Constants.FILE_IMAGE_WALLPAPER_EDITED, MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            // Should never happen
            Log.e("Could not open a file", e);
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }
        boolean ok = bitmap.compress(CompressFormat.JPEG, 90, outputStream);
        IoUtil.closeSilently(outputStream);
        if (!ok) {
            Log.w("Could not encode dimmed image");
            sendBroadcast(new Intent(ACTION_UPDATE_WALLPAPER_END_FAILURE));
            return false;
        }
        return true;
    }

    private void refreshDatabaseFromNetworkIfNeeded(SharedPreferences sharedPreferences) {
        long lastDatabaseUpdate = sharedPreferences.getLong(Constants.PREF_DATABASE_LAST_DOWNLOAD, 0);
        if (System.currentTimeMillis() - lastDatabaseUpdate > THREE_DAYS) {
            Log.d("Last update was more than 3 days ago: refreshing database from network");
            try {
                WebcamManager.get().refreshDatabaseFromNetwork(this);
            } catch (IOException e) {
                Log.w("Could not refresh database from network.  Will try next time.", e);
            }
        }
    }

    private Long getRandomWebcamId(boolean avoidNight) {
        Log.d("avoidNight=" + avoidNight);
        return getRandomWebcamId(avoidNight, 0);
    }

    private Long getRandomWebcamId(boolean avoidNight, int recursion) {
        Log.d("avoidNight=" + avoidNight + " recursion=" + recursion);
        if (recursion > 20) {
            Log.w("Could not find random webcamId after " + recursion + " trials");
            return null;
        }
        String[] projection = { WebcamColumns._ID, WebcamColumns.COORDINATES, WebcamColumns.PUBLIC_ID, WebcamColumns.VISIBILITY_BEGIN_HOUR,
                WebcamColumns.VISIBILITY_BEGIN_MIN, WebcamColumns.VISIBILITY_END_HOUR, WebcamColumns.VISIBILITY_END_MIN };
        WebcamSelection where = new WebcamSelection().excludeRandom((Boolean) null).or().excludeRandom(false);
        WebcamCursor cursor = where.query(getContentResolver(), projection);
        try {
            if (cursor == null) {
                Log.w("Could not find random webcamId");
                return null;
            }
            int count = cursor.getCount();
            int randomIndex = new Random().nextInt(count);
            cursor.moveToPosition(randomIndex);
            long res = cursor.getId();
            String publicId = cursor.getPublicId();
            Log.d("res=" + res + " publicId=" + publicId);
            if (avoidNight) {
                if (cursor.getVisibilityBeginHour() != null) {
                    boolean isNight = isNight(cursor.getVisibilityBeginHour(), cursor.getVisibilityBeginMin(), cursor.getVisibilityEndHour(),
                            cursor.getVisibilityEndMin());
                    Log.d("isNight=" + isNight);
                    if (isNight) {
                        // Recurse
                        return getRandomWebcamId(avoidNight, recursion + 1);
                    }
                } else {
                    boolean isNight;
                    String coordinatesStr = cursor.getCoordinates();
                    if (coordinatesStr == null) {
                        isNight = false;
                    } else {
                        String[] split = coordinatesStr.split(",");
                        double lat = Double.valueOf(split[0]);
                        double lon = Double.valueOf(split[1]);
                        isNight = SunriseSunset.isNight(lat, lon);
                    }
                    Log.d("isNight=" + isNight);
                    if (isNight) {
                        // Recurse
                        return getRandomWebcamId(avoidNight, recursion + 1);
                    }
                }
            }
            return res;
        } finally {
            if (cursor != null) cursor.close();
        }

    }

    private boolean isNight(int startHour, int startMinute, int endHour, int endMinute) {
        Log.d("startHour=" + startHour + " startMinute=" + startMinute + " endHour=" + endHour + " endMinute=" + endMinute);
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
        Intent intent = new Intent(context, HelloMundoService.class);
        intent.setAction(ACTION_UPDATE_WALLPAPER);
        intent.putExtra(HelloMundoService.EXTRA_FROM_ALARM, true);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getUpdateAllPendingIntent(Context context) {
        Intent intent = new Intent(context, HelloMundoService.class);
        intent.setAction(ACTION_UPDATE_ALL);
        intent.putExtra(HelloMundoService.EXTRA_FROM_ALARM, false);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getWidgetsAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, HelloMundoService.class);
        intent.setAction(ACTION_UPDATE_WIDGETS);
        intent.putExtra(HelloMundoService.EXTRA_FROM_ALARM, true);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void updateWallpaperNow(Context context) {
        Intent intent = new Intent(context, HelloMundoService.class);
        intent.setAction(ACTION_UPDATE_WALLPAPER);
        context.startService(intent);
    }

    public static void updateWidgetsNow(Context context) {
        Intent intent = new Intent(context, HelloMundoService.class);
        intent.setAction(ACTION_UPDATE_WIDGETS);
        context.startService(intent);
    }

    public static void updateAllNow(Context context) {
        Intent intent = new Intent(context, HelloMundoService.class);
        intent.setAction(ACTION_UPDATE_ALL);
        context.startService(intent);
    }

}
