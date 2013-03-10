/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright 2013 Benoit 'BoD' Lubek (BoD@JRAF.org).  All Rights Reserved.
 */
package org.jraf.android.worldtour.app.saveshare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.Blocking;
import org.jraf.android.util.Blocking.Type;
import org.jraf.android.util.EnvironmentUtil;
import org.jraf.android.util.IoUtil;
import org.jraf.android.util.MediaScannerUtil;
import org.jraf.android.util.MediaScannerUtil.OnScanCompletedListener;
import org.jraf.android.util.SimpleAsyncTask;
import org.jraf.android.util.SimpleAsyncTaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.app.main.ProgressDialogFragment;
import org.jraf.android.worldtour.model.AppwidgetManager;
import org.jraf.android.worldtour.model.WebcamInfo;
import org.jraf.android.worldtour.provider.WebcamColumns;

public class SaveShareHelper {
    private static final String TAG = Constants.TAG + SaveShareHelper.class.getSimpleName();

    public static final int WALLPAPER = -1;

    private static final String FRAGMENT_ASYNC_TASK = "FRAGMENT_ASYNC_TASK";
    private static final SaveShareHelper INSTANCE = new SaveShareHelper();


    public static SaveShareHelper get() {
        return INSTANCE;
    }

    private SaveShareHelper() {}

    @Blocking(Type.DISK)
    private long getCurrentWallpaperWebcamId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final long currentWebcamId = preferences.getLong(Constants.PREF_CURRENT_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
        return currentWebcamId;
    }

    public void saveImage(final Context context, FragmentManager fragmentManager, final int appwidgetId) {
        if (Config.LOGD) Log.d(TAG, "saveImage appwidgetId=" + appwidgetId);
        if (!EnvironmentUtil.isSdCardMountedReadWrite()) {
            fragmentManager.beginTransaction()
                    .add(AlertDialogFragment.newInstance(0, 0, R.string.main_dialog_noSdCard, 0, android.R.string.ok, 0, null), Constants.FRAGMENT_DIALOG)
                    .commit();
            return;
        }

        fragmentManager.beginTransaction().add(new SimpleAsyncTaskFragment() {
            private boolean mTaskFinished;

            @Override
            protected void onPreExecute() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
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
                saveAndInsertImage(context, appwidgetId);
            }

            @Override
            protected void postExecute(boolean ok) {
                mTaskFinished = true;
                DialogFragment dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag(Constants.FRAGMENT_DIALOG);
                if (dialogFragment != null) dialogFragment.dismissAllowingStateLoss();
                if (!ok) {
                    Toast.makeText(context, R.string.common_toast_unexpectedError, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(context, R.string.main_toast_fileSaved, Toast.LENGTH_SHORT).show();

                if (getActivity() instanceof SaveShareListener) {
                    ((SaveShareListener) getActivity()).onDone();
                }
            }
        }, FRAGMENT_ASYNC_TASK).commit();
    }


    public void shareImage(final Context context, FragmentManager fragmentManager, final int appwidgetId) {
        if (Config.LOGD) Log.d(TAG, "shareImage appwidgetId=" + appwidgetId);
        if (!EnvironmentUtil.isSdCardMountedReadWrite()) {
            fragmentManager.beginTransaction()
                    .add(AlertDialogFragment.newInstance(0, 0, R.string.main_dialog_noSdCard, 0, android.R.string.ok, 0, null), Constants.FRAGMENT_DIALOG)
                    .commit();
            return;
        }
        new SimpleAsyncTask() {
            private WebcamInfo mWebcamInfo;

            @Override
            protected void background() throws Exception {
                mWebcamInfo = saveAndInsertImage(context, appwidgetId);
            }

            @Override
            protected void postExecute(boolean ok) {
                if (!ok) {
                    Toast.makeText(context, R.string.common_toast_unexpectedError, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, mWebcamInfo.name);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mWebcamInfo.getShareText(context));
                shareIntent.putExtra("sms_body", mWebcamInfo.name);
                Uri uri = Uri.parse(mWebcamInfo.localBitmapUriStr);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                context.startActivity(Intent.createChooser(shareIntent, context.getText(R.string.common_shareWith)));

                if (context instanceof SaveShareListener) {
                    ((SaveShareListener) context).onDone();
                }
            }
        }.execute();
    }

    @Blocking(Type.DISK)
    private WebcamInfo saveAndInsertImage(Context context, int appwidgetId) throws Exception {
        long webcamId;
        if (appwidgetId == WALLPAPER) {
            webcamId = getCurrentWallpaperWebcamId(context);
        } else {
            webcamId = AppwidgetManager.get().getCurrentWebcamId(context, appwidgetId);
            if (webcamId == Constants.WEBCAM_ID_NONE) throw new Exception("Could not get webcamId for appwidgetId=" + appwidgetId);
        }

        // 2.1 equivalent of File path = new File(Environment.getExternalStoragePublicDirectory(), Environment.DIRECTORY_PICTURES);
        File picturesPath = new File(Environment.getExternalStorageDirectory(), "Pictures");
        File path = new File(picturesPath, "WorldTour");
        WebcamInfo webcamInfo = getWebcamInfo(context, webcamId);
        if (webcamInfo == null) {
            throw new Exception("Could not get webcam info");
        }
        String fileName = webcamInfo.getFileName(context);
        File file = new File(path, fileName);
        path.mkdirs();
        String imageFile;
        if (appwidgetId == WALLPAPER) {
            imageFile = Constants.FILE_IMAGE_WALLPAPER;
        } else {
            imageFile = AppwidgetManager.get().getFileName(appwidgetId);
        }
        InputStream inputStream = context.openFileInput(imageFile);
        OutputStream outputStream = new FileOutputStream(file);
        try {
            IoUtil.copy(inputStream, outputStream);
        } finally {
            IoUtil.close(inputStream, outputStream);
        }

        // Scan it
        final AtomicReference<Uri> scannedImageUri = new AtomicReference<Uri>();
        MediaScannerUtil.scanFile(context, new String[] { file.getPath() }, null, new OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String p, Uri uri) {
                if (Config.LOGD) Log.d(TAG, "onScanCompleted path=" + p + " uri=" + uri);
                scannedImageUri.set(uri);
            }
        });

        // Wait until the media scanner has found our file
        long start = System.currentTimeMillis();
        while (scannedImageUri.get() == null) {
            if (Config.LOGD) Log.d(TAG, "saveAndInsertImage Waiting 250ms for media scanner...");
            SystemClock.sleep(250);
            if (System.currentTimeMillis() - start > 5000) {
                throw new Exception("MediaScanner did not scan the file " + file + " after 5000ms");
            }
        }
        webcamInfo.localBitmapUriStr = scannedImageUri.get().toString();
        return webcamInfo;
    }

    @Blocking(Type.DISK)
    private WebcamInfo getWebcamInfo(Context context, final long webcamId) {
        String[] projection = { WebcamColumns.NAME, WebcamColumns.LOCATION, WebcamColumns.TIMEZONE, WebcamColumns.PUBLIC_ID, WebcamColumns.TYPE };
        Uri webcamUri = ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, webcamId);
        Cursor cursor = context.getContentResolver().query(webcamUri, projection, null, null, null);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e(TAG, "Could not find webcam with id=" + webcamId);
                return null;
            }
            WebcamInfo res = new WebcamInfo();
            res.name = cursor.getString(0);
            res.location = cursor.getString(1);
            res.timeZone = cursor.getString(2);
            res.publicId = cursor.getString(3);
            res.type = cursor.getInt(4);
            return res;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
