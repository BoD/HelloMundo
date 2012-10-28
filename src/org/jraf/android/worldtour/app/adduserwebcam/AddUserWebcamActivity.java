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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.SimpleAsyncTaskFragment;
import org.jraf.android.util.validation.OnValidationListener;
import org.jraf.android.util.validation.Validators;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.model.WebcamManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AddUserWebcamActivity extends SherlockFragmentActivity {
    protected static final String FRAGMENT_ASYNC_TASK = "FRAGMENT_ASYNC_TASK";

    private static String TAG = Constants.TAG + AddUserWebcamActivity.class.getSimpleName();

    private View mBtnDone;
    private EditText mEdtName;
    private EditText mEdtUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_webcam);
        Validators validators = Validators.newValidators(mOnValidationListener);

        final View customActionBarView = getLayoutInflater().inflate(R.layout.add_user_webcam_actionbar, null);
        mBtnDone = customActionBarView.findViewById(R.id.actionbar_done);
        mBtnDone.setOnClickListener(mDoneOnClickListener);

        View btnDiscard = customActionBarView.findViewById(R.id.actionbar_discard);
        btnDiscard.setOnClickListener(mDiscardOnClickListener);

        mEdtUrl = (EditText) findViewById(R.id.edtUrl);
        validators.addUrlValidator(mEdtUrl);

        mEdtName = (EditText) findViewById(R.id.edtName);
        validators.addNotEmptyValidator(mEdtName);

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        validators.validate();
    }

    private final OnValidationListener mOnValidationListener = new OnValidationListener() {
        @Override
        public void onValidation(boolean valid) {
            mBtnDone.setEnabled(valid);
        }
    };

    private final OnClickListener mDoneOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            getSupportFragmentManager().beginTransaction().add(new SimpleAsyncTaskFragment() {
                @Override
                protected void background() throws Exception {
                    WebcamManager.get().insertUserWebcam(AddUserWebcamActivity.this, mEdtName.getText().toString().trim(), mEdtUrl.getText().toString().trim());
                }

                @Override
                protected void postExecute(boolean ok) {
                    finish();
                }
            }, FRAGMENT_ASYNC_TASK).commit();
        }
    };

    private final OnClickListener mDiscardOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}