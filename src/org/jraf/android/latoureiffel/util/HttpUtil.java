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
package org.jraf.android.latoureiffel.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

import org.jraf.android.latoureiffel.Config;
import org.jraf.android.latoureiffel.Constants;
import org.jraf.android.latoureiffel.app.Application;

public class HttpUtil {
    private static final String TAG = Constants.TAG + HttpUtil.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 15000;

    private static final String HTTP_USER_AGENT = System.getProperties().getProperty("http.agent") + " " + Constants.TAG + Application.sVersionCode;

    private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_HEADER_ACCEPT = "Accept";
    private static final String HTTP_HEADER_USER_AGENT = "User-Agent";


    /*
     * Https: trust every server, don't check for any certificate.
     * See http://stackoverflow.com/questions/995514/https-connection-android/1000205#1000205
     */
    static {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        } };

        // Install the all-trusting trust manager
        try {
            final SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (final Exception e) {
            Log.e(TAG, "Could not install the all-trusting trust manager", e);
        }
    }

    /**
     * Https: accept all hostnames.
     */
    private static final HostnameVerifier ACCEPT_ALL_HOSTNAME_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static class Options {
        public String contentType;
        public String accept;
    }

    public static InputStream getAsStream(String url, Options options) throws IOException {
        if (Config.LOGD_HTTP) Log.d(TAG, "----------------");
        if (Config.LOGD_HTTP) Log.d(TAG, "getAsStream url=" + url);
        final URL u = new URL(url);
        final HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        // Https: accept all hostnames
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setHostnameVerifier(ACCEPT_ALL_HOSTNAME_VERIFIER);
        }

        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        if (options.contentType != null) {
            connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, options.contentType);
        }
        if (options.accept != null) {
            connection.setRequestProperty(HTTP_HEADER_ACCEPT, options.accept);
        }
        connection.setRequestProperty(HTTP_HEADER_USER_AGENT, HTTP_USER_AGENT);
        connection.setUseCaches(false);

        InputStream connectionInputStream = null;
        try {
            connectionInputStream = connection.getInputStream();
        } catch (final IOException e) {
            // non 2xx responses are handled by throwing an IOException
            int responseCode = -1;
            try {
                responseCode = connection.getResponseCode();
            } catch (final IOException e2) {
                // in case of 401 without a WWW-Authenticate header, getResponseCode throws a specific IOException
                if (e2.getMessage() != null && e2.getMessage().contains("authentication")) {
                    responseCode = 401;
                } else {
                    Log.w(TAG, "getAsStream Could not get response code", e);
                }
            }
            if (responseCode == -1) {
                connection.disconnect();
                throw e;
            }
            connection.disconnect();
            throw new IOException("Server replied with code " + responseCode, e);
        }

        final InputStream inputStream = connectionInputStream;
        // this InputStream will call connection.disconnect() when it is closed
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public int available() throws IOException {
                return inputStream.available();
            }

            @Override
            public void mark(int readlimit) {
                inputStream.mark(readlimit);
            }

            @Override
            public boolean markSupported() {
                return inputStream.markSupported();
            }

            @Override
            public int read(byte[] buffer) throws IOException {
                return inputStream.read(buffer);
            }

            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException {
                return inputStream.read(buffer, offset, length);
            }

            @Override
            public long skip(long byteCount) throws IOException {
                return inputStream.skip(byteCount);
            }

            @Override
            public void close() throws IOException {
                if (Config.LOGD_HTTP) Log.d(TAG, "Input Stream close()");
                inputStream.close();
                connection.disconnect();
            }
        };
    }

    public static InputStream getAsStream(String url) throws IOException {
        return getAsStream(url, new Options());
    }
}
