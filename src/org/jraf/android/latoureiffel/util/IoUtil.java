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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtil {
    /**
     * Note: {@code in} and {@code out} won't be closed.
     */
    public static long copy(final InputStream in, final OutputStream out) throws IOException {
        long res = 0;
        final byte[] buffer = new byte[1500];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            out.flush();
            res += read;
        }
        return res;
    }

    /**
     * Note: {@code in} won't be closed.
     */
    public static final String inputStreamToString(final InputStream in) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            stringBuilder.append(new String(buffer, 0, read, "utf-8"));
        }
        return stringBuilder.toString();
    }

    /**
     * Silently close ignoring any exception.
     */
    public static void close(final InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (final IOException e) {
                // so be it
            }
        }
    }

    /**
     * Silently close ignoring any exception.
     */
    public static void close(final OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (final IOException e) {
                // so be it
            }
        }
    }

    /**
     * Silently close ignoring any exception.
     */
    public static void close(final BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException e) {
                // so be it
            }
        }
    }
}
