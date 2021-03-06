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
package org.jraf.android.hellomundo.model;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.service.HelloMundoService;
import org.jraf.android.hellomundo.provider.appwidget.AppwidgetColumns;
import org.jraf.android.hellomundo.provider.appwidget.AppwidgetContentValues;
import org.jraf.android.hellomundo.provider.appwidget.AppwidgetCursor;
import org.jraf.android.hellomundo.provider.appwidget.AppwidgetSelection;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.collection.CollectionUtil;
import org.jraf.android.util.log.wrapper.Log;

public class AppwidgetManager {
    private static final AppwidgetManager INSTANCE = new AppwidgetManager();

    public static AppwidgetManager get() {
        return INSTANCE;
    }

    private AppwidgetManager() {}

    @Background
    public void insertOrUpdate(Context context, int appWidgetId, long webcamId, long currentWebcamId) {
        Log.d("appWidgetId=" + appWidgetId + " webcamId=" + webcamId + " currentWebcamId=" + currentWebcamId);
        String[] projection = { AppwidgetColumns._ID };
        AppwidgetSelection where = new AppwidgetSelection().appwidgetId(appWidgetId);
        ContentResolver contentResolver = context.getContentResolver();
        AppwidgetCursor cursor = where.query(contentResolver, projection);
        long appWidgetLineId = -1;
        try {
            if (cursor.moveToFirst()) {
                appWidgetLineId = cursor.getId();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        AppwidgetContentValues values = new AppwidgetContentValues();
        values.putAppwidgetId(appWidgetId);
        values.putWebcamId(webcamId);
        values.putCurrentWebcamId(currentWebcamId);
        if (appWidgetLineId == -1) {
            values.insert(contentResolver);
        } else {
            AppwidgetSelection whereId = new AppwidgetSelection().id(appWidgetLineId);
            values.update(contentResolver, whereId);
        }

        // Schedule alarm after insertion
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_UPDATE_INTERVAL,
                Constants.PREF_UPDATE_INTERVAL_DEFAULT));
        PendingIntent widgetsPendingIntent = HelloMundoService.getWidgetsAlarmPendingIntent(context);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, widgetsPendingIntent);
    }

    @Background
    public void delete(Context context, int... appWidgetIds) {
        List<Integer> idList = CollectionUtil.asList(appWidgetIds);
        Log.d("appWidgetIds=" + idList);
        AppwidgetSelection where = new AppwidgetSelection().appwidgetId(appWidgetIds);
        ContentResolver contentResolver = context.getContentResolver();
        where.delete(contentResolver);

        // Disable alarm if there are no more widgets
        int count = getWidgetCount(context);
        if (count == 0) {
            Log.d("Count=0, canceling alarm");
            PendingIntent widgetsPendingIntent = HelloMundoService.getWidgetsAlarmPendingIntent(context);
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
            Log.d("res=" + res);
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
        AppwidgetSelection where = new AppwidgetSelection().appwidgetId(appwidgetId);
        AppwidgetCursor cursor = where.query(context.getContentResolver(), projection);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getCurrentWebcamId();
            }
            return Constants.WEBCAM_ID_NONE;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
