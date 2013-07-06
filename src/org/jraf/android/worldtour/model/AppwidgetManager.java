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
package org.jraf.android.worldtour.model;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.collection.CollectionUtil;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.service.WorldTourService;
import org.jraf.android.worldtour.provider.AppwidgetColumns;

public class AppwidgetManager {
    private static String TAG = Constants.TAG + AppwidgetManager.class.getSimpleName();

    private static final AppwidgetManager INSTANCE = new AppwidgetManager();

    public static AppwidgetManager get() {
        return INSTANCE;
    }

    private AppwidgetManager() {}

    @Background
    public void insertOrUpdate(Context context, int appWidgetId, long webcamId, long currentWebcamId) {
        if (Config.LOGD) Log.d(TAG, "insertOrUpdate appWidgetId=" + appWidgetId + " webcamId=" + webcamId + " currentWebcamId=" + currentWebcamId);
        String[] projection = { AppwidgetColumns._ID };
        String selection = AppwidgetColumns.APPWIDGET_ID + "=?";
        String[] selectionArgs = { "" + appWidgetId };
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(AppwidgetColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        long appWidgetLineId = -1;
        try {
            if (cursor.moveToFirst()) {
                appWidgetLineId = cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        ContentValues values = new ContentValues(2);
        values.put(AppwidgetColumns.APPWIDGET_ID, appWidgetId);
        values.put(AppwidgetColumns.WEBCAM_ID, webcamId);
        values.put(AppwidgetColumns.CURRENT_WEBCAM_ID, currentWebcamId);
        if (appWidgetLineId == -1) {
            contentResolver.insert(AppwidgetColumns.CONTENT_URI, values);
        } else {
            contentResolver.update(ContentUris.withAppendedId(AppwidgetColumns.CONTENT_URI, appWidgetLineId), values, null, null);
        }

        // Schedule alarm after insertion
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_UPDATE_INTERVAL,
                Constants.PREF_UPDATE_INTERVAL_DEFAULT));
        PendingIntent widgetsPendingIntent = WorldTourService.getWidgetsAlarmPendingIntent(context);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, widgetsPendingIntent);
    }

    @Background
    public void delete(Context context, int... appWidgetIds) {
        List<Integer> idList = CollectionUtil.asList(appWidgetIds);
        if (Config.LOGD) Log.d(TAG, "delete appWidgetIds=" + idList);
        String selection = AppwidgetColumns.APPWIDGET_ID + " in (" + TextUtils.join(",", idList) + ")";
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(AppwidgetColumns.CONTENT_URI, selection, null);

        // Disable alarm if there are no more widgets
        int count = getWidgetCount(context);
        if (count == 0) {
            if (Config.LOGD) Log.d(TAG, "delete Count=0, canceling alarm");
            PendingIntent widgetsPendingIntent = WorldTourService.getWidgetsAlarmPendingIntent(context);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(widgetsPendingIntent);
        }
    }

    public int getWidgetCount(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(AppwidgetColumns.CONTENT_URI, null, null, null, null);
        try {
            if (cursor == null) return 0;
            int res = cursor.getCount();
            if (Config.LOGD) Log.d(TAG, "getWidgetCount res=" + res);
            return res;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String getFileName(int appwidgetId) {
        return Constants.FILE_IMAGE_APPWIDGET + "_" + appwidgetId;
    }

    public long getCurrentWebcamId(Context context, int appwidgetId) {
        String[] projection = { AppwidgetColumns.CURRENT_WEBCAM_ID };
        String selection = AppwidgetColumns.APPWIDGET_ID + "=?";
        String[] selectionArgs = { "" + appwidgetId };
        final Cursor cursor = context.getContentResolver().query(AppwidgetColumns.CONTENT_URI, projection, selection, selectionArgs, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
            return Constants.WEBCAM_ID_NONE;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
