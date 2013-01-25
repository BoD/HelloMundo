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
package org.jraf.android.worldtour.app;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import org.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksAdapter;
import org.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksCompat;
import org.jraf.android.util.activitylifecyclecallbackscompat.ApplicationHelper;
import org.jraf.android.worldtour.analytics.AnalyticsHelper;

import com.google.analytics.tracking.android.EasyTracker;

public class Application extends android.app.Application {
    public static int sVersionCode;
    public static String sVersionName;

    @Override
    public void onCreate() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            sVersionCode = packageInfo.versionCode;
            sVersionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            // Should never happen
            throw new AssertionError(e);
        }

        // Google Analytics
        EasyTracker.getInstance().setContext(this);
        ApplicationHelper.registerActivityLifecycleCallbacks(this, mAnalyticActivityLifecycleCallbacks);

        super.onCreate();
    }


    /*
     * Analytics.
     */

    private ActivityLifecycleCallbacksCompat mAnalyticActivityLifecycleCallbacks = new ActivityLifecycleCallbacksAdapter() {
        @Override
        public void onActivityStarted(Activity activity) {
            AnalyticsHelper.get().activityStart(activity);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            AnalyticsHelper.get().activityStop(activity);
        }
    };
}
