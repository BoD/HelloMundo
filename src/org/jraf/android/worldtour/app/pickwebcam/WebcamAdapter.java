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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.ViewHolder;
import org.jraf.android.util.ui.ExtendHeightAnimation;
import org.jraf.android.util.ui.LoadingImageView;

public class WebcamAdapter extends ResourceCursorAdapter {
    private final int mExtendedHeight;

    public WebcamAdapter(Context context) {
        super(context, R.layout.cell_webcam, null, false);
        mExtendedHeight = context.getResources().getDimensionPixelSize(R.dimen.cell_webcam_layExtended_height);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final TextView txtName = (TextView) ViewHolder.get(view, R.id.txtName);
        txtName.setText(cursor.getString(1));

        final View layExtended = ViewHolder.get(view, R.id.layExtended);

        final View btnExtend = ViewHolder.get(view, R.id.btnExtend);
        btnExtend.setTag(layExtended);
        btnExtend.setOnClickListener(mOnExtendOnClickListener);

        final LoadingImageView imgThumbnail = (LoadingImageView) ViewHolder.get(view, R.id.imgThumbnail);
        imgThumbnail.loadBitmap(cursor.getString(2));

        final TextView txtLocationAndTime = (TextView) ViewHolder.get(view, R.id.txtLocationAndTime);
        txtLocationAndTime.setText(cursor.getString(3));

        final TextView txtSourceUrl = (TextView) ViewHolder.get(view, R.id.txtSourceUrl);
        txtSourceUrl.setText(context.getString(R.string.pickWebcam_source, cursor.getString(4)));

    }

    private final OnClickListener mOnExtendOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final View layExtended = (View) v.getTag();
            final LayoutParams layoutParams = layExtended.getLayoutParams();
            if (layoutParams.height == 0) {
                //                layoutParams.height = mExtendedHeight;
                final ExtendHeightAnimation animation = new ExtendHeightAnimation(layExtended, mExtendedHeight, true);
                animation.setDuration(300);
                layExtended.startAnimation(animation);
            } else {
                //                layoutParams.height = 0;
                final ExtendHeightAnimation animation = new ExtendHeightAnimation(layExtended, mExtendedHeight, false);
                animation.setDuration(300);
                layExtended.startAnimation(animation);
            }
            //            layExtended.setLayoutParams(layoutParams);
        }
    };
}
