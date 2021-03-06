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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.service.HelloMundoService;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class WallpaperChangedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("intent=" + StringUtil.toString(intent));
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wallpaperChangedInternal = sharedPreferences.getBoolean(Constants.PREF_WALLPAPER_CHANGED_INTERNAL,
                Constants.PREF_WALLPAPER_CHANGED_INTERNAL_DEFAULT);
        if (wallpaperChangedInternal) {
            Log.d("Wallpaper changed internally");
            // Update flag (in a background thread to avoid ANRs)
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    sharedPreferences.edit().putBoolean(Constants.PREF_WALLPAPER_CHANGED_INTERNAL, false).commit();
                    return null;
                }
            }.execute();
        } else {
            Log.d("Wallpaper changed by external app: disabling service and alarm");
            // Disable setting (in a background thread to avoid ANRs)
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    sharedPreferences.edit().putBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, false).commit();
                    return null;
                }
            }.execute();

            // Disable alarm
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = HelloMundoService.getWallpaperAlarmPendingIntent(context);
            alarmManager.cancel(pendingIntent);
        }
    }
}
