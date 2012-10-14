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
import android.view.ViewGroup;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.worldtour.Constants;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AddUserWebcamActivity extends SherlockFragmentActivity {
    private static String TAG = Constants.TAG + AddUserWebcamActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_webcam);

        final View customActionBarView = getLayoutInflater().inflate(R.layout.add_user_webcam_actionbar, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Done"
                finish(); // TODO: don't just finish()!
            }
        });
        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Discard"
                finish(); // TODO: don't just finish()!
            }
        });


        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

}