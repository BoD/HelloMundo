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
package org.jraf.android.worldtour.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.jraf.android.util.Blocking;
import org.jraf.android.util.HttpUtil;
import org.jraf.android.util.IoUtil;
import org.jraf.android.worldtour.provider.WebcamColumns;
import org.jraf.android.worldtour.provider.WebcamType;


public class WebcamManager {
    private static final String URL_DATABASE = "http://lubek.b.free.fr/data3.txt_fr";

    private static final String HTTP = "http://";
    private static final String EMPTY = "-";

    private static WebcamManager sWebcamManager;

    public synchronized static WebcamManager get() {
        if (sWebcamManager == null) {
            sWebcamManager = new WebcamManager();
        }
        return sWebcamManager;
    }

    private WebcamManager() {}

    @Blocking
    public void refreshDatabaseFromNetwork(Context context) throws IOException {
        final ArrayList<String> publicIds = new ArrayList<String>(40);
        final InputStream inputStream = HttpUtil.getAsStream(URL_DATABASE);
        final ContentResolver contentResolver = context.getContentResolver();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            final ContentValues values = new ContentValues(12);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                parseLine(line, values);

                // Already present?
                Long id = null;
                final String publicId = values.getAsString(WebcamColumns.PUBLIC_ID);
                publicIds.add(publicId);
                final String[] projection = { WebcamColumns._ID };
                final String selection = WebcamColumns.PUBLIC_ID + "=?";
                final String[] selectionArgs = { publicId };
                final Cursor cursor = contentResolver.query(WebcamColumns.CONTENT_URI, projection, selection, selectionArgs, null);
                try {
                    if (cursor.moveToNext()) {
                        id = cursor.getLong(0);
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }

                if (id == null) {
                    // Not found: new object
                    contentResolver.insert(WebcamColumns.CONTENT_URI, values);
                } else {
                    // Found: existing object
                    contentResolver.update(WebcamColumns.CONTENT_URI, values, selection, selectionArgs);
                }
                values.clear();
            }
        } finally {
            IoUtil.close(inputStream);
        }

        final String publicIdList = TextUtils.join(",", publicIds);
        final String where = WebcamColumns.TYPE + "=? and " + WebcamColumns.PUBLIC_ID + " not in (" + publicIdList + ")";
        final String[] selectionArgs = { String.valueOf(WebcamType.SERVER) };
        // Now delete objects that exist locally but not remotely
        contentResolver.delete(WebcamColumns.CONTENT_URI, where, selectionArgs);
    }

    private void parseLine(String line, ContentValues res) {
        final String[] vals = line.split(";");
        res.put(WebcamColumns.PUBLIC_ID, vals[0]);
        res.put(WebcamColumns.NAME, vals[1]);
        res.put(WebcamColumns.LOCATION, vals[2]);
        res.put(WebcamColumns.SOURCE_URL, vals[3]);
        res.put(WebcamColumns.URL, HTTP + vals[4]);
        res.put(WebcamColumns.THUMB_URL, HTTP + vals[5]);
        final String[] addedDateVals = vals[6].split("-");
        final long dateMillis = new GregorianCalendar(Integer.parseInt(addedDateVals[0]), Integer.parseInt(addedDateVals[1]) - 1,
                Integer.parseInt(addedDateVals[2])).getTimeInMillis();
        res.put(WebcamColumns.ADDED_DATE, dateMillis);
        res.put(WebcamColumns.TIMEZONE, vals[7]);

        // Referer
        if (vals.length > 8) {
            final String referer = vals[8];
            if (!EMPTY.equals(referer)) {
                res.put(WebcamColumns.HTTP_REFERER, HTTP + referer);
            }
        }

        // Resize
        if (vals.length > 9) {
            final String resize = vals[9];
            if (!EMPTY.equals(resize)) {
                final String[] resizeVals = resize.split("x");
                final int width = Integer.parseInt(resizeVals[0]);
                final int height = Integer.parseInt(resizeVals[1]);
                res.put(WebcamColumns.RESIZE_WIDTH, width);
                res.put(WebcamColumns.RESIZE_HEIGHT, height);
            }
        }

        // Visibility begin
        if (vals.length > 10) {
            final String visibilityBegin = vals[10];
            if (!EMPTY.equals(visibilityBegin)) {
                final String[] visibilityBeginVals = visibilityBegin.split(":");
                final int beginHour = Integer.parseInt(visibilityBeginVals[0]);
                final int beginMin = Integer.parseInt(visibilityBeginVals[1]);
                res.put(WebcamColumns.VISIBILITY_BEGIN_HOUR, beginHour);
                res.put(WebcamColumns.VISIBILITY_BEGIN_MIN, beginMin);
            }
        }

        // Visibility end
        if (vals.length > 11) {
            final String visibilityEnd = vals[11];
            if (!EMPTY.equals(visibilityEnd)) {
                final String[] visibilityEndVals = visibilityEnd.split(":");
                final int endHour = Integer.parseInt(visibilityEndVals[0]);
                final int endMin = Integer.parseInt(visibilityEndVals[1]);
                res.put(WebcamColumns.VISIBILITY_END_HOUR, endHour);
                res.put(WebcamColumns.VISIBILITY_END_MIN, endMin);
            }
        }

        res.put(WebcamColumns.TYPE, WebcamType.SERVER);
    }
}
