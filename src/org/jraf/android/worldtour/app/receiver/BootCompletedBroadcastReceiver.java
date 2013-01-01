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
package org.jraf.android.worldtour.app.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.service.WorldTourService;
import org.jraf.android.worldtour.model.AppwidgetManager;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = Constants.TAG + BootCompletedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Config.LOGD) Log.d(TAG, "onReceive context=" + context + " intent=" + intent);
        boolean wallpaperEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER,
                Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT);
        if (Config.LOGD) Log.d(TAG, "onReceive wallpaperEnabled=" + wallpaperEnabled);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_UPDATE_INTERVAL,
                Constants.PREF_UPDATE_INTERVAL_DEFAULT));

        if (wallpaperEnabled) {
            PendingIntent wallpaperPendingIntent = WorldTourService.getWallpaperAlarmPendingIntent(context);
            // Set the alarm to trigger in 1 minute (allows for the network to be up)
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 60 * 1000, interval, wallpaperPendingIntent);
        }

        int widgetCount = AppwidgetManager.get().getWidgetCount(context);
        if (widgetCount > 0) {
            PendingIntent widgetsPendingIntent = WorldTourService.getWidgetsAlarmPendingIntent(context);
            // Set the alarm to trigger in 1 minute (allows for the network to be up)
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 60 * 1000, interval, widgetsPendingIntent);
        }
    }
}
