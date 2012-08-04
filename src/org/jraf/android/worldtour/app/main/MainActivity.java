package org.jraf.android.worldtour.app.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jraf.android.backport.switchwidget.Switch;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.DateTimeUtil;
import org.jraf.android.util.IoUtil;
import org.jraf.android.util.SimpleAsyncTask;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.pickwebcam.PickWebcamActivity;
import org.jraf.android.worldtour.app.preference.PreferenceActivity;
import org.jraf.android.worldtour.app.service.WorldTourService;
import org.jraf.android.worldtour.provider.WebcamColumns;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockActivity {
    private static String TAG = Constants.TAG + MainActivity.class.getSimpleName();

    private static final int REQUEST_PICK_WEBCAM = 0;

    private boolean mBroadcastReceiverRegistered;
    private boolean mLoading;
    private MenuItem mRefreshMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Switch swiOnOff = (Switch) findViewById(R.id.swiOnOff);
        boolean enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREF_AUTO_UPDATE_WALLPAPER,
                Constants.PREF_AUTO_UPDATE_WALLPAPER_DEFAULT);
        swiOnOff.setChecked(enabled);
        swiOnOff.setOnCheckedChangeListener(mOnOffOnCheckedChangeListener);
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
        updateWebcamRandom();
        updateWebcamName();
        updateWebcamImage();
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
        switch (requestCode) {
            case REQUEST_PICK_WEBCAM:
                String publicId = data.getData().getAuthority();
                if (Config.LOGD) Log.d(TAG, "onActivityResult publicId=" + publicId);
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.PREF_SELECTED_WEBCAM_PUBLIC_ID, publicId).commit();
                updateWebcamRandom();
                startService(new Intent(this, WorldTourService.class));
                break;
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pick:
                startActivityForResult(new Intent(this, PickWebcamActivity.class), REQUEST_PICK_WEBCAM);
                return true;

            case R.id.menu_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;

            case R.id.menu_refresh:
                if (mLoading) return true;
                startService(new Intent(this, WorldTourService.class));
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
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    PendingIntent pendingIntent = WorldTourService.getServicePendingIntent(MainActivity.this);
                    if (isChecked) {
                        long interval = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(Constants.PREF_UPDATE_INTERVAL,
                                Constants.PREF_UPDATE_INTERVAL_DEFAULT));
                        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, pendingIntent);
                        startService(new Intent(MainActivity.this, WorldTourService.class));
                    } else {
                        alarmManager.cancel(pendingIntent);
                    }
                }
            }.execute();
        }
    };


    /*
     * Webcam info.
     */

    private void updateWebcamRandom() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedPublicId = preferences.getString(Constants.PREF_SELECTED_WEBCAM_PUBLIC_ID, Constants.PREF_SELECTED_WEBCAM_PUBLIC_ID_DEFAULT);
        if (Constants.WEBCAM_PUBLIC_ID_RANDOM.equals(selectedPublicId)) {
            findViewById(R.id.imgWebcamInfo_random).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.imgWebcamInfo_random).setVisibility(View.GONE);
        }
    }

    private void updateWebcamName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String currentPublicId = preferences.getString(Constants.PREF_CURRENT_WEBCAM_PUBLIC_ID, Constants.PREF_SELECTED_WEBCAM_PUBLIC_ID_DEFAULT);
        new SimpleAsyncTask() {
            private String mName;
            private String mLocation;
            private String mTimeZone;

            @Override
            protected void background() throws Exception {
                String[] projection = { WebcamColumns.NAME, WebcamColumns.LOCATION, WebcamColumns.TIMEZONE };
                String selection = WebcamColumns.PUBLIC_ID + "=?";
                String[] selectionArgs = { currentPublicId };
                Cursor cursor = getContentResolver().query(WebcamColumns.CONTENT_URI, projection, selection, selectionArgs, null);
                try {
                    if (cursor == null || !cursor.moveToFirst()) {
                        throw new Exception("Could not find webcam with webcamId=" + currentPublicId);
                    }
                    mName = cursor.getString(0);
                    mLocation = cursor.getString(1);
                    mTimeZone = cursor.getString(2);
                } finally {
                    if (cursor != null) cursor.close();
                }
            }

            @Override
            protected void postExecute(boolean ok) {
                if (!ok) return;
                ((TextView) findViewById(R.id.txtWebcamInfo_name)).setText(mName);
                String location = mLocation;
                boolean specialCam = Constants.SPECIAL_CAMS.contains(currentPublicId);
                if (!specialCam) {
                    location += " - " + DateTimeUtil.getCurrentTimeForTimezone(MainActivity.this, mTimeZone);
                }
                ((TextView) findViewById(R.id.txtWebcamInfo_name)).setText(mName);
                ((TextView) findViewById(R.id.txtWebcamInfo_location)).setText(location);
            }
        }.execute();
    }

    private void updateWebcamImage() {
        if (!new File(getFilesDir(), Constants.FILE_IMAGE).exists()) {
            // The service has never been started, there is no file yet: start it now
            startService(new Intent(this, WorldTourService.class));
            findViewById(R.id.imgPreview).setVisibility(View.INVISIBLE);
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
                ImageView imgPreview = (ImageView) findViewById(R.id.imgPreview);
                imgPreview.setImageBitmap(result);
                imgPreview.setVisibility(View.VISIBLE);
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
}