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
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.provider.webcam.WebcamColumns;
import org.jraf.android.worldtour.provider.webcam.WebcamCursor;

public class PickWebcamListFragment extends ListFragment implements LoaderCallbacks<Cursor>, WebcamCallbacks {
    private static final String TAG = Constants.TAG + PickWebcamListFragment.class.getSimpleName();

    private WebcamAdapter mAdapter;
    private boolean mHasAnimated;

    private long mCurrentWebcamId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getLong("mCurrentWebcamId", Constants.WEBCAM_ID_NONE) != Constants.WEBCAM_ID_NONE) {
            mCurrentWebcamId = savedInstanceState.getLong("mCurrentWebcamId", Constants.WEBCAM_ID_NONE);
        }
        mAdapter = new WebcamAdapter(getActivity(), this, mCurrentWebcamId);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = super.onCreateView(inflater, container, savedInstanceState);

        // Add 'random' item
        ListView listView = (ListView) res.findViewById(android.R.id.list);
        setListAdapter(mAdapter);
        mAdapter.setListView(listView);
        listView.addHeaderView(getHeaderView(listView), null, true);

        // Disable dividers since they are handled manually in cell layouts
        listView.setDividerHeight(0);

        // Disable this optimization because this cause problems on dividers in hdpi
        listView.setScrollingCacheEnabled(false);

        // Layout animation
        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.listview_layout));
        return res;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("mCurrentWebcamId", mCurrentWebcamId);
        super.onSaveInstanceState(outState);
    }

    private View getHeaderView(ListView listView) {
        View res = getActivity().getLayoutInflater().inflate(R.layout.pick_webcam_item, listView, false);
        ImageView imgThumbnail = (ImageView) res.findViewById(R.id.imgThumbnail);
        imgThumbnail.setImageResource(R.drawable.ic_random_thumbnail);
        imgThumbnail.setScaleType(ScaleType.FIT_CENTER);
        res.findViewById(R.id.btnExtend).setVisibility(View.GONE);
        ((TextView) res.findViewById(R.id.txtName)).setText(R.string.pickWebcam_random_title);
        ((TextView) res.findViewById(R.id.txtLocationAndTime)).setText(R.string.pickWebcam_random_subtitle);

        if (mCurrentWebcamId == Constants.WEBCAM_ID_RANDOM) {
            res.findViewById(R.id.conMainItem).setSelected(true);
        }
        return res;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (Config.LOGD) Log.d(TAG, "onListItemClick position=" + position + " id=" + id);
        long res;
        if (position == 0) {
            // Random webcam
            res = Constants.WEBCAM_ID_RANDOM;
        } else {
            res = id;
        }
        if (Config.LOGD) Log.d(TAG, "onListItemClick res=" + res);
        getActivity().setResult(Activity.RESULT_OK, new Intent().setData(ContentUris.withAppendedId(WebcamColumns.CONTENT_URI, res)));
        getActivity().finish();
    }


    /*
     * LoaderCallbacks implementation.
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), WebcamColumns.CONTENT_URI, null, null, null, null) {
            @Override
            public Cursor loadInBackground() {
                Cursor c = super.loadInBackground();
                return new WebcamCursor(c);
            }
        };
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
     * WebcamCallbacks implementation.
     */

    @Override
    public void setExcludedFromRandom(final long id, final boolean excluded) {
        if (Config.LOGD) Log.d(TAG, "setExcludedFromRandom id=" + id + " excluded=" + excluded);
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
        if (Config.LOGD) Log.d(TAG, "showOnMap coordinates=" + coordinates + " label=" + label);
        String uri;
        try {
            uri = "geo:" + coordinates + "?z=5&q=" + coordinates + "(" + URLEncoder.encode(label, "utf-8") + ")";
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e); // Should never happen
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        getActivity().startActivity(Intent.createChooser(intent, null));
    }

    @Override
    public void showPreview(long id) {
        if (Config.LOGD) Log.d(TAG, "showPreview id=" + id);
        PreviewDialogFragment.newInstance(id).show(getFragmentManager(), "dialog");
    }

    @Override
    public void delete(long id) {
        if (Config.LOGD) Log.d(TAG, "delete id=" + id);
        AlertDialogFragment.newInstance(0, R.string.common_confirmation, R.string.pickWebcam_deleteConfirmDialog_message, 0, android.R.string.yes,
                android.R.string.no, id).show(getFragmentManager());

    }

    public void setCurrentWebcamId(long currentWebcamId) {
        mCurrentWebcamId = currentWebcamId;
    }
}
