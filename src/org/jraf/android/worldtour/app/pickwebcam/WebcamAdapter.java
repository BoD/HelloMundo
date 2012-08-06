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

import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.DateTimeUtil;
import org.jraf.android.util.ViewHolder;
import org.jraf.android.util.ui.ExtendHeightAnimation;
import org.jraf.android.util.ui.LoadingImageView;
import org.jraf.android.worldtour.Constants;

public class WebcamAdapter extends ResourceCursorAdapter {
    private final int mExtendedHeight;
    private final WebcamCallbacks mWebcamCallbacks;
    private final HashSet<Long> mExtendedIds = new HashSet<Long>(5);
    private final HashMap<String, String> mLocalTimeCache = new HashMap<String, String>(50);

    public WebcamAdapter(Context context, WebcamCallbacks webcamCallbacks) {
        super(context, R.layout.cell_webcam, null, false);
        mExtendedHeight = context.getResources().getDimensionPixelSize(R.dimen.cell_webcam_extended_height);
        mWebcamCallbacks = webcamCallbacks;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long id = cursor.getLong(0);

        TextView txtName = (TextView) ViewHolder.get(view, R.id.txtName);
        String name = cursor.getString(1);
        txtName.setText(name);

        View layExtended = ViewHolder.get(view, R.id.layExtended);
        layExtended.setTag(id);
        View btnExtend = ViewHolder.get(view, R.id.btnExtend);
        btnExtend.setTag(layExtended);
        btnExtend.setOnClickListener(mExtendOnClickListener);
        LayoutParams layoutParams = layExtended.getLayoutParams();
        if (mExtendedIds.contains(id)) {
            layoutParams.height = mExtendedHeight;
        } else {
            layoutParams.height = 0;
        }

        LoadingImageView imgThumbnail = (LoadingImageView) ViewHolder.get(view, R.id.imgThumbnail);
        imgThumbnail.loadBitmap(cursor.getString(2));

        TextView txtLocationAndTime = (TextView) ViewHolder.get(view, R.id.txtLocationAndTime);
        String location = cursor.getString(3);
        String publicId = cursor.getString(6);
        boolean specialCam = Constants.SPECIAL_CAMS.contains(publicId);
        if (!specialCam) {
            location += " - " + getLocalTime(context, cursor.getString(7));
        }
        txtLocationAndTime.setText(location);

        TextView txtSourceUrl = (TextView) ViewHolder.get(view, R.id.txtSourceUrl);
        String sourceUrl = cursor.getString(4);
        txtSourceUrl.setText(context.getString(R.string.pickWebcam_source, sourceUrl));
        txtSourceUrl.setTag(sourceUrl);
        txtSourceUrl.setOnClickListener(mSourceOnClickListener);

        boolean excludedFromRandom = !cursor.isNull(5) && cursor.getInt(5) == 1;
        View btnExcludeFromRandom = ViewHolder.get(view, R.id.btnExcludeFromRandom);
        btnExcludeFromRandom.setSelected(excludedFromRandom);
        txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, excludedFromRandom ? R.drawable.ic_excluded_from_random : 0, 0);
        btnExcludeFromRandom.setTag(id);
        btnExcludeFromRandom.setOnClickListener(mExcludeFromRandomOnClickListener);

        View btnShowOnMap = ViewHolder.get(view, R.id.btnShowOnMap);
        String coordinates = cursor.getString(8);
        if (coordinates == null) {
            btnShowOnMap.setEnabled(false);
        } else {
            btnShowOnMap.setEnabled(true);
            btnShowOnMap.setTag(R.id.coordinates, coordinates);
            btnShowOnMap.setTag(R.id.name, name);
            btnShowOnMap.setOnClickListener(mShowOnMapOnClickListener);
        }

        View btnPreview = ViewHolder.get(view, R.id.btnPreview);
        btnPreview.setTag(id);
        btnPreview.setOnClickListener(mPreviewOnClickListener);
    }

    private String getLocalTime(Context context, String timeZone) {
        String res = mLocalTimeCache.get(timeZone);
        if (res == null) {
            res = DateTimeUtil.getCurrentTimeForTimezone(context, timeZone);
            mLocalTimeCache.put(timeZone, res);
        }
        return res;
    }


    private final OnClickListener mExtendOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            View layExtended = (View) v.getTag();
            long id = (Long) layExtended.getTag();
            LayoutParams layoutParams = layExtended.getLayoutParams();
            if (layoutParams.height == 0) {
                ExtendHeightAnimation animation = new ExtendHeightAnimation(layExtended, mExtendedHeight, true);
                animation.setDuration(300);
                layExtended.startAnimation(animation);
                mExtendedIds.add(id);
            } else {
                ExtendHeightAnimation animation = new ExtendHeightAnimation(layExtended, mExtendedHeight, false);
                animation.setDuration(300);
                layExtended.startAnimation(animation);
                mExtendedIds.remove(id);
            }
        }
    };

    private final OnClickListener mExcludeFromRandomOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            long id = (Long) v.getTag();
            mWebcamCallbacks.setExcludedFromRandom(id, !v.isSelected());
        }
    };

    private final OnClickListener mSourceOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String sourceUrl = (String) v.getTag();
            mWebcamCallbacks.showSource(sourceUrl);
        }
    };

    private final OnClickListener mShowOnMapOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String coordinates = (String) v.getTag(R.id.coordinates);
            String label = (String) v.getTag(R.id.name);
            mWebcamCallbacks.showOnMap(coordinates, label);
        }
    };

    private final OnClickListener mPreviewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Long id = (Long) v.getTag();
            mWebcamCallbacks.showPreview(id);
        }
    };
}
