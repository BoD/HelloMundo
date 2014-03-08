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
package org.jraf.android.hellomundo.app;

import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.jraf.android.hellomundo.Config;
import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.analytics.AnalyticsHelper;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksAdapter;
import org.jraf.android.util.activitylifecyclecallbackscompat.ActivityLifecycleCallbacksCompat;
import org.jraf.android.util.activitylifecyclecallbackscompat.ApplicationHelper;
import org.jraf.android.util.log.wrapper.Log;

import com.google.analytics.tracking.android.EasyTracker;

//@formatter:off
@ReportsCrashes(
    mode = ReportingInteractionMode.TOAST, 
    resToastText = R.string.acra_toast, 
    formKey = "", 
    formUri = "https://bod.cloudant.com/acra-worldtour/_design/acra-storage/_update/report",
    reportType = org.acra.sender.HttpSender.Type.JSON,
    httpMethod = org.acra.sender.HttpSender.Method.PUT,
    customReportContent = {
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
    }, 
    logcatArguments = { "-t", "300", "-v", "time" })
//@formatter:on
public class Application extends android.app.Application {
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

        // Log
        Log.init("HelloMundo");

        // Google Analytics
        EasyTracker.getInstance().setContext(this);
        ApplicationHelper.registerActivityLifecycleCallbacks(this, mAnalyticActivityLifecycleCallbacks);

        // ACRA
        if (Config.ACRA) {
            try {
                ACRA.init(this);
                ACRAConfiguration config = ACRA.getConfig();
                config.setFormUriBasicAuthLogin(getString(R.string.acra_login));
                config.setFormUriBasicAuthPassword(getString(R.string.acra_password));
            } catch (Throwable t) {
                Log.w("Problem while initializing ACRA", t);
            }
        }

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

        if (Config.STRICT_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) setupStrictMode();
    }


    /*
     * Strict mode.
     */

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void setupStrictMode() {
        // Do this in a Handler.post because of this issue: http://code.google.com/p/android/issues/detail?id=35298
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
            }
        });
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
