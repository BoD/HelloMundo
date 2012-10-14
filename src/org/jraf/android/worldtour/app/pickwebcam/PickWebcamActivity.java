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
package org.jraf.android.worldtour.app.pickwebcam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.worldtour.app.adduserwebcam.AddUserWebcamActivity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PickWebcamActivity extends SherlockFragmentActivity {
    private static final int REQUEST_NEW_WEBCAM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_webcam);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.root) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.root, new PickWebcamListFragment());
            transaction.commit();
        }
    }


    /*
     * Action bar.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.pick_webcam, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_newWebcam:
                startActivityForResult(new Intent(this, AddUserWebcamActivity.class), REQUEST_NEW_WEBCAM);
        }
        return super.onOptionsItemSelected(item);
    }
}
