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

import java.io.IOException;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.closed.IoUtil;
import org.jraf.android.util.closed.StringUtil;
import org.jraf.android.worldtour.app.common.LifecycleDispatchSherlockActivity;


public class EulaActivity extends LifecycleDispatchSherlockActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eula);
        findViewById(R.id.btnOk).setOnClickListener(mOkOnClickListener);
        final WebView eulaWebView = (WebView) findViewById(R.id.webView);
        String html;
        try {
            html = IoUtil.inputStreamToString(getResources().openRawResource(R.raw.eula));
        } catch (final IOException e) {
            // should never happen
            throw new AssertionError("Could not read eula file");
        }
        html = StringUtil.reworkForWebView(html);
        eulaWebView.loadData(html, "text/html", "utf-8");
    }

    private final OnClickListener mOkOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
