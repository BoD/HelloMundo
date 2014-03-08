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
package org.jraf.android.hellomundo.app.saveshare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.model.AppwidgetManager;
import org.jraf.android.hellomundo.model.WebcamInfo;
import org.jraf.android.hellomundo.provider.webcam.WebcamColumns;
import org.jraf.android.hellomundo.provider.webcam.WebcamCursor;
import org.jraf.android.hellomundo.provider.webcam.WebcamSelection;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.annotation.Background.Type;
import org.jraf.android.util.async.Task;
import org.jraf.android.util.async.TaskFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.environment.EnvironmentUtil;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.wrapper.Log;
import org.jraf.android.util.mediascanner.MediaScannerUtil;
import org.jraf.android.util.mediascanner.MediaScannerUtil.OnScanCompletedListener;

public class SaveShareHelper {
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

    public void saveImage(FragmentManager fragmentManager, final int appwidgetId) {
        Log.d("appwidgetId=" + appwidgetId);
        if (!EnvironmentUtil.isExternalStorageMountedReadWrite()) {
            AlertDialogFragment.newInstance(0, 0, R.string.main_dialog_noSdCard, 0, android.R.string.ok, 0, (Serializable) null).show(fragmentManager);
            return;
        }

        new TaskFragment(new Task<FragmentActivity>() {
            @Override
            protected void doInBackground() throws Exception {
                saveAndInsertImage(getActivity(), appwidgetId);
            }

            @Override
            protected void onPostExecuteOk() {
                super.onPostExecuteOk();
                if (getActivity() instanceof SaveShareListener) {
                    ((SaveShareListener) getActivity()).onDone();
                }
            }
        }.toastOk(R.string.main_toast_fileSaved).toastFail(R.string.common_toast_unexpectedError)).execute(fragmentManager);
    }


    public void shareImage(FragmentManager fragmentManager, final int appwidgetId) {
        Log.d("appwidgetId=" + appwidgetId);
        if (!EnvironmentUtil.isExternalStorageMountedReadWrite()) {
            AlertDialogFragment.newInstance(0, 0, R.string.main_dialog_noSdCard, 0, android.R.string.ok, 0, (Serializable) null).show(fragmentManager);
            return;
        }
        new TaskFragment(new Task<FragmentActivity>() {
            private WebcamInfo mWebcamInfo;

            @Override
            protected void doInBackground() throws Exception {
                mWebcamInfo = saveAndInsertImage(getActivity(), appwidgetId);
            }

            @Override
            protected void onPostExecuteOk() {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/jpeg");

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, mWebcamInfo.name);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mWebcamInfo.getShareText(getActivity()));
                shareIntent.putExtra("sms_body", mWebcamInfo.name);
                Uri uri = Uri.parse(mWebcamInfo.localBitmapUriStr);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                getActivity().startActivity(Intent.createChooser(shareIntent, getActivity().getText(R.string.common_shareWith)));

                if (getActivity() instanceof SaveShareListener) {
                    ((SaveShareListener) getActivity()).onDone();
                }
            }
        }.toastFail(R.string.common_toast_unexpectedError)).execute(fragmentManager);
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
        File path = new File(picturesPath, "HelloMundo");
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
                Log.d("path=" + p + " uri=" + uri);
                scannedImageUri.set(uri);
            }
        });

        // Wait until the media scanner has found our file
        long start = System.currentTimeMillis();
        while (scannedImageUri.get() == null) {
            Log.d("Waiting 250ms for media scanner...");
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
        WebcamSelection where = new WebcamSelection().id(webcamId);
        WebcamCursor cursor = where.query(context.getContentResolver(), projection);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Log.w("Could not find webcam with id=" + webcamId);
                return null;
            }
            WebcamInfo res = new WebcamInfo();
            res.name = cursor.getName();
            res.location = cursor.getLocation();
            res.timeZone = cursor.getTimezone();
            res.publicId = cursor.getPublicId();
            res.type = cursor.getType();
            return res;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
