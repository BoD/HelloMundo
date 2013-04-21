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
package org.jraf.android.worldtour.app.adduserwebcam;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.SimpleAsyncTaskFragment;
import org.jraf.android.util.validation.Validators;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.common.LifecycleDispatchSherlockFragmentActivity;
import org.jraf.android.worldtour.model.WebcamManager;

import com.actionbarsherlock.app.ActionBar;

public class AddUserWebcamActivity extends LifecycleDispatchSherlockFragmentActivity {

    private static String TAG = Constants.TAG + AddUserWebcamActivity.class.getSimpleName();

    private EditText mEdtName;
    private EditText mEdtUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_webcam);
        Validators validators = Validators.newValidators();

        final View customActionBarView = getLayoutInflater().inflate(R.layout.add_user_webcam_actionbar, null);
        final View btnDone = customActionBarView.findViewById(R.id.actionbar_done);
        btnDone.setOnClickListener(mDoneOnClickListener);
        validators.enableWhenValid(btnDone);

        View btnDiscard = customActionBarView.findViewById(R.id.actionbar_discard);
        btnDiscard.setOnClickListener(mDiscardOnClickListener);

        mEdtUrl = (EditText) findViewById(R.id.edtUrl);
        mEdtUrl.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (btnDone.isEnabled()) btnDone.performClick();
                return true;
            }
        });
        validators.addUrlValidator(mEdtUrl);

        mEdtName = (EditText) findViewById(R.id.edtName);
        validators.addNotEmptyValidator(mEdtName);

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        validators.validate();
    }

    private final OnClickListener mDoneOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            new SimpleAsyncTaskFragment() {
                @Override
                protected void doInBackground() throws Exception {
                    String url = mEdtUrl.getText().toString().trim();
                    if (url.startsWith("http://")) {
                        url = url.substring(7);
                    }
                    WebcamManager.get().insertUserWebcam(AddUserWebcamActivity.this, mEdtName.getText().toString().trim(), url);
                }

                @Override
                protected void onPostExecute(boolean ok) {
                    setResult(RESULT_OK);
                    finish();
                }
            }.execute(getSupportFragmentManager());
        }
    };

    private final OnClickListener mDiscardOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}