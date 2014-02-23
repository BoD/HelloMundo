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
package org.jraf.android.hellomundo.app.about;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

import org.jraf.android.hellomundo.app.common.BaseActivity;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.webview.WebViewUtil;


public class EulaActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eula);
        findViewById(R.id.btnOk).setOnClickListener(mOkOnClickListener);
        WebView eulaWebView = (WebView) findViewById(R.id.webView);
        WebViewUtil.loadFromRaw(eulaWebView, R.raw.eula);
    }

    private final OnClickListener mOkOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
