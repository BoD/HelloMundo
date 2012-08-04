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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Application extends android.app.Application {
    public static int sVersionCode;
    public static String sVersionName;

    @Override
    public void onCreate() {
        synchronized (this) {
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                sVersionCode = packageInfo.versionCode;
                sVersionName = packageInfo.versionName;
            } catch (NameNotFoundException e) {
                // should never happen
                throw new AssertionError(e);
            }
        }

        super.onCreate();
    }
}
