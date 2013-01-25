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
package org.jraf.android.worldtour.app.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.worldtour.app.common.LifecycleDispatchSherlockActivity;


public class AboutActivity extends LifecycleDispatchSherlockActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        findViewById(R.id.btnShare).setOnClickListener(mShareOnClickListener);
        findViewById(R.id.btnRate).setOnClickListener(mRateOnClickListener);
        findViewById(R.id.btnOtherApps).setOnClickListener(mOtherAppsOnClickListener);
        findViewById(R.id.btnEula).setOnClickListener(mEulaOnClickListener);
        findViewById(R.id.btnDonate).setOnClickListener(mDonateOnClickListener);
    }

    private final OnClickListener mShareOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_shareText_subject));
            String shareTextBody = getString(R.string.about_shareText_body, getPackageName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareTextBody);
            shareIntent.putExtra("sms_body", shareTextBody);
            startActivity(/*Intent.createChooser(*/shareIntent/*, null)*/);
        }
    };

    private final OnClickListener mRateOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getPackageName()));
            startActivity(intent);
        }
    };

    private final OnClickListener mOtherAppsOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://search?q=pub:BoD"));
            startActivity(intent);
        }
    };

    private final OnClickListener mEulaOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(AboutActivity.this, EulaActivity.class));
        }
    };

    private final OnClickListener mDonateOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri
                    .parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=BoD%40JRAF%2eorg&lc=US&item_name=Donate%20to%20BoD&item_number=Donate%20to%20BoD&no_note=0&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest"
                            + getPackageName()));
            startActivity(intent);
        }
    };
}
