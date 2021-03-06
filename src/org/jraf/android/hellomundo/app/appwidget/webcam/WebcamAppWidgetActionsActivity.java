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

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.main.MainActivity;
import org.jraf.android.hellomundo.app.saveshare.SaveShareHelper;
import org.jraf.android.hellomundo.app.saveshare.SaveShareListener;
import org.jraf.android.hellomundo.app.service.HelloMundoService;
import org.jraf.android.hellomundo.provider.webcam.WebcamCursor;
import org.jraf.android.hellomundo.provider.webcam.WebcamSelection;
import org.jraf.android.hellomundo.provider.webcam.WebcamType;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.string.StringUtil;

public class WebcamAppWidgetActionsActivity extends FragmentActivity implements OnClickListener, SaveShareListener {
    private static final String PREFIX = WebcamAppWidgetActionsActivity.class.getName() + ".";
    public static final String EXTRA_WEBCAM_ID = PREFIX + "EXTRA_CURRENT_WEBCAM_ID";
    public static final String EXTRA_CURRENT_WEBCAM_ID = PREFIX + "EXTRA_CURRENT_WEBCAM_ID";

    private static final String FRAGMENT_DIALOG = "FRAGMENT_DIALOG";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private long mWebcamId;
    private long mCurrentWebcamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("intent=" + StringUtil.toString(getIntent()));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            mWebcamId = extras.getLong(EXTRA_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
            mCurrentWebcamId = extras.getLong(EXTRA_CURRENT_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d("Received an invalid appwidget id: abort");
            finish();
            return;
        }

        new TaskFragment(new Task<MainActivity>() {
            private String mName;
            private String mLocation;
            private String mTimeZone;
            private String mPublicId;
            private WebcamType mType;

            @Override
            protected void doInBackground() throws Throwable {
                WebcamCursor cursor = new WebcamSelection().id(mCurrentWebcamId).query(getContentResolver());
                try {
                    if (cursor == null || !cursor.moveToFirst()) {
                        throw new Exception("Could not find webcam with id=" + mCurrentWebcamId);
                    }
                    mName = cursor.getName();
                    mLocation = cursor.getLocation();
                    mTimeZone = cursor.getTimezone();
                    mPublicId = cursor.getPublicId();
                    mType = cursor.getType();
                } finally {
                    if (cursor != null) cursor.close();
                }
            }

            @Override
            protected void onPostExecuteOk() {
                String title = mName;
                if (mType != WebcamType.USER) {
                    String location = mLocation;
                    boolean specialCam = Constants.SPECIAL_CAMS.contains(mPublicId);
                    if (!specialCam) {
                        location += " - " + DateTimeUtil.getCurrentTimeForTimezone(WebcamAppWidgetActionsActivity.this, mTimeZone);
                    }
                    title += ", " + location;
                }

                ActionsDialogFragment actionsDialogFragment = ActionsDialogFragment.newInstance(title);
                actionsDialogFragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG);
            }
        }).execute(getSupportFragmentManager());
    }

    public static class ActionsDialogFragment extends DialogFragment {
        private String mTitle;

        public static ActionsDialogFragment newInstance(String title) {
            ActionsDialogFragment res = new ActionsDialogFragment();
            Bundle args = new Bundle(1);
            args.putString("title", title);
            res.setArguments(args);
            return res;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = getArguments().getString("title");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ContextThemeWrapper contextWithTheme = new ContextThemeWrapper(getActivity(), R.style.Theme_WebcamAppWidgetActions);
            //            final ContextThemeWrapper contextWithTheme = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Light);


            AlertDialog.Builder builder = new AlertDialog.Builder(contextWithTheme);
            builder.setTitle(mTitle);
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
            //            dialog.getListView().setBackgroundResource(R.drawable.abs__ab_solid_light_holo);
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
                intent.putExtra(WebcamConfigureActivity.EXTRA_CURRENT_WEBCAM_ID, mWebcamId);
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
