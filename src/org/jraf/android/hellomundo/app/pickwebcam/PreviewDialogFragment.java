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

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.jraf.android.hellomundo.provider.webcam.WebcamColumns;
import org.jraf.android.hellomundo.provider.webcam.WebcamCursor;
import org.jraf.android.hellomundo.provider.webcam.WebcamSelection;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.http.HttpUtil;
import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.wrapper.Log;

import com.github.kevinsawicki.http.HttpRequest;

public class PreviewDialogFragment extends DialogFragment {
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
                if (!isAdded()) return null;
                String[] projection = { WebcamColumns.URL, WebcamColumns.HTTP_REFERER };
                WebcamSelection where = new WebcamSelection().id(mWebcamId);
                WebcamCursor cursor = where.query(getActivity().getContentResolver(), projection);
                String url = null;
                String httpReferer = null;
                try {
                    if (cursor == null || !cursor.moveToFirst()) {
                        Log.d("Could not find webcam with webcamId=" + mWebcamId);
                        return null;
                    }
                    url = cursor.getUrl();
                    httpReferer = cursor.getHttpReferer();
                } finally {
                    if (cursor != null) cursor.close();
                }

                InputStream inputStream;
                try {
                    HttpRequest httpRequest = HttpUtil.get(url);
                    if (httpReferer != null) httpRequest.referer(httpReferer);
                    inputStream = httpRequest.stream();
                } catch (IOException e) {
                    Log.w("Could not download webcam with webcamId=" + mWebcamId, e);
                    return null;
                }

                try {
                    return BitmapFactory.decodeStream(inputStream);
                } catch (Throwable t) {
                    Log.w("Could not decode stream", t);
                    return null;
                } finally {
                    IoUtil.closeSilently(inputStream);
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
