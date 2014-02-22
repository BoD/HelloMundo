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
import android.widget.RemoteViews;

import org.jraf.android.hellomundo.app.service.HelloMundoService;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class RefreshAppWidgetProvider extends AppWidgetProvider {
    private static volatile boolean sAnimate;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("intent=" + StringUtil.toString(intent));

        String action = intent.getAction();
        if (HelloMundoService.ACTION_UPDATE_START.equals(action)) {
            if (sAnimate) {
                // Already animating
                return;
            }
            sAnimate = true;
            startAnimationThread(context);
        } else if (HelloMundoService.ACTION_UPDATE_END.equals(action)) {
            sAnimate = false;
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d();
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d("appWidgetId=" + appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_refresh);
        views.setOnClickPendingIntent(R.id.image, HelloMundoService.getUpdateAllPendingIntent(context));
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
                                Log.d("run0");
                                views.setImageViewResource(R.id.image, R.drawable.widget_refresh_0);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                                break;
                            case 1:
                                Log.d("run1");
                                views.setImageViewResource(R.id.image, R.drawable.widget_refresh_1);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                                break;
                            case 2:
                                Log.d("run2");
                                views.setImageViewResource(R.id.image, R.drawable.widget_refresh_2);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                                break;
                            case 3:
                                Log.d("run3");
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
                    views.setOnClickPendingIntent(R.id.image, HelloMundoService.getUpdateAllPendingIntent(context));
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        }.start();
    }
}
