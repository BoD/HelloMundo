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
package org.jraf.android.hellomundo.app.adduserwebcam;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.jraf.android.hellomundo.app.common.BaseActivity;
import org.jraf.android.hellomundo.model.WebcamManager;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.validation.Validators;

import com.actionbarsherlock.app.ActionBar;

public class AddUserWebcamActivity extends BaseActivity {
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
            new TaskFragment(new Task<AddUserWebcamActivity>() {
                @Override
                protected void doInBackground() throws Exception {
                    String url = getActivity().mEdtUrl.getText().toString().trim();
                    if (url.startsWith("http://")) url = url.substring(7);
                    WebcamManager.get().insertUserWebcam(AddUserWebcamActivity.this, getActivity().mEdtName.getText().toString().trim(), url);
                }

                @Override
                protected void onPostExecuteOk() {
                    setResult(RESULT_OK);
                    getActivity().finish();
                }
            }).execute(getSupportFragmentManager());
        }
    };

    private final OnClickListener mDiscardOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}