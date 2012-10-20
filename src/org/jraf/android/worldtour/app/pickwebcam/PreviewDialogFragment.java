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

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.HttpUtil;
import org.jraf.android.util.HttpUtil.Options;
import org.jraf.android.util.IoUtil;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.provider.WebcamColumns;

public class PreviewDialogFragment extends DialogFragment {
    private static final String TAG = Constants.TAG + PreviewDialogFragment.class.getSimpleName();

    private long mWebcamId;

    private View mPgbLoading;
    private ImageView mImgPreview;
    private View mImgPreviewFrame;

    protected Handler mHandler = new Handler();

    public PreviewDialogFragment() {}

    public static PreviewDialogFragment newInstance(Long webcamId) {
        PreviewDialogFragment res = new PreviewDialogFragment();
        Bundle args = new Bundle(1);
        args.putLong("webcamId", webcamId);
        res.setArguments(args);
        return res;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_Preview);
        mWebcamId = getArguments().getLong("webcamId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        View view = inflater.inflate(R.layout.preview, container, false);
        mPgbLoading = view.findViewById(R.id.pgbLoading);
        mImgPreview = (ImageView) view.findViewById(R.id.imgPreview);
        mImgPreviewFrame = view.findViewById(R.id.imgPreviewFrame);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                String[] projection = { WebcamColumns.URL, WebcamColumns.HTTP_REFERER };
                Uri uri = ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, mWebcamId);
                Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
                String url = null;
                String httpReferer = null;
                try {
                    if (cursor == null || !cursor.moveToFirst()) {
                        Log.w(TAG, "onHandleIntent Could not find webcam with webcamId=" + mWebcamId);
                        return null;
                    }
                    url = cursor.getString(0);
                    httpReferer = cursor.getString(1);
                } finally {
                    if (cursor != null) cursor.close();
                }
                Options options = new Options();
                options.referer = httpReferer;
                InputStream inputStream;
                try {
                    inputStream = HttpUtil.getAsStream(url);
                } catch (IOException e) {
                    Log.w(TAG, "onHandleIntent Could not download webcam with webcamId=" + mWebcamId, e);
                    return null;
                }

                try {
                    return BitmapFactory.decodeStream(inputStream);
                } finally {
                    IoUtil.close(inputStream);
                }
            }

            @Override
            protected void onPostExecute(final Bitmap result) {
                if (!isAdded()) return;
                if (result == null) {
                    Toast.makeText(getActivity(), R.string.pickWebcam_previewDialog_cantPreviewToast, Toast.LENGTH_SHORT).show();
                    dismissAllowingStateLoss();
                    return;
                }
                mPgbLoading.setVisibility(View.GONE);

                // I'm not sure exactly why, but waiting a bit before showing the picture fixes a visual glitch
                // with the progressbar moving to the top of the dialog for a fraction of a second before
                // disappearing.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mImgPreview.setImageBitmap(result);
                        mImgPreviewFrame.setVisibility(View.VISIBLE);
                    }
                }, 10);
            }
        }.execute();
    }
}
