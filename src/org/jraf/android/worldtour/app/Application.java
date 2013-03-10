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

import java.util.UUID;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksAdapter;
import org.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksCompat;
import org.jraf.android.util.activitylifecyclecallbackscompat.ApplicationHelper;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.analytics.AnalyticsHelper;

import com.google.analytics.tracking.android.EasyTracker;

@ReportsCrashes(mode = ReportingInteractionMode.TOAST, resToastText = R.string.acra_toast, formKey = "dHYzRm43eWk1eEwxRGFfVHNURmxVY3c6MQ", customReportContent = {
        //@formatter:off
        ReportField.REPORT_ID,
        ReportField.APP_VERSION_CODE,
        ReportField.APP_VERSION_NAME,
        ReportField.PACKAGE_NAME,
        ReportField.FILE_PATH,
        ReportField.PHONE_MODEL,
        ReportField.BRAND,
        ReportField.PRODUCT,
        ReportField.ANDROID_VERSION,
        ReportField.BUILD,
        ReportField.TOTAL_MEM_SIZE,
        ReportField.AVAILABLE_MEM_SIZE,
        ReportField.CUSTOM_DATA,
        ReportField.IS_SILENT,
        ReportField.STACK_TRACE,
        ReportField.INITIAL_CONFIGURATION,
        ReportField.CRASH_CONFIGURATION,
        ReportField.DISPLAY,
        ReportField.USER_COMMENT,
        ReportField.USER_EMAIL,
        ReportField.USER_APP_START_DATE,
        ReportField.USER_CRASH_DATE,
        ReportField.DUMPSYS_MEMINFO,
        ReportField.LOGCAT,
        ReportField.INSTALLATION_ID,
        ReportField.DEVICE_FEATURES,
        ReportField.ENVIRONMENT,
        ReportField.SHARED_PREFERENCES,
        ReportField.SETTINGS_SYSTEM,
        ReportField.SETTINGS_SECURE
        //@formatter:on
}/*, applicationLogFile = Log.FILE, applicationLogFileLines = 300*/, logcatArguments = { "-t", "300", "-v", "time" })
public class Application extends android.app.Application {
    private static final String TAG = Constants.TAG + Application.class.getSimpleName();

    public static int sVersionCode;
    public static String sVersionName;

    @Override
    public void onCreate() {
        super.onCreate();
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

        // ACRA
        ACRA.init(this);

        // UUID
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String uuidStr = sharedPreferences.getString(Constants.PREF_UUID, null);
        UUID uuid;
        if (uuidStr == null) {
            uuid = UUID.randomUUID();
            uuidStr = uuid.toString();
            sharedPreferences.edit().putString(Constants.PREF_UUID, uuidStr).commit();
        } else {
            uuid = UUID.fromString(uuidStr);
        }

        // A/B testing
        //        if (Config.LOGD) Log.d(TAG, "onCreate uuid=" + uuid);
        //        if (Config.LOGD) Log.d(TAG, "onCreate uuid.getLeastSignificantBits()=" + uuid.getLeastSignificantBits());
        //        Constants.PHOTON = uuid.getLeastSignificantBits() % 2 == 0;
        //        Log.i(TAG, "PHOTON=" + Constants.PHOTON);
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
