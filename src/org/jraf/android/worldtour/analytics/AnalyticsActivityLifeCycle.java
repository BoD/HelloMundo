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

import org.jraf.android.util.app.ActivityLifeCycle;

public class AnalyticsActivityLifeCycle implements ActivityLifeCycle {
    private static final AnalyticsActivityLifeCycle INSTANCE = new AnalyticsActivityLifeCycle();

    public static AnalyticsActivityLifeCycle get() {
        return INSTANCE;
    }

    private AnalyticsActivityLifeCycle() {}

    @Override
    public void onStart(Activity activity) {
        AnalyticsHelper.get().activityStart(activity);
    }

    @Override
    public void onStop(Activity activity) {
        AnalyticsHelper.get().activityStop(activity);
    }
}
