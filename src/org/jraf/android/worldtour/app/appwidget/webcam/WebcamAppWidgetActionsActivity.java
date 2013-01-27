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
package org.jraf.android.worldtour.app.appwidget.webcam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.activitylifecyclecallbackscompat.app.LifecycleDispatchFragmentActivity;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.saveshare.SaveShareHelper;
import org.jraf.android.worldtour.app.saveshare.SaveShareListener;

public class WebcamAppWidgetActionsActivity extends LifecycleDispatchFragmentActivity implements OnClickListener, SaveShareListener {
    private static final String TAG = Constants.TAG + WebcamAppWidgetActionsActivity.class.getSimpleName();

    private static final String PREFIX = WebcamAppWidgetActionsActivity.class.getName() + ".";
    public static final String EXTRA_CURRENT_WEBCAM_ID = PREFIX + "EXTRA_CURRENT_WEBCAM_ID";

    private static final String FRAGMENT_DIALOG = "FRAGMENT_DIALOG";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return;

        ActionsDialogFragment actionsDialogFragment = ActionsDialogFragment.newInstance();
        actionsDialogFragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG);
    }

    public static class ActionsDialogFragment extends DialogFragment {
        public static ActionsDialogFragment newInstance() {
            ActionsDialogFragment res = new ActionsDialogFragment();
            return res;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), com.actionbarsherlock.R.style.Theme_Sherlock_Light));
            builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
            builder.setItems(getResources().getStringArray(R.array.webcamAppwidget_actions_labels), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((OnClickListener) getActivity()).onClick(dialog, which);
                }
            });
            return builder.create();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (Config.LOGD) Log.d(TAG, "onClick which=" + which);
        dialog.dismiss();

        switch (which) {
            case 0:
                // Share image
                SaveShareHelper.get().shareImage(this, getSupportFragmentManager(), mAppWidgetId);
                break;

            case 1:
                // Save image
                SaveShareHelper.get().saveImage(this, getSupportFragmentManager(), mAppWidgetId);
                break;

            case 2:
                // Pick another webcam
                Intent intent = getIntent();
                intent.setClass(this, WebcamConfigureActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void onDone() {
        finish();
    }
}
