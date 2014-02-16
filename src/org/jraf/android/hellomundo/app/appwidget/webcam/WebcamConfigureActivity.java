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

import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.pickwebcam.PickWebcamActivity;
import org.jraf.android.hellomundo.app.service.WorldTourService;
import org.jraf.android.hellomundo.model.AppwidgetManager;
import org.jraf.android.util.activitylifecyclecallbackscompat.app.LifecycleDispatchActivity;

public class WebcamConfigureActivity extends LifecycleDispatchActivity {
    private static final String PREFIX = WebcamConfigureActivity.class.getName() + ".";
    public static final String EXTRA_CURRENT_WEBCAM_ID = PREFIX + "EXTRA_CURRENT_WEBCAM_ID";

    private static final int REQUEST_PICK_WEBCAM = 0;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return;
        startActivityForResult(
                new Intent(this, PickWebcamActivity.class).putExtra(PickWebcamActivity.EXTRA_CURRENT_WEBCAM_ID,
                        getIntent().getLongExtra(EXTRA_CURRENT_WEBCAM_ID, Constants.WEBCAM_ID_NONE)), REQUEST_PICK_WEBCAM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        if (resultCode == RESULT_CANCELED) {
            setResult(RESULT_CANCELED, resultValue);
            finish();
            return;
        }

        long webcamId = ContentUris.parseId(data.getData());
        AppwidgetManager.get().insertOrUpdate(this, mAppWidgetId, webcamId, Constants.WEBCAM_ID_NONE);

        WorldTourService.updateWidgetsNow(this);

        setResult(RESULT_OK, resultValue);
        finish();
    }

}
