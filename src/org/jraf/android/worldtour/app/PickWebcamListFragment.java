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
package org.jraf.android.worldtour.app;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.jraf.android.worldtour.provider.WebcamColumns;

public class PickWebcamListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private WebcamAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new WebcamAdapter(getActivity());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
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
