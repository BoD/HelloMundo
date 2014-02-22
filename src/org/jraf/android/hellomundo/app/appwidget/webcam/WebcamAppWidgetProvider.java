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
package org.jraf.android.hellomundo.app.appwidget.webcam;

import android.appwidget.AppWidgetProvider;
import android.content.Context;

import org.jraf.android.hellomundo.model.AppwidgetManager;
import org.jraf.android.util.collection.CollectionUtil;
import org.jraf.android.util.log.wrapper.Log;

public class WebcamAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d("context=" + context + " appWidgetIds=" + CollectionUtil.asList(appWidgetIds));
        AppwidgetManager.get().delete(context, appWidgetIds);
    }
}
