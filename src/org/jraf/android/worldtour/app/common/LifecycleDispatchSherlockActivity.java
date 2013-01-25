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

import android.os.Bundle;

import org.jraf.android.util.activitylifecyclecallbackscompat.ApplicationHelper;
import org.jraf.android.util.activitylifecyclecallbackscompat.MainLifecycleDispatcher;

import com.actionbarsherlock.app.SherlockActivity;

public class LifecycleDispatchSherlockActivity extends SherlockActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ApplicationHelper.PRE_ICS) MainLifecycleDispatcher.get().onActivityCreated(this, savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ApplicationHelper.PRE_ICS) MainLifecycleDispatcher.get().onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ApplicationHelper.PRE_ICS) MainLifecycleDispatcher.get().onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ApplicationHelper.PRE_ICS) MainLifecycleDispatcher.get().onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ApplicationHelper.PRE_ICS) MainLifecycleDispatcher.get().onActivityStopped(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ApplicationHelper.PRE_ICS) MainLifecycleDispatcher.get().onActivitySaveInstanceState(this, outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ApplicationHelper.PRE_ICS) MainLifecycleDispatcher.get().onActivityDestroyed(this);
    }
}
