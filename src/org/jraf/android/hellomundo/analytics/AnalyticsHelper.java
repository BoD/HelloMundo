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
package org.jraf.android.hellomundo.analytics;

import android.app.Activity;

import org.jraf.android.util.log.wrapper.Log;

import com.google.analytics.tracking.android.EasyTracker;

public class AnalyticsHelper {
    private static final AnalyticsHelper INSTANCE = new AnalyticsHelper();

    public static AnalyticsHelper get() {
        return INSTANCE;
    }

    private AnalyticsHelper() {}

    public void activityStart(Activity activity) {
        try {
            EasyTracker.getInstance().activityStart(activity);
        } catch (Throwable t) {
            Log.w("An exception occured while calling Google Analytics", t);
        }
    }

    public void activityStop(Activity activity) {
        try {
            EasyTracker.getInstance().activityStop(activity);
        } catch (Throwable t) {
            Log.w("An exception occured while calling Google Analytics", t);
        }
    }

    public void sendEvent(String category, String action, String label) {
        try {
            EasyTracker.getTracker().sendEvent(category, action, label, null);
        } catch (Throwable t) {
            Log.w("An exception occured while calling Google Analytics", t);
        }
    }
}
