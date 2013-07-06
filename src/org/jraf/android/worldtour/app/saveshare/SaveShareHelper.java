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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.annotation.Background.Type;
import org.jraf.android.util.async.ProgressDialogAsyncTaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.environment.EnvironmentUtil;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.mediascanner.MediaScannerUtil;
import org.jraf.android.util.mediascanner.MediaScannerUtil.OnScanCompletedListener;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.model.AppwidgetManager;
import org.jraf.android.worldtour.model.WebcamInfo;
import org.jraf.android.worldtour.provider.WebcamColumns;

public class SaveShareHelper {
    private static final String TAG = Constants.TAG + SaveShareHelper.class.getSimpleName();

    public static final int WALLPAPER = -1;

    private static final SaveShareHelper INSTANCE = new SaveShareHelper();


    public static SaveShareHelper get() {
        return INSTANCE;
    }

    private SaveShareHelper() {}

    @Background(Type.DISK)
    private long getCurrentWallpaperWebcamId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final long currentWebcamId = preferences.getLong(Constants.PREF_CURRENT_WEBCAM_ID, Constants.PREF_SELECTED_WEBCAM_ID_DEFAULT);
        return currentWebcamId;
    }

    public void saveImage(final Context context, FragmentManager fragmentManager, final int appwidgetId) {
        if (Config.LOGD) Log.d(TAG, "saveImage appwidgetId=" + appwidgetId);
        if (!EnvironmentUtil.isExternalStorageMountedReadWrite()) {
            AlertDialogFragment.newInstance(0, 0, R.string.main_dialog_noSdCard, 0, android.R.string.ok, 0, null).show(fragmentManager);
            return;
        }

        new ProgressDialogAsyncTaskFragment() {
            @Override
            protected void doInBackground() throws Exception {
                saveAndInsertImage(context, appwidgetId);
            }

            @Override
            protected void onPostExecuteOk() {
                super.onPostExecuteOk();
                if (getActivity() instanceof SaveShareListener) {
                    ((SaveShareListener) getActivity()).onDone();
                }
            }
        }.toastOk(R.string.main_toast_fileSaved).toastFail(R.string.common_toast_unexpectedError).execute(fragmentManager);
    }


    public void shareImage(final Context context, FragmentManager fragmentManager, final int appwidgetId) {
        if (Config.LOGD) Log.d(TAG, "shareImage appwidgetId=" + appwidgetId);
        if (!EnvironmentUtil.isExternalStorageMountedReadWrite()) {
            AlertDialogFragment.newInstance(0, 0, R.string.main_dialog_noSdCard, 0, android.R.string.ok, 0, null).show(fragmentManager);
            return;
        }
        new ProgressDialogAsyncTaskFragment() {
            private WebcamInfo mWebcamInfo;

            @Override
            protected void doInBackground() throws Exception {
                mWebcamInfo = saveAndInsertImage(context, appwidgetId);
            }

            @Override
            protected void onPostExecuteOk() {
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
        }.toastFail(R.string.common_toast_unexpectedError).execute(fragmentManager);
    }

    @Background(Type.DISK)
    private WebcamInfo saveAndInsertImage(Context context, int appwidgetId) throws Exception {
        long webcamId;
        if (appwidgetId == WALLPAPER) {
            webcamId = getCurrentWallpaperWebcamId(context);
        } else {
            webcamId = AppwidgetManager.get().getCurrentWebcamId(context, appwidgetId);
            if (webcamId == Constants.WEBCAM_ID_NONE) throw new Exception("Could not get webcamId for appwidgetId=" + appwidgetId);
        }

        // 2.1 equivalent of File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
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
            IoUtil.closeSilently(inputStream, outputStream);
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

    @Background(Type.DISK)
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
