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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.jraf.android.util.Blocking;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.provider.AppwidgetColumns;

public class AppwidgetManager {
    private static String TAG = Constants.TAG + AppwidgetManager.class.getSimpleName();

    private static final AppwidgetManager INSTANCE = new AppwidgetManager();

    public static AppwidgetManager get() {
        return INSTANCE;
    }

    private AppwidgetManager() {}

    @Blocking
    public void insertOrUpdate(Context context, int appWidgetId, long webcamId) {
        if (Config.LOGD) Log.d(TAG, "insertOrUpdate appWidgetId=" + appWidgetId + " webcamId=" + webcamId);
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
        if (appWidgetLineId == -1) {
            contentResolver.insert(AppwidgetColumns.CONTENT_URI, values);
        } else {
            contentResolver.update(ContentUris.withAppendedId(AppwidgetColumns.CONTENT_URI, appWidgetLineId), values, null, null);
        }
    }
}
