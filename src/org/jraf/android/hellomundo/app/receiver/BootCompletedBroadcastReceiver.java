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
package org.jraf.android.hellomundo.app.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.service.HelloMundoService;
import org.jraf.android.hellomundo.model.AppwidgetManager;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("intent=" + StringUtil.toString(intent));
        boolean wallpaperEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER,
                Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT);
        Log.d("wallpaperEnabled=" + wallpaperEnabled);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_UPDATE_INTERVAL,
                Constants.PREF_UPDATE_INTERVAL_DEFAULT));

        if (wallpaperEnabled) {
            PendingIntent wallpaperPendingIntent = HelloMundoService.getWallpaperAlarmPendingIntent(context);
            // Set the alarm to trigger in 1 minute (allows for the network to be up)
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 60 * 1000, interval, wallpaperPendingIntent);
        }

        int widgetCount = AppwidgetManager.get().getWidgetCount(context);
        if (widgetCount > 0) {
            PendingIntent widgetsPendingIntent = HelloMundoService.getWidgetsAlarmPendingIntent(context);
            // Set the alarm to trigger in 1 minute (allows for the network to be up)
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 60 * 1000, interval, widgetsPendingIntent);
        }
    }
}
