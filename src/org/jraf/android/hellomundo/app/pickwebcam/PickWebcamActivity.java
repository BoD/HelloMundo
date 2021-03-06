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
package org.jraf.android.hellomundo.app.pickwebcam;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.adduserwebcam.AddUserWebcamActivity;
import org.jraf.android.hellomundo.app.common.BaseActivity;
import org.jraf.android.hellomundo.app.service.HelloMundoService;
import org.jraf.android.hellomundo.provider.webcam.WebcamColumns;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.dialog.AlertDialogListener;
import org.jraf.android.util.log.wrapper.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PickWebcamActivity extends BaseActivity implements AlertDialogListener {
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
                        Log.d("position=" + position);
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
                    Log.d("User deleted currently selected webcam: reset to defaults");
                    Editor editor = sharedPreferences.edit();
                    editor.putLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
                    editor.putLong(Constants.PREF_CURRENT_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
                    editor.commit();

                    // Invoke service to download the default image now.
                    HelloMundoService.updateWallpaperNow(PickWebcamActivity.this);
                }
            }
        }.toastOk(R.string.pickWebcam_webcamDeletedToast)).execute(getSupportFragmentManager());
    }

    @Override
    public void onClickNegative(int tag, Object payload) {}


    @Override
    public void onClickListItem(int tag, int index, Object payload) {}
}
