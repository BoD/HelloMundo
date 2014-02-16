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
package org.jraf.android.hellomundo.app.main;

import java.io.File;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jraf.android.backport.switchwidget.Switch;
import org.jraf.android.hellomundo.Config;
import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.app.about.AboutActivity;
import org.jraf.android.hellomundo.app.common.LifecycleDispatchSherlockFragmentActivity;
import org.jraf.android.hellomundo.app.pickwebcam.PickWebcamActivity;
import org.jraf.android.hellomundo.app.preference.PreferenceActivity;
import org.jraf.android.hellomundo.app.saveshare.SaveShareHelper;
import org.jraf.android.hellomundo.app.service.HelloMundoService;
import org.jraf.android.hellomundo.app.welcome.WelcomeActivity;
import org.jraf.android.hellomundo.model.AppwidgetManager;
import org.jraf.android.hellomundo.model.WebcamManager;
import org.jraf.android.hellomundo.provider.webcam.WebcamCursor;
import org.jraf.android.hellomundo.provider.webcam.WebcamSelection;
import org.jraf.android.hellomundo.provider.webcam.WebcamType;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.bitmap.BitmapUtil;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.ui.UiUtil;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends LifecycleDispatchSherlockFragmentActivity {
    private static String TAG = Constants.TAG + MainActivity.class.getSimpleName();

    private static final int REQUEST_PICK_WEBCAM = 0;
    private static final int REQUEST_SETTINGS = 1;

    private boolean mBroadcastReceiverRegistered;
    private boolean mLoading;
    private MenuItem mRefreshMenuItem;
    private boolean mNeedToUpdateWebcamInfo = true;
    private boolean mImageAvailable;

    private Switch mSwiOnOff;
    private ImageView mImgPreview;
    private View mImgPreviewFrame;
    private TextView mTxtWebcamInfoName;
    private TextView mTxtWebcamInfoLocation;
    private View mTxtNoImageWarning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSwiOnOff = (Switch) findViewById(R.id.swiOnOff);
        mImgPreview = (ImageView) findViewById(R.id.imgPreview);
        mImgPreview.setOnClickListener(mImgPreviewOnClickListener);
        mImgPreviewFrame = findViewById(R.id.imgPreviewFrame);
        mTxtWebcamInfoName = (TextView) findViewById(R.id.txtWebcamInfo_name);
        mTxtWebcamInfoLocation = (TextView) findViewById(R.id.txtWebcamInfo_location);
        mTxtNoImageWarning = findViewById(R.id.txtNoImageWarning);

        getSupportActionBar().setLogo(R.drawable.ic_home);
        setTitle(null);

        showWelcome();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HelloMundoService.ACTION_UPDATE_WALLPAPER_START);
        intentFilter.addAction(HelloMundoService.ACTION_UPDATE_WALLPAPER_END_FAILURE);
        intentFilter.addAction(HelloMundoService.ACTION_UPDATE_WALLPAPER_END_SUCCESS);
        registerReceiver(mBroadcastReceiver, intentFilter);
        mBroadcastReceiverRegistered = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = sharedPreferences.getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT);
        mSwiOnOff.setOnCheckedChangeListener(null);
        mSwiOnOff.setChecked(enabled);
        mSwiOnOff.setOnCheckedChangeListener(mOnOffOnCheckedChangeListener);

        if (!mNeedToUpdateWebcamInfo) {
            mNeedToUpdateWebcamInfo = true;
            return;
        }

        final boolean firstRun = sharedPreferences.getBoolean(Constants.PREF_FIRST_RUN, true);
        new TaskFragment(new Task<MainActivity>() {
            @Override
            protected void doInBackground() throws Throwable {
                if (firstRun) {
                    sharedPreferences.edit().putBoolean(Constants.PREF_FIRST_RUN, false).commit();
                    handleFirstRun();
                }
            }

            @Override
            protected void onPostExecuteOk() {
                getActivity().updateWebcamRandom();
                getActivity().updateWebcamName();
                getActivity().updateWebcamImage();
            }
        }).execute(getSupportFragmentManager());
    }

    @Override
    protected void onStop() {
        if (mBroadcastReceiverRegistered) {
            unregisterReceiver(mBroadcastReceiver);
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) return;
        mNeedToUpdateWebcamInfo = false;
        switch (requestCode) {
            case REQUEST_PICK_WEBCAM:
                long selectedWebcamId = ContentUris.parseId(data.getData());
                if (Config.LOGD) Log.d(TAG, "onActivityResult selectedWebcamId=" + selectedWebcamId);
                PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(Constants.PREF_SELECTED_WEBCAM_ID, selectedWebcamId).commit();
                updateWebcamRandom();
                HelloMundoService.updateWallpaperNow(this);
                break;

            case REQUEST_SETTINGS:
                // Reset the alarm because the use may have changed the frequency in the settings
                setAlarm(mSwiOnOff.isChecked());
                break;
        }
    }


    /*
     * First run.
     */

    @Background
    private void handleFirstRun() {
        if (Config.LOGD) Log.d(TAG, "handleFirstRun");
        WebcamManager.get().insertWebcamsFromBundledFile(this);
    }



    /*
     * Action bar.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        mRefreshMenuItem = menu.getItem(1);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_refresh).setEnabled(!mLoading);
        menu.findItem(R.id.menu_save).setEnabled(!mLoading);
        menu.findItem(R.id.menu_share).setEnabled(!mLoading);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pick:
                long selectedWebcamId = PreferenceManager.getDefaultSharedPreferences(this).getLong(Constants.PREF_SELECTED_WEBCAM_ID,
                        Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
                startActivityForResult(new Intent(this, PickWebcamActivity.class).putExtra(PickWebcamActivity.EXTRA_CURRENT_WEBCAM_ID, selectedWebcamId),
                        REQUEST_PICK_WEBCAM);
                return true;

            case R.id.menu_settings:
                startActivityForResult(new Intent(this, PreferenceActivity.class), REQUEST_SETTINGS);
                return true;

            case R.id.menu_refresh:
                if (mLoading) return true;
                HelloMundoService.updateAllNow(this);
                return true;

            case R.id.menu_save:
                SaveShareHelper.get().saveImage(getSupportFragmentManager(), SaveShareHelper.WALLPAPER);
                return true;

            case R.id.menu_share:
                SaveShareHelper.get().shareImage(getSupportFragmentManager(), SaveShareHelper.WALLPAPER);
                return true;

            case R.id.menu_help:
                View btnPick = findViewById(R.id.menu_pick);
                showWelcomeScreen(btnPick, mSwiOnOff);
                return true;

            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     * Broadcast receiver.
     */

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Config.LOGD) Log.d(TAG, "onReceive context=" + context + " intent=" + intent);
            String action = intent.getAction();
            if (HelloMundoService.ACTION_UPDATE_WALLPAPER_START.equals(action)) {
                updateWebcamName();
                setLoading(true);
            } else if (HelloMundoService.ACTION_UPDATE_WALLPAPER_END_SUCCESS.equals(action)) {
                setLoading(false);
                updateWebcamImage();
                if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER,
                        Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT)) {
                    Toast.makeText(MainActivity.this, R.string.main_toast_wallpaperUpdated, Toast.LENGTH_SHORT).show();
                }
            } else if (HelloMundoService.ACTION_UPDATE_WALLPAPER_END_FAILURE.equals(action)) {
                setLoading(false);
                if (!mImageAvailable) {
                    setNoImageWarningVisible(true);
                }
            }
        }
    };

    private void setNoImageWarningVisible(boolean visible) {
        mTxtNoImageWarning.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    /*
     * Switch on/off.
     */

    private final OnCheckedChangeListener mOnOffOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
            new TaskFragment(new Task<MainActivity>() {
                @Override
                protected void doInBackground() throws Throwable {
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, isChecked)
                            .commit();
                }

                @Override
                protected void onPostExecuteOk() {
                    getActivity().setAlarm(isChecked);
                }
            }).execute(getSupportFragmentManager());
        }
    };

    private void setAlarm(boolean enabled) {
        long interval = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Constants.PREF_UPDATE_INTERVAL,
                Constants.PREF_UPDATE_INTERVAL_DEFAULT));

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent wallpaperPendingIntent = HelloMundoService.getWallpaperAlarmPendingIntent(MainActivity.this);
        if (enabled) {
            // Set the alarm
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, wallpaperPendingIntent);

            // Update the wallpaper now
            HelloMundoService.updateWallpaperNow(this);
        } else {
            alarmManager.cancel(wallpaperPendingIntent);
        }

        int widgetCount = AppwidgetManager.get().getWidgetCount(this);
        if (widgetCount > 0) {
            PendingIntent widgetsPendingIntent = HelloMundoService.getWidgetsAlarmPendingIntent(this);
            // Set the alarm to trigger in 1 minute (allows for the network to be up)
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, widgetsPendingIntent);
        }
    }


    /*
     * Preview img.
     */

    private final OnClickListener mImgPreviewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            long selectedWebcamId = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getLong(Constants.PREF_SELECTED_WEBCAM_ID,
                    Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
            startActivityForResult(
                    new Intent(MainActivity.this, PickWebcamActivity.class).putExtra(PickWebcamActivity.EXTRA_CURRENT_WEBCAM_ID, selectedWebcamId),
                    REQUEST_PICK_WEBCAM);
        }
    };


    /*
     * Webcam info.
     */

    private void updateWebcamRandom() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long selectedWebcamId = preferences.getLong(Constants.PREF_SELECTED_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
        if (selectedWebcamId == Constants.WEBCAM_ID_RANDOM) {
            findViewById(R.id.imgWebcamInfo_random).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.imgWebcamInfo_random).setVisibility(View.GONE);
        }
    }

    private void updateWebcamName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final long currentWebcamId = preferences.getLong(Constants.PREF_CURRENT_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
        new TaskFragment(new Task<MainActivity>() {
            private String mName;
            private String mLocation;
            private String mTimeZone;
            private String mPublicId;
            private WebcamType mType;

            @Override
            protected void doInBackground() throws Throwable {
                WebcamSelection where = new WebcamSelection().id(currentWebcamId);
                WebcamCursor cursor = where.query(getContentResolver());
                try {
                    if (cursor == null || !cursor.moveToFirst()) {
                        throw new Exception("Could not find webcam with id=" + currentWebcamId);
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
                getActivity().mTxtWebcamInfoName.setText(mName);
                if (mType == WebcamType.USER) {
                    getActivity().mTxtWebcamInfoLocation.setText(R.string.common_userDefined);
                } else {
                    String location = mLocation;
                    boolean specialCam = Constants.SPECIAL_CAMS.contains(mPublicId);
                    if (!specialCam) {
                        location += " - " + DateTimeUtil.getCurrentTimeForTimezone(MainActivity.this, mTimeZone);
                    }
                    getActivity().mTxtWebcamInfoLocation.setText(location);
                }
            }
        }).execute(getSupportFragmentManager());
    }

    private void updateWebcamImage() {
        if (!new File(getFilesDir(), Constants.FILE_IMAGE_WALLPAPER).exists()) {
            // The service has never been started, there is no file yet: start it now
            HelloMundoService.updateWallpaperNow(this);
            mImgPreviewFrame.setVisibility(View.INVISIBLE);
            mImageAvailable = false;
            return;
        }
        setNoImageWarningVisible(false);
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return BitmapUtil.tryDecodeFile(new File(getFilesDir(), Constants.FILE_IMAGE_WALLPAPER), null);
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result == null) return;
                mImgPreview.setImageBitmap(result);
                mImgPreviewFrame.setVisibility(View.VISIBLE);
                mImageAvailable = true;
            }
        }.execute();
    }


    /*
     * Loading.
     */

    private void setLoading(boolean loading) {
        mLoading = loading;
        if (mRefreshMenuItem == null) return;
        if (loading) {
            mRefreshMenuItem.setActionView(R.layout.main_refreshing);
        } else {
            mRefreshMenuItem.setActionView(null);
        }
    }


    /*
     * Welcome screen.
     */

    private void showWelcome() {
        // Check for need for welcome screen
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int welcomeResumeIndex = sharedPreferences.getInt(Constants.PREF_WELCOME_RESUME_INDEX, -1);
        boolean seenWelcome = sharedPreferences.getBoolean(Constants.PREF_SEEN_WELCOME, false);
        if (!seenWelcome || welcomeResumeIndex != -1) {
            getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    View btnPick = findViewById(R.id.menu_pick);
                    View swiOnOff = findViewById(R.id.swiOnOff);

                    // This could be called when the views are not there yet, so we must test for null
                    if (btnPick != null && swiOnOff != null) {
                        showWelcomeScreen(btnPick, swiOnOff);

                        // Now get rid of this listener
                        getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }

    private void showWelcomeScreen(View btnPick, View swiOnOff) {
        // Pick button
        Rect rectPick = UiUtil.getLocationInWindow(btnPick);

        // On/off switch
        Rect rectSwiOnOff = UiUtil.getLocationInWindow(swiOnOff);

        showWelcomeScreen(rectPick, rectSwiOnOff);
    }

    private void showWelcomeScreen(Rect rectPick, Rect rectSwiOnOff) {
        if (Config.LOGD) Log.d(TAG, "showWelcomeScreen rectPick=" + rectPick + " rectSwiOnOff=" + rectSwiOnOff);
        startActivity(new Intent(this, WelcomeActivity.class).putExtra(WelcomeActivity.EXTRA_RECT_PICK, rectPick).putExtra(WelcomeActivity.EXTRA_RECT_SWITCH,
                rectSwiOnOff));
    }
}