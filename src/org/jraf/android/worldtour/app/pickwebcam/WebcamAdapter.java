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

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.ui.ViewHolder;
import org.jraf.android.util.ui.animation.ExtendHeightAnimation;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.provider.WebcamCursorWrapper;
import org.jraf.android.worldtour.provider.WebcamType;

import com.squareup.picasso.Picasso;

public class WebcamAdapter extends ResourceCursorAdapter {
    private final int mExtendedHeight;
    private final WebcamCallbacks mWebcamCallbacks;
    private final HashSet<Long> mExtendedIds = new HashSet<Long>(5);
    private final HashMap<String, String> mLocalTimeCache = new HashMap<String, String>(50);
    private ListView mListView;
    private final long mCurrentWebcamId;

    public WebcamAdapter(Context context, WebcamCallbacks webcamCallbacks, long currentWebcamId) {
        super(context, R.layout.pick_webcam_item, null, false);
        mExtendedHeight = context.getResources().getDimensionPixelSize(R.dimen.cell_webcam_extended_height);
        mWebcamCallbacks = webcamCallbacks;
        mCurrentWebcamId = currentWebcamId;
    }

    public void setListView(ListView listView) {
        mListView = listView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        WebcamCursorWrapper c = new WebcamCursorWrapper(cursor); // TODO: do not do that in bindView (which is called for every item!)
        long id = c.getId();

        TextView txtName = (TextView) ViewHolder.get(view, R.id.txtName);
        String name = c.getName();
        txtName.setText(name);

        Long type = c.getType();
        boolean isUserWebcam = type != null && type.intValue() == WebcamType.USER;

        // Extend
        View conExtended = ViewHolder.get(view, R.id.conExtended);
        conExtended.setTag(id);
        View btnExtend = ViewHolder.get(view, R.id.btnExtend);
        btnExtend.setTag(conExtended);

        if (cursor.getPosition() == cursor.getCount() - 1) {
            btnExtend.setTag(R.id.lastItem, true);
        } else {
            btnExtend.setTag(R.id.lastItem, false);
        }
        btnExtend.setOnClickListener(mExtendOnClickListener);
        LayoutParams layoutParams = conExtended.getLayoutParams();
        if (mExtendedIds.contains(id)) {
            layoutParams.height = mExtendedHeight;
        } else {
            layoutParams.height = 0;
        }

        // Thumbnail
        ImageView imgThumbnail = ViewHolder.get(view, R.id.imgThumbnail);
        if (isUserWebcam) {
            imgThumbnail.setImageResource(R.drawable.ic_thumbnail_user_defined);
        } else {
            imgThumbnail.setImageResource(0);
            Picasso.with(context).load(c.getThumbUrl())
                    .resizeDimen(R.dimen.pickWebcam_item_imgThumbnail_widthHeight, R.dimen.pickWebcam_item_imgThumbnail_widthHeight).centerCrop()
                    .placeholder(R.drawable.ic_thumbnail_bg).into(imgThumbnail);
        }

        // Location & time
        TextView txtLocationAndTime = ViewHolder.get(view, R.id.txtLocationAndTime);
        if (isUserWebcam) {
            txtLocationAndTime.setText(R.string.common_userDefined);
        } else {
            String location = c.getLocation();
            String publicId = c.getPublicId();
            boolean specialCam = Constants.SPECIAL_CAMS.contains(publicId);
            if (!specialCam) {
                location += " - " + getLocalTime(context, c.getTimezone());
            }
            txtLocationAndTime.setText(location);
        }

        // Source url
        TextView txtSourceUrl = ViewHolder.get(view, R.id.txtSourceUrl);
        String sourceUrl;
        if (isUserWebcam) {
            sourceUrl = c.getUrl();
            sourceUrl = sourceUrl.substring("http://".length());
            int slashIdx = sourceUrl.indexOf('/');
            if (slashIdx != -1) {
                sourceUrl = sourceUrl.substring(0, slashIdx);
            }
        } else {
            sourceUrl = c.getSourceUrl();
        }
        txtSourceUrl.setText(context.getString(R.string.pickWebcam_source, sourceUrl));
        txtSourceUrl.setTag(sourceUrl);
        txtSourceUrl.setOnClickListener(mSourceOnClickListener);

        // Exclude from random
        Long excludeRandom = c.getExcludeRandom();
        boolean excludedFromRandom = excludeRandom != null && excludeRandom.intValue() == 1;
        View btnExcludeFromRandom = ViewHolder.get(view, R.id.btnExcludeFromRandom);
        btnExcludeFromRandom.setSelected(excludedFromRandom);
        btnExcludeFromRandom.setTag(id);
        btnExcludeFromRandom.setOnClickListener(mExcludeFromRandomOnClickListener);
        ImageView imgExcludedFromRandom = ViewHolder.get(view, R.id.imgExcludedFromRandom);
        if (excludedFromRandom) {
            imgExcludedFromRandom.setVisibility(View.VISIBLE);
        } else {
            imgExcludedFromRandom.setVisibility(View.GONE);
        }

        // Show on map / delete
        View btnShowOnMap = ViewHolder.get(view, R.id.btnShowOnMap);
        ImageView imgShowOnMap = ViewHolder.get(view, R.id.imgShowOnMap);
        TextView txtShowOnMap = ViewHolder.get(view, R.id.txtShowOnMap);
        if (isUserWebcam) {
            imgShowOnMap.setImageResource(R.drawable.ic_ext_delete);
            txtShowOnMap.setText(R.string.pickWebcam_delete);
            btnShowOnMap.setTag(id);
            btnShowOnMap.setOnClickListener(mDeleteOnClickListener);
        } else {
            imgShowOnMap.setImageResource(R.drawable.ic_ext_show_on_map);
            txtShowOnMap.setText(R.string.pickWebcam_showOnMap);
            String coordinates = c.getCoordinates();
            if (coordinates == null) {
                btnShowOnMap.setEnabled(false);
            } else {
                btnShowOnMap.setEnabled(true);
                btnShowOnMap.setTag(R.id.coordinates, coordinates);
                btnShowOnMap.setTag(R.id.name, name);
                btnShowOnMap.setOnClickListener(mShowOnMapOnClickListener);
            }
        }

        View btnPreview = ViewHolder.get(view, R.id.btnPreview);
        btnPreview.setTag(id);
        btnPreview.setOnClickListener(mPreviewOnClickListener);

        // Current webcam
        View conMainItem = ViewHolder.get(view, R.id.conMainItem);
        conMainItem.setSelected(id == mCurrentWebcamId);
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
            View conExtended = (View) v.getTag();
            long id = (Long) conExtended.getTag();
            LayoutParams layoutParams = conExtended.getLayoutParams();
            if (layoutParams.height == 0) {
                // Extend
                ExtendHeightAnimation animation = new ExtendHeightAnimation(conExtended, mExtendedHeight, true);
                animation.setDuration(300);
                if ((Boolean) v.getTag(R.id.lastItem) == true) {
                    animation.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation a) {}

                        @Override
                        public void onAnimationRepeat(Animation a) {}

                        @Override
                        public void onAnimationEnd(Animation a) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                                moveToBottomEclair();
                            } else {
                                moveToBottomFroyo();
                            }
                        }
                    });
                }
                conExtended.startAnimation(animation);
                mExtendedIds.add(id);
            } else {
                // Collapse
                ExtendHeightAnimation animation = new ExtendHeightAnimation(conExtended, mExtendedHeight, false);
                animation.setDuration(300);
                conExtended.startAnimation(animation);
                mExtendedIds.remove(id);
            }
        }
    };

    private void moveToBottomEclair() {
        mListView.setSelection(getCount() - 1);
    }

    @TargetApi(8)
    private void moveToBottomFroyo() {
        mListView.smoothScrollToPosition(getCount());
    }

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

    private final OnClickListener mDeleteOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            long id = (Long) v.getTag();
            mWebcamCallbacks.delete(id);
        }
    };
}
