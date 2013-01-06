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
package org.jraf.android.worldtour.app.welcome;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;

public class WelcomeActivity extends FragmentActivity {
    private static final String TAG = Constants.TAG + WelcomeActivity.class.getSimpleName();

    private static final String PREFIX = WelcomeActivity.class.getName() + ".";
    public static final String EXTRA_RECT_PICK = PREFIX + "EXTRA_RECT_PICK";
    public static final String EXTRA_RECT_SWITCH = PREFIX + "EXTRA_RECT_SWITCH";

    protected static final int NB_PAGES = 4;

    private ViewPager mViewPager;
    private Button mBtnNext;
    private ImageView mImgDot0;
    private ImageView mImgDot1;
    private ImageView mImgDot2;
    private ImageView mImgDot3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
        mBtnNext = (Button) findViewById(R.id.btnNext);
        mBtnNext.setOnClickListener(mNextOnClickListener);
        mImgDot0 = (ImageView) findViewById(R.id.imgDot0);
        mImgDot1 = (ImageView) findViewById(R.id.imgDot1);
        mImgDot2 = (ImageView) findViewById(R.id.imgDot2);
        mImgDot3 = (ImageView) findViewById(R.id.imgDot3);
        mOnPageChangeListener.onPageSelected(0);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int resumeIndex = sharedPreferences.getInt(Constants.PREF_WELCOME_RESUME_INDEX, 0);
        mViewPager.setCurrentItem(resumeIndex);

