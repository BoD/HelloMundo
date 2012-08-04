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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.ui.LoadingImageView;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.provider.WebcamColumns;

public class PickWebcamListFragment extends ListFragment implements LoaderCallbacks<Cursor>, WebcamCallbacks {
    private static final String TAG = Constants.TAG + PickWebcamListFragment.class.getSimpleName();

    private WebcamAdapter mAdapter;
    private boolean mHasAnimated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new WebcamAdapter(getActivity(), this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = super.onCreateView(inflater, container, savedInstanceState);

        // Add 'random' item
        ListView listView = (ListView) res.findViewById(android.R.id.list);
        setListAdapter(mAdapter);
        listView.addHeaderView(getHeaderView(listView), null, true);

        // Disable dividers since they are handled manually in cell layouts
        listView.setDividerHeight(0);

        // Disable this optimization because this cause problems on dividers in hdpi
        listView.setScrollingCacheEnabled(false);

        // Layout animation
        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.listview_layout));
        return res;
    }

    private View getHeaderView(ListView listView) {
        View res = getActivity().getLayoutInflater().inflate(R.layout.cell_webcam, listView, false);
        LoadingImageView imgThumbnail = (LoadingImageView) res.findViewById(R.id.imgThumbnail);
        imgThumbnail.setImageResource(R.drawable.ic_random_thumbnail);
        imgThumbnail.setLoadingBackground(0);
        imgThumbnail.setScaleType(ScaleType.FIT_CENTER);
        res.findViewById(R.id.btnExtend).setVisibility(View.GONE);
        ((TextView) res.findViewById(R.id.txtName)).setText(R.string.pickWebcam_random);
        ((TextView) res.findViewById(R.id.txtLocationAndTime)).setText(R.string.pickWebcam_subtitle);
        return res;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (Config.LOGD) Log.d(TAG, "onListItemClick position=" + position + " id=" + id);
        String publicId;
        if (position == 0) {
            // Random webcam
            publicId = Constants.WEBCAM_PUBLIC_ID_RANDOM;
        } else {
            Cursor cursor = mAdapter.getCursor();
            cursor.moveToPosition(position - 1);
            publicId = cursor.getString(6);
        }
        if (Config.LOGD) Log.d(TAG, "onListItemClick publicId=" + publicId);
        getActivity().setResult(Activity.RESULT_OK, new Intent().setData(new Uri.Builder().authority(publicId).build()));
        getActivity().finish();
    }


    /*
     * LoaderCallbacks implementation.
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { WebcamColumns._ID, // 0
                WebcamColumns.NAME, // 1
                WebcamColumns.THUMB_URL, // 2
                WebcamColumns.LOCATION, // 3
                WebcamColumns.SOURCE_URL, // 4
                WebcamColumns.EXCLUDE_RANDOM, // 5
                WebcamColumns.PUBLIC_ID, // 6
                WebcamColumns.TIMEZONE, // 7
                WebcamColumns.COORDINATES, // 8
        };
        return new CursorLoader(getActivity(), WebcamColumns.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (!mHasAnimated) {
            mHasAnimated = true;
            getListView().startLayoutAnimation();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    /*
     * Exclude from random
     */

    @Override
    public void setExcludedFromRandom(final long id, final boolean excluded) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ContentValues values = new ContentValues(1);
                values.put(WebcamColumns.EXCLUDE_RANDOM, excluded);
                getActivity().getContentResolver().update(ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, id), values, null, null);
                return null;
            }
        }.execute();
    }

    @Override
    public void showSource(String sourceUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + sourceUrl));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        getActivity().startActivity(intent);
    }

    @Override
    public void showOnMap(String coordinates, String label) {
        // Inverse longitude for geo url
        /*        if (coordinates.contains(",-")) {
                    coordinates = coordinates.replace(",-", ",");
                } else {
                    coordinates = coordinates.replace(",", ",-");
                }*/
        String uri;
        try {
            uri = "geo:" + coordinates + "?z=5&q=" + coordinates + "(" + URLEncoder.encode(label, "utf-8") + ")";
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e); // Should never happen
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        getActivity().startActivity(intent);
    }
}
