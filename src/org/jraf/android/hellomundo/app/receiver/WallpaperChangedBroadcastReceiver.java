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
package org.jraf.android.hellomundo.app.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jraf.android.hellomundo.Config;
import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.service.HelloMundoService;

public class WallpaperChangedBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = Constants.TAG + WallpaperChangedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Config.LOGD) Log.d(TAG, "onReceive context=" + context + " intent=" + intent);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wallpaperChangedInternal = sharedPreferences.getBoolean(Constants.PREF_WALLPAPER_CHANGED_INTERNAL,
                Constants.PREF_WALLPAPER_CHANGED_INTERNAL_DEFAULT);
        if (wallpaperChangedInternal) {
            if (Config.LOGD) Log.d(TAG, "onReceive Wallpaper changed internally");
            // Update flag
            sharedPreferences.edit().putBoolean(Constants.PREF_WALLPAPER_CHANGED_INTERNAL, false).commit();
        } else {
            if (Config.LOGD) Log.d(TAG, "onReceive Wallpaper changed by external app: disabling service and alarm");
            // Disable setting
            sharedPreferences.edit().putBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, false).commit();

            // Disable alarm
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = HelloMundoService.getWallpaperAlarmPendingIntent(context);
            alarmManager.cancel(pendingIntent);
        }
    }
}