        // Now reset the values
        Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.PREF_WELCOME_RESUME_INDEX, -1);
        editor.putBoolean(Constants.PREF_SEEN_WELCOME, true);
        editor.commit();
    }

    private void setShowCasePosition(View view, Rect r) {
        Rect rect = new Rect(r);
        if (rect.top > 0) {
            int margin = getResources().getDimensionPixelSize(R.dimen.welcome_showCase_margin);
            rect.inset(-margin, -margin);
        }

        View imgShowCase = view.findViewById(R.id.imgShowCase);
        LayoutParams layoutParams = imgShowCase.getLayoutParams();
        layoutParams.width = rect.width();
        layoutParams.height = rect.height();
        ((RelativeLayout.LayoutParams) layoutParams).topMargin = rect.top;
        ((RelativeLayout.LayoutParams) layoutParams).leftMargin = rect.left;
        imgShowCase.setLayoutParams(layoutParams);

        View imgShowCaseHide = view.findViewById(R.id.imgShowCase_hide);
        layoutParams = imgShowCaseHide.getLayoutParams();
        layoutParams.width = rect.width();
        layoutParams.height = rect.height();
        ((RelativeLayout.LayoutParams) layoutParams).topMargin = rect.top;
        ((RelativeLayout.LayoutParams) layoutParams).leftMargin = rect.left;
        imgShowCaseHide.setLayoutParams(layoutParams);


        View imgAbove = view.findViewById(R.id.imgAbove);
        layoutParams = imgAbove.getLayoutParams();
        layoutParams.height = rect.top;
        imgAbove.setLayoutParams(layoutParams);

        View imgLeft = view.findViewById(R.id.imgLeft);
        layoutParams = imgLeft.getLayoutParams();
        layoutParams.width = rect.left;
        imgLeft.setLayoutParams(layoutParams);
    }

    private final WelcomePagerAdapter mPagerAdapter = new WelcomePagerAdapter();

    private final OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position >= 2) return;
            View view = mPagerAdapter.getView(position);
            TextView txtText = (TextView) view.findViewById(R.id.txtText);
            int alpha = (int) (255 * (1f - positionOffset * 3));
            if (alpha > 255) alpha = 255;
            if (alpha < 0) alpha = 0;
            txtText.setTextColor(Color.argb(alpha, 255, 255, 255));

            ImageView imgArrowUp = (ImageView) view.findViewById(R.id.imgArrowUp);
            imgArrowUp.setAlpha(alpha);

            ImageView imgShowCaseHide = (ImageView) view.findViewById(R.id.imgShowCase_hide);
            imgShowCaseHide.setVisibility(View.VISIBLE);
            imgShowCaseHide.getBackground().setAlpha(255 - alpha);
        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    mImgDot0.setSelected(true);
                    mImgDot1.setSelected(false);
                    mImgDot2.setSelected(false);
                    mImgDot3.setSelected(false);
                    mBtnNext.setText(R.string.welcome_next);
                    mBtnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_next, 0);
                    break;

                case 1:
                    mImgDot0.setSelected(false);
                    mImgDot1.setSelected(true);
                    mImgDot2.setSelected(false);
                    mImgDot3.setSelected(false);
                    mBtnNext.setText(R.string.welcome_next);
                    mBtnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_next, 0);
                    break;

                case 2:
                    mImgDot0.setSelected(false);
                    mImgDot1.setSelected(false);
                    mImgDot2.setSelected(true);
                    mImgDot3.setSelected(false);
                    mBtnNext.setText(R.string.welcome_next);
                    mBtnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_next, 0);
                    break;

                case 3:
                    mImgDot0.setSelected(false);
                    mImgDot1.setSelected(false);
                    mImgDot2.setSelected(false);
                    mImgDot3.setSelected(true);
                    mBtnNext.setText(R.string.common_done);
                    mBtnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_done, 0);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    private final OnClickListener mNextOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int currentItem = mViewPager.getCurrentItem();
            if (currentItem < NB_PAGES - 1) {
                mViewPager.setCurrentItem(currentItem + 1, true);
            } else {
                finish();
            }
        }
    };

    @Override
    public void onBackPressed() {
        int currentItem = mViewPager.getCurrentItem();
        if (currentItem > 0) {
            mViewPager.setCurrentItem(currentItem - 1, true);
        }
    }


    /*
     * Pager adapter.
     */

    private class WelcomePagerAdapter extends PagerAdapter {
        private final View[] mViews = new View[NB_PAGES];

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.welcome_page, container, false);
            TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            TextView txtText = (TextView) view.findViewById(R.id.txtText);
            TextView txtText2 = (TextView) view.findViewById(R.id.txtText2);
            ImageView imgArrowUp = (ImageView) view.findViewById(R.id.imgArrowUp);
            ImageView imgWidget = (ImageView) view.findViewById(R.id.imgWidget);
            ImageView imgLogo = (ImageView) view.findViewById(R.id.imgLogo);
            View imgShowCase = view.findViewById(R.id.imgShowCase);
            int imgArrowUpWidth = getResources().getDimensionPixelSize(R.dimen.welcome_imgArrowUp_width);
            int marginTop = getResources().getDimensionPixelSize(R.dimen.welcome_marginTop_arrowUp);
            switch (position) {
                case 0:
                    txtTitle.setText(R.string.welcome_0_txtTitle);
                    txtText.setText(R.string.welcome_0_txtText);
                    imgWidget.setVisibility(View.GONE);
                    imgLogo.setVisibility(View.VISIBLE);
                    Rect rectPick = getIntent().getParcelableExtra(EXTRA_RECT_PICK);
                    LayoutParams layoutParams = imgArrowUp.getLayoutParams();
                    ((RelativeLayout.LayoutParams) layoutParams).topMargin = rectPick.bottom + marginTop;
                    ((RelativeLayout.LayoutParams) layoutParams).leftMargin = rectPick.centerX() - imgArrowUpWidth / 2;
                    imgArrowUp.setLayoutParams(layoutParams);
                    setShowCasePosition(view, rectPick);
                    break;

                case 1:
                    txtTitle.setVisibility(View.INVISIBLE);
                    txtText.setText(R.string.welcome_1_txtText);
                    imgWidget.setVisibility(View.GONE);
                    imgLogo.setVisibility(View.GONE);
                    Rect rectSwitch = getIntent().getParcelableExtra(EXTRA_RECT_SWITCH);
                    setShowCasePosition(view, rectSwitch);
                    layoutParams = imgArrowUp.getLayoutParams();
                    ((RelativeLayout.LayoutParams) layoutParams).topMargin = rectSwitch.bottom + marginTop;
                    ((RelativeLayout.LayoutParams) layoutParams).leftMargin = rectSwitch.centerX() - imgArrowUpWidth / 2;
                    imgArrowUp.setLayoutParams(layoutParams);
                    break;

                case 2:
                    imgArrowUp.setVisibility(View.GONE);
                    txtTitle.setVisibility(View.INVISIBLE);
                    txtText2.setText(R.string.welcome_2_txtText);
                    imgWidget.setVisibility(View.VISIBLE);
                    imgLogo.setVisibility(View.GONE);
                    imgShowCase.setBackgroundResource(R.color.welcome_bg);
                    break;

                case 3:
                    imgArrowUp.setVisibility(View.GONE);
                    txtTitle.setText(R.string.welcome_3_txtTitle);
                    txtText2.setText(R.string.welcome_3_txtText);
                    imgWidget.setVisibility(View.GONE);
                    imgLogo.setVisibility(View.GONE);
                    imgShowCase.setBackgroundResource(R.color.welcome_bg);
                    break;
            }

            container.addView(view, 0);
            mViews[position] = view;


            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return NB_PAGES;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public View getView(int position) {
            return mViews[position];
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Config.LOGD) Log.d(TAG, "onConfigurationChanged");
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(Constants.PREF_WELCOME_RESUME_INDEX, mViewPager.getCurrentItem()).commit();
        finish();
    }
}
