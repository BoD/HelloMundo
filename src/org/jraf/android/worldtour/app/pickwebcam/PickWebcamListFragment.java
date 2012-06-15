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
import android.widget.ImageView;
import android.widget.ListView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.worldtour.provider.WebcamColumns;

public class PickWebcamListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private WebcamAdapter mAdapter;

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
        listView.addHeaderView(getHeaderView(listView), null, true);

        setListAdapter(mAdapter);

        return res;
    }

    private View getHeaderView(ListView listView) {
        final View res = getActivity().getLayoutInflater().inflate(R.layout.cell_webcam, listView, false);
        final ImageView imgThumbnail = (ImageView) res.findViewById(R.id.imgThumbnail);
        imgThumbnail.setImageResource(R.drawable.ic_random);
        final int padding = getResources().getDimensionPixelSize(R.dimen.cell_webcam_random_padding);
        imgThumbnail.setPadding(padding, padding, padding, padding);
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
        };
        return new CursorLoader(getActivity(), WebcamColumns.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
