/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright 2013 Benoit 'BoD' Lubek (BoD@JRAF.org).  All Rights Reserved.
 */
package org.jraf.android.worldtour.analytics;

import android.app.Activity;
import android.util.Log;

import org.jraf.android.worldtour.Constants;

import com.google.analytics.tracking.android.EasyTracker;

public class AnalyticsHelper {
    private static final String TAG = Constants.TAG + AnalyticsHelper.class.getSimpleName();

    private static final AnalyticsHelper INSTANCE = new AnalyticsHelper();

    public static AnalyticsHelper get() {
        return INSTANCE;
    }

    private AnalyticsHelper() {}

    public void activityStart(Activity activity) {
        try {
            EasyTracker.getInstance().activityStart(activity);
        } catch (Exception e) {
            Log.w(TAG, "An exception occured while calling Google Analytics", e);
        }
    }

    public void activityStop(Activity activity) {
        try {
            EasyTracker.getInstance().activityStop(activity);
        } catch (Exception e) {
            Log.w(TAG, "An exception occured while calling Google Analytics", e);
        }
    }

    public void sendEvent(String category, String action, String label) {
        try {
            EasyTracker.getTracker().sendEvent(category, action, label, null);
        } catch (Exception e) {
            Log.w(TAG, "An exception occured while calling Google Analytics", e);
        }
    }
}
