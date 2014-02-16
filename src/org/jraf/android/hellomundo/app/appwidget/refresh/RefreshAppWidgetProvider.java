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
package org.jraf.android.hellomundo.app.appwidget.refresh;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import org.jraf.android.hellomundo.Config;
import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.service.WorldTourService;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.string.StringUtil;

public class RefreshAppWidgetProvider extends AppWidgetProvider {
    private static String TAG = Constants.TAG + RefreshAppWidgetProvider.class.getSimpleName();

    private static volatile boolean sAnimate;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Config.LOGD) Log.d(TAG, "onReceive intent=" + StringUtil.toString(intent));

        String action = intent.getAction();
        if (WorldTourService.ACTION_UPDATE_START.equals(action)) {
            if (sAnimate) {
                // Already animating
                return;
            }
            sAnimate = true;
            startAnimationThread(context);
        } else if (WorldTourService.ACTION_UPDATE_END.equals(action)) {
            sAnimate = false;
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (Config.LOGD) Log.d(TAG, "onUpdate");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (Config.LOGD) Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_refresh);
        views.setOnClickPendingIntent(R.id.image, WorldTourService.getUpdateAllPendingIntent(context));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void startAnimationThread(final Context context) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, getClass());
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);

        new Thread() {
            @Override
            public void run() {
                int anim = 0;
                while (sAnimate) {
                    for (int appWidgetId : appWidgetIds) {
                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_refresh);

                        switch (anim % 4) {
                            case 0:
                                if (Config.LOGD) Log.d(TAG, "run0");
                                views.setImageViewResource(R.id.image, R.drawable.widget_refresh_0);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                                break;
                            case 1:
                                if (Config.LOGD) Log.d(TAG, "run1");
                                views.setImageViewResource(R.id.image, R.drawable.widget_refresh_1);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                                break;
                            case 2:
                                if (Config.LOGD) Log.d(TAG, "run2");
                                views.setImageViewResource(R.id.image, R.drawable.widget_refresh_2);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                                break;
                            case 3:
                                if (Config.LOGD) Log.d(TAG, "run3");
                                views.setImageViewResource(R.id.image, R.drawable.widget_refresh_3);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                                break;
                        }
                    }

                    SystemClock.sleep(300);
                    anim++;
                }

                // Now set the default image / onClickListener
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_refresh);
                    views.setOnClickPendingIntent(R.id.image, WorldTourService.getUpdateAllPendingIntent(context));
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        }.start();
    }
}
