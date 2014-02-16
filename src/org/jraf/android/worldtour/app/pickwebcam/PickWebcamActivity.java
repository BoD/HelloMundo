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

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.adduserwebcam.AddUserWebcamActivity;
import org.jraf.android.worldtour.app.common.LifecycleDispatchSherlockFragmentActivity;
import org.jraf.android.worldtour.app.service.WorldTourService;
import org.jraf.android.worldtour.provider.webcam.WebcamColumns;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PickWebcamActivity extends LifecycleDispatchSherlockFragmentActivity implements AlertDialogListener {
    private static final String TAG = Constants.TAG + PickWebcamActivity.class.getSimpleName();

    private static final int REQUEST_NEW_WEBCAM = 0;
    private static final String PREFIX = PickWebcamActivity.class.getName() + ".";
    public static final String EXTRA_CURRENT_WEBCAM_ID = PREFIX + "EXTRA_CURRENT_WEBCAM_ID";

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_webcam);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.conList) == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            PickWebcamListFragment pickWebcamListFragment = new PickWebcamListFragment();
            pickWebcamListFragment.setCurrentWebcamId(getIntent().getLongExtra(EXTRA_CURRENT_WEBCAM_ID, Constants.WEBCAM_ID_NONE));
            transaction.add(R.id.conList, pickWebcamListFragment);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) return;
        switch (requestCode) {
            case REQUEST_NEW_WEBCAM:
                Toast.makeText(PickWebcamActivity.this, R.string.pickWebcam_webcamAddedToast, Toast.LENGTH_SHORT).show();
                final PickWebcamListFragment pickWebcamListFragment = (PickWebcamListFragment) getSupportFragmentManager().findFragmentById(R.id.conList);
                // Go to the bottom of the list.
                // We have to wait a bit to do this because the listview has to be updated (after the loader is restarted by the system)
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int position = pickWebcamListFragment.getListAdapter().getCount() - 1;
                        if (Config.LOGD) Log.d(TAG, "position=" + position);
                        pickWebcamListFragment.getListView().setSelection(position);
                    }
                }, 200);
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);

        }
    }

    /*
     * Alert dialog.
     */

    @Override
    public void onClickPositive(int tag, Object payload) {
        final long id = (Long) payload;
        new TaskFragment(new Task<PickWebcamActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                getContentResolver().delete(ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, id), null, null);

                // Check if the selected cam is the one we just deleted, if yes set the eiffel tower one
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PickWebcamActivity.this);
                long selectedWebcamId = sharedPreferences.getLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
                if (id == selectedWebcamId) {
                    if (Config.LOGD) Log.d(TAG, "onClickPositive User deleted currently selected webcam: reset to defaults");
                    Editor editor = sharedPreferences.edit();
                    editor.putLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
                    editor.putLong(Constants.PREF_CURRENT_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
                    editor.commit();

                    // Invoke service to download the default image now.
                    WorldTourService.updateWallpaperNow(PickWebcamActivity.this);
                }
            }
        }.toastOk(R.string.pickWebcam_webcamDeletedToast)).execute(getSupportFragmentManager());
    }

    @Override
    public void onClickNegative(int tag, Object payload) {}


    @Override
    public void onClickListItem(int tag, int index, Object payload) {}
}
