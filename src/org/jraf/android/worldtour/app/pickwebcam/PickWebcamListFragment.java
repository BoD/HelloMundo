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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.ui.LoadingImageView;
import org.jraf.android.worldtour.provider.WebcamColumns;

public class PickWebcamListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private WebcamAdapter mAdapter;

    private boolean mHasAnimated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new WebcamAdapter(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View res = super.onCreateView(inflater, container, savedInstanceState);

        // Add 'random' item
        final ListView listView = (ListView) res.findViewById(android.R.id.list);
        setListAdapter(mAdapter);
        listView.addHeaderView(getHeaderView(listView), null, true);

        listView.setDividerHeight(0);

        // Layout animation
        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.listview_layout));
        return res;
    }

    private View getHeaderView(ListView listView) {
        final View res = getActivity().getLayoutInflater().inflate(R.layout.cell_webcam, listView, false);
        final LoadingImageView imgThumbnail = (LoadingImageView) res.findViewById(R.id.imgThumbnail);
        imgThumbnail.setImageResource(R.drawable.ic_random_thumbnail);
        imgThumbnail.setLoadingBackground(0);
        imgThumbnail.setImageViewScaleType(ScaleType.FIT_CENTER);
        res.findViewById(R.id.btnExtend).setVisibility(View.GONE);
        ((TextView) res.findViewById(R.id.txtName)).setText(R.string.pickWebcam_random);
        ((TextView) res.findViewById(R.id.txtLocationAndTime)).setText(R.string.pickWebcam_subtitle);
        return res;
    }


    /*
     * LoaderCallbacks implementation.
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = { WebcamColumns._ID, // 0
                WebcamColumns.NAME, // 1
                WebcamColumns.THUMB_URL, // 2
                WebcamColumns.LOCATION, // 3
                WebcamColumns.SOURCE_URL, // 4
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
}
