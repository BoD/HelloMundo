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
package org.jraf.android.worldtour.app.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jraf.android.backport.switchwidget.Switch;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.Blocking;
import org.jraf.android.util.DateTimeUtil;
import org.jraf.android.util.EnvironmentUtil;
import org.jraf.android.util.FileUtil;
import org.jraf.android.util.IoUtil;
import org.jraf.android.util.MediaScannerUtil;
import org.jraf.android.util.SimpleAsyncTask;
import org.jraf.android.util.SimpleAsyncTaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.about.AboutActivity;
import org.jraf.android.worldtour.app.pickwebcam.PickWebcamActivity;
import org.jraf.android.worldtour.app.preference.PreferenceActivity;
import org.jraf.android.worldtour.app.service.WorldTourService;
import org.jraf.android.worldtour.model.WebcamManager;
import org.jraf.android.worldtour.provider.WebcamColumns;
import org.jraf.android.worldtour.provider.WebcamType;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {
    private static String TAG = Constants.TAG + MainActivity.class.getSimpleName();

    private static final int REQUEST_PICK_WEBCAM = 0;
    private static final int REQUEST_SETTINGS = 1;

    private static final String FRAGMENT_ASYNC_TASK = "FRAGMENT_ASYNC_TASK";

    private boolean mBroadcastReceiverRegistered;
    private boolean mLoading;
    private MenuItem mRefreshMenuItem;
    private boolean mNeedToUpdateWebcamInfo = true;

    private Switch mSwiOnOff;
    private ImageView mImgPreview;
    private View mImgPreviewFrame;
    private TextView mTxtWebcamInfoName;
    private TextView mTxtWebcamInfoLocation;

    private final Handler mHandler = new Handler();

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WorldTourService.ACTION_UPDATE_START);
        intentFilter.addAction(WorldTourService.ACTION_UPDATE_END_FAILURE);
        intentFilter.addAction(WorldTourService.ACTION_UPDATE_END_SUCCESS);
        registerReceiver(mBroadcastReceiver, intentFilter);
        mBroadcastReceiverRegistered = true;

    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER,
                Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT);
        mSwiOnOff.setOnCheckedChangeListener(null);
        mSwiOnOff.setChecked(enabled);
        mSwiOnOff.setOnCheckedChangeListener(mOnOffOnCheckedChangeListener);

        if (!mNeedToUpdateWebcamInfo) {
            mNeedToUpdateWebcamInfo = true;
            return;
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean firstRun = sharedPreferences.getBoolean(Constants.PREF_FIRST_RUN, true);
        new SimpleAsyncTask() {
            @Override
            protected void background() throws Exception {
                if (firstRun) {
                    handleFirstRun();
                    sharedPreferences.edit().putBoolean(Constants.PREF_FIRST_RUN, false).commit();
                }
            }

            @Override
            protected void postExecute(boolean ok) {
                updateWebcamRandom();
                updateWebcamName();
                updateWebcamImage();
            }
        }.execute();
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
                startService(new Intent(this, WorldTourService.class));
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

    @Blocking
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
                startActivityForResult(new Intent(this, PickWebcamActivity.class), REQUEST_PICK_WEBCAM);
                return true;

            case R.id.menu_settings:
                startActivityForResult(new Intent(this, PreferenceActivity.class), REQUEST_SETTINGS);
                return true;

            case R.id.menu_refresh:
                if (mLoading) return true;
                startService(new Intent(this, WorldTourService.class));
                return true;

            case R.id.menu_save:
                saveCurrentImage();
                return true;

            case R.id.menu_share:
                shareCurrentImage();
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
            if (WorldTourService.ACTION_UPDATE_START.equals(action)) {
                updateWebcamName();
                setLoading(true);
            } else if (WorldTourService.ACTION_UPDATE_END_SUCCESS.equals(action)) {
                setLoading(false);
                updateWebcamImage();
                if (PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER,
                        Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT)) {
                    Toast.makeText(MainActivity.this, R.string.main_toast_wallpaperUpdated, Toast.LENGTH_SHORT).show();
                }
            } else if (WorldTourService.ACTION_UPDATE_END_FAILURE.equals(action)) {
                setLoading(false);
                // TODO
            }
        }
    };


    /*
     * Switch on/off.
     */

    private final OnCheckedChangeListener mOnOffOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
            new SimpleAsyncTask() {
                @Override
                protected void background() throws Exception {
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER, isChecked)
                            .commit();
                }

                @Override
                protected void postExecute(boolean ok) {
                    setAlarm(isChecked);
                }
            }.execute();
        }
    };

    private void setAlarm(boolean enabled) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = WorldTourService.getAlarmPendingIntent(MainActivity.this);
        if (enabled) {
            long interval = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Constants.PREF_UPDATE_INTERVAL,
                    Constants.PREF_UPDATE_INTERVAL_DEFAULT));

            // Set the alarm
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, pendingIntent);

            // Update the wallpaper now
            startService(new Intent(MainActivity.this, WorldTourService.class));
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }


    /*
     * Preview img.
     */

    private final OnClickListener mImgPreviewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(MainActivity.this, PickWebcamActivity.class), REQUEST_PICK_WEBCAM);
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
        new SimpleAsyncTask() {
            private String mName;
            private String mLocation;
            private String mTimeZone;
            private String mPublicId;
            private int mType;

            @Override
            protected void background() throws Exception {
                String[] projection = { WebcamColumns.NAME, WebcamColumns.LOCATION, WebcamColumns.TIMEZONE, WebcamColumns.PUBLIC_ID, WebcamColumns.TYPE, };
                Uri webcamUri = ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, currentWebcamId);
                Cursor cursor = getContentResolver().query(webcamUri, projection, null, null, null);
                try {
                    if (cursor == null || !cursor.moveToFirst()) {
                        throw new Exception("Could not find webcam with id=" + currentWebcamId);
                    }
                    mName = cursor.getString(0);
                    mLocation = cursor.getString(1);
                    mTimeZone = cursor.getString(2);
                    mPublicId = cursor.getString(3);
                    mType = cursor.getInt(4);
                } finally {
                    if (cursor != null) cursor.close();
                }
            }

            @Override
            protected void postExecute(boolean ok) {
                if (!ok) return;
                mTxtWebcamInfoName.setText(mName);
                if (mType == WebcamType.USER) {
                    mTxtWebcamInfoLocation.setText(R.string.common_userDefined);
                } else {
                    String location = mLocation;
                    boolean specialCam = Constants.SPECIAL_CAMS.contains(mPublicId);
                    if (!specialCam) {
                        location += " - " + DateTimeUtil.getCurrentTimeForTimezone(MainActivity.this, mTimeZone);
                    }
                    mTxtWebcamInfoLocation.setText(location);
                }
            }
        }.execute();
    }

    private void updateWebcamImage() {
        if (!new File(getFilesDir(), Constants.FILE_IMAGE).exists()) {
            // The service has never been started, there is no file yet: start it now
            startService(new Intent(this, WorldTourService.class));
            mImgPreviewFrame.setVisibility(View.INVISIBLE);
            return;
        }
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                InputStream inputStream = null;
                try {
                    inputStream = openFileInput(Constants.FILE_IMAGE);
                    return BitmapFactory.decodeStream(inputStream);
                } catch (IOException e) {
                    Log.w(TAG, "Could not open image file", e);
                    return null;
                } finally {
                    IoUtil.close(inputStream);
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result == null) return;
                mImgPreview.setImageBitmap(result);
                mImgPreviewFrame.setVisibility(View.VISIBLE);
            }
        }.execute();
    }


    /*
     * Loading.
     */

    private void setLoading(boolean loading) {
        mLoading = loading;
        if (loading) {
            mRefreshMenuItem.setActionView(R.layout.main_refreshing);
        } else {
            mRefreshMenuItem.setActionView(null);
        }
    }


    /*
     * Save current image.
     */

    private void saveCurrentImage() {
        if (Config.LOGD) Log.d(TAG, "saveCurrentImage");
        if (!EnvironmentUtil.isSdCardMountedReadWrite()) {
            getSupportFragmentManager().beginTransaction()
                    .add(AlertDialogFragment.newInstance(0, 0, R.string.main_dialog_noSdCard, 0, android.R.string.ok, 0, null), Constants.FRAGMENT_DIALOG)
                    .commit();
            return;
        }

        getSupportFragmentManager().beginTransaction().add(new SimpleAsyncTaskFragment() {
            private boolean mTaskFinished;

            @Override
            protected void onPreExecute() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTaskFinished) {
                            ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
                            progressDialogFragment.show(getFragmentManager(), Constants.FRAGMENT_DIALOG);
                        }
                    }
                }, 500);
            }

            @Override
            protected void background() throws Exception {
                saveAndInsertImage();
            }

            @Override
            protected void postExecute(boolean ok) {
                mTaskFinished = true;
                DialogFragment dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag(Constants.FRAGMENT_DIALOG);
                if (dialogFragment != null) dialogFragment.dismissAllowingStateLoss();
                if (!ok) {
                    Toast.makeText(MainActivity.this, R.string.common_toast_unexpectedError, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, R.string.main_toast_fileSaved, Toast.LENGTH_SHORT).show();
            }
        }, FRAGMENT_ASYNC_TASK).commit();
    }


    protected String getFileName(WebcamInfo currentWebcamInfo) {
        boolean specialCam = Constants.SPECIAL_CAMS.contains(currentWebcamInfo.publicId);
        String location = currentWebcamInfo.location;
        if (!specialCam) {
            location += " - " + DateTimeUtil.getCurrentTimeForTimezone(this, currentWebcamInfo.timeZone);
        } else {
            location += " - " + DateTimeUtil.formatTime(this, new Date());
        }

        String res = DateTimeUtil.formatDate(new Date(), "yyyy-MM-dd") + " - ";
        res += currentWebcamInfo.name + " - ";
        res += location;
        res += ".jpg";

        res = FileUtil.stripBadCharsForFileName(res, "_");
        return res;
    }

    private class WebcamInfo {
        public String name;
        public String location;
        public String timeZone;
        public String publicId;
        public String uriStr;

        public String getShareText() {
            String date = DateTimeUtil.formatDate(MainActivity.this, new Date()) + ", ";
            boolean specialCam = Constants.SPECIAL_CAMS.contains(publicId);
            if (!specialCam) {
                date += DateTimeUtil.getCurrentTimeForTimezone(MainActivity.this, timeZone);
            } else {
                date += DateTimeUtil.formatTime(MainActivity.this, new Date());
            }
            return getString(R.string.main_shareText, name, location, date);
        }
    }

    private WebcamInfo getCurrentWebcamInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final long currentWebcamId = preferences.getLong(Constants.PREF_CURRENT_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
        String[] projection = { WebcamColumns.NAME, WebcamColumns.LOCATION, WebcamColumns.TIMEZONE, WebcamColumns.PUBLIC_ID };
        Uri webcamUri = ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, currentWebcamId);
        Cursor cursor = getContentResolver().query(webcamUri, projection, null, null, null);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e(TAG, "Could not find webcam with id=" + currentWebcamId);
                return null;
            }
            WebcamInfo res = new WebcamInfo();
            res.name = cursor.getString(0);
            res.location = cursor.getString(1);
            res.timeZone = cursor.getString(2);
            res.publicId = cursor.getString(3);
            return res;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private WebcamInfo saveAndInsertImage() throws Exception {
        // 2.1 equivalent of File path = new File(Environment.getExternalStoragePublicDirectory(), Environment.DIRECTORY_PICTURES);
        File picturesPath = new File(Environment.getExternalStorageDirectory(), "Pictures");
        File path = new File(picturesPath, "WorldTour");
        WebcamInfo currentWebcamInfo = getCurrentWebcamInfo();
        if (currentWebcamInfo == null) {
            throw new Exception("Could not get current webcam info");
        }
        String fileName = getFileName(currentWebcamInfo);
        File file = new File(path, fileName);
        path.mkdirs();
        InputStream inputStream = openFileInput(Constants.FILE_IMAGE);
        OutputStream outputStream = new FileOutputStream(file);
        try {
            IoUtil.copy(inputStream, outputStream);
        } finally {
            IoUtil.close(inputStream, outputStream);
        }

        String uriStr = MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), currentWebcamInfo.name,
                currentWebcamInfo.getShareText());
        currentWebcamInfo.uriStr = uriStr;

        // Tell the media scanner about the new file so that it is immediately available to the user.
        MediaScannerUtil.scanFile(MainActivity.this, new String[] { file.toString() }, null, null);

        return currentWebcamInfo;
    }


    /*
     * Share current image.
     */

    private void shareCurrentImage() {
        if (Config.LOGD) Log.d(TAG, "shareCurrentImage");
        if (!EnvironmentUtil.isSdCardMountedReadWrite()) {
            getSupportFragmentManager().beginTransaction()
                    .add(AlertDialogFragment.newInstance(0, 0, R.string.main_dialog_noSdCard, 0, android.R.string.ok, 0, null), Constants.FRAGMENT_DIALOG)
                    .commit();
            return;
        }
        new SimpleAsyncTask() {
            private WebcamInfo mWebcamInfo;

            @Override
            protected void background() throws Exception {
                mWebcamInfo = saveAndInsertImage();
            }

            @Override
            protected void postExecute(boolean ok) {
                if (!ok) {
                    Toast.makeText(MainActivity.this, R.string.common_toast_unexpectedError, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, mWebcamInfo.name);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mWebcamInfo.getShareText());
                shareIntent.putExtra("sms_body", mWebcamInfo.name);
                Uri uri = Uri.parse(mWebcamInfo.uriStr);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(/*Intent.createChooser(*/shareIntent/*, null)*/);
            }
        }.execute();
    }
}