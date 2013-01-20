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
package org.jraf.android.worldtour.app.common;

import org.jraf.android.util.app.ActivityLifeCycle;
import org.jraf.android.worldtour.analytics.AnalyticsActivityLifeCycle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;


public class AnalyticsSherlockPreferenceActivity extends SherlockPreferenceActivity {
    @Override
    protected void onStart() {
        super.onStart();
        getActivityLifeCycle().onStart(this);
    }

    @Override
    protected void onStop() {
        getActivityLifeCycle().onStop(this);
        super.onStop();
    }

    public ActivityLifeCycle getActivityLifeCycle() {
        return AnalyticsActivityLifeCycle.get();
    }
}
