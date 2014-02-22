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

import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jraf.android.hellomundo.app.saveshare.SaveShareHelper;
import org.jraf.android.hellomundo.app.saveshare.SaveShareListener;
import org.jraf.android.hellomundo.app.service.HelloMundoService;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.activitylifecyclecallbackscompat.app.LifecycleDispatchFragmentActivity;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class WebcamAppWidgetActionsActivity extends LifecycleDispatchFragmentActivity implements OnClickListener, SaveShareListener {
    private static final String PREFIX = WebcamAppWidgetActionsActivity.class.getName() + ".";
    public static final String EXTRA_CURRENT_WEBCAM_ID = PREFIX + "EXTRA_CURRENT_WEBCAM_ID";

    private static final String FRAGMENT_DIALOG = "FRAGMENT_DIALOG";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("intent=" + StringUtil.toString(getIntent()));
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
            final ContextThemeWrapper contextWithTheme = new ContextThemeWrapper(getActivity(), com.actionbarsherlock.R.style.Theme_Sherlock);
            //            final ContextThemeWrapper contextWithTheme = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Light);


            AlertDialog.Builder builder = new AlertDialog.Builder(contextWithTheme);
            builder.setNegativeButton(android.R.string.cancel, null);
            //            builder.setItems(getResources().getStringArray(R.array.webcamAppwidget_actions_labels), new OnClickListener() {
            //                @Override
            //                public void onClick(DialogInterface dialog, int which) {
            //                    ((OnClickListener) getActivity()).onClick(dialog, which);
            //                }
            //            });
            builder.setAdapter(new ArrayAdapter<String>(contextWithTheme, android.R.layout.simple_list_item_1, android.R.id.text1, getResources()
                    .getStringArray(R.array.webcamAppwidget_actions_labels)) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView res = (TextView) super.getView(position, convertView, parent);
                    int icon = 0;
                    switch (position) {
                        case 0:
                            // Pick another webcam
                            icon = R.drawable.ic_action_pick;
                            break;

                        case 1:
                            // Refresh
                            icon = R.drawable.ic_action_refresh;
                            break;

                        case 2:
                            // Share image
                            icon = R.drawable.ic_action_share;
                            break;

                        case 3:
                            // Save image
                            icon = R.drawable.ic_action_save;
                            break;


                    }
                    res.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
                    res.setCompoundDrawablePadding(contextWithTheme.getResources().getDimensionPixelSize(R.dimen.webcamAppwidgetActions_iconPadding));



                    return res;
                }
            }, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((OnClickListener) getActivity()).onClick(dialog, which);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.getListView().setBackgroundResource(R.drawable.abs__ab_solid_dark_holo);
            return dialog;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            Log.d();
            FragmentActivity activity = getActivity();
            if (activity != null) activity.finish();
        }

        @Override
        public void onDestroyView() {
            // Workaround for http://code.google.com/p/android/issues/detail?id=17423
            if (getDialog() != null) {
                getDialog().setOnDismissListener(null);
            }
            super.onDestroyView();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Log.d("which=" + which);
        // We don't finish the activity right now, so remove the OnDismissListener
        ((Dialog) dialog).setOnDismissListener(null);
        dialog.dismiss();

        switch (which) {

            case 0:
                // Pick another webcam
                Intent intent = getIntent();
                intent.setClass(this, WebcamConfigureActivity.class);
                if (getIntent().getExtras() != null) {
                    intent.putExtra(WebcamConfigureActivity.EXTRA_CURRENT_WEBCAM_ID,
                            getIntent().getLongExtra(EXTRA_CURRENT_WEBCAM_ID, AppWidgetManager.INVALID_APPWIDGET_ID));
                }
                startActivity(intent);
                finish();
                break;

            case 1:
                // Refresh
                HelloMundoService.updateWidgetsNow(this);
                finish();
                break;

            case 2:
                // Share image
                SaveShareHelper.get().shareImage(getSupportFragmentManager(), mAppWidgetId);
                break;

            case 3:
                // Save image
                SaveShareHelper.get().saveImage(getSupportFragmentManager(), mAppWidgetId);
                break;


        }
    }

    @Override
    public void onDone() {
        finish();
    }
}
