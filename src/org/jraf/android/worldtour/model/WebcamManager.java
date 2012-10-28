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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.Blocking;
import org.jraf.android.util.HttpUtil;
import org.jraf.android.util.IoUtil;
import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.provider.WebcamColumns;
import org.jraf.android.worldtour.provider.WebcamType;

public class WebcamManager {
    private static String TAG = Constants.TAG + WebcamManager.class.getSimpleName();

    private static String URL_DATABASE = "http://lubek.b.free.fr/data4.txt";

    private static String HTTP = "http://";
    private static String EMPTY = "-";

    private static final WebcamManager INSTANCE = new WebcamManager();

    public synchronized static WebcamManager get() {
        return INSTANCE;
    }

    private WebcamManager() {}

    @Blocking
    public void insertWebcamsFromBundledFile(Context context) {
        if (Config.LOGD) Log.d(TAG, "insertWebcamsFromBundledFile");
        InputStream inputStream = context.getResources().openRawResource(R.raw.data4);
        ContentResolver contentResolver = context.getContentResolver();
        try {
            insertWebcams(inputStream, contentResolver);
        } catch (IOException e) {
            Log.e(TAG, "Could not insert webcams from bundled file", e);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(Constants.PREF_DATABASE_LAST_DOWNLOAD, System.currentTimeMillis()).commit();
    }

    @Blocking
    public void refreshDatabaseFromNetwork(Context context) throws IOException {
        InputStream inputStream = HttpUtil.getAsStream(URL_DATABASE);
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<String> publicIds = insertWebcams(inputStream, contentResolver);
        String publicIdList = TextUtils.join(",", publicIds);
        String where = WebcamColumns.TYPE + "=? and " + WebcamColumns.PUBLIC_ID + " not in (" + publicIdList + ")";
        String[] selectionArgs = { String.valueOf(WebcamType.SERVER) };
        // Now delete objects that exist locally but not remotely
        contentResolver.delete(WebcamColumns.CONTENT_URI, where, selectionArgs);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(Constants.PREF_DATABASE_LAST_DOWNLOAD, System.currentTimeMillis()).commit();
    }

    private ArrayList<String> insertWebcams(InputStream inputStream, ContentResolver contentResolver) throws IOException {
        ArrayList<String> publicIds = new ArrayList<String>(40);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            ContentValues values = new ContentValues(12);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                parseLine(line, values);

                // Already present?
                Long id = null;
                String publicId = values.getAsString(WebcamColumns.PUBLIC_ID);
                publicIds.add(publicId);
                String[] projection = { WebcamColumns._ID };
                String selection = WebcamColumns.PUBLIC_ID + "=?";
                String[] selectionArgs = { publicId };
                Cursor cursor = contentResolver.query(WebcamColumns.CONTENT_URI, projection, selection, selectionArgs, null);
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
            return publicIds;
        } finally {
            IoUtil.close(inputStream);
        }
    }

    private void parseLine(String line, ContentValues res) {
        String[] vals = line.split(";");
        res.put(WebcamColumns.PUBLIC_ID, vals[0]);
        res.put(WebcamColumns.NAME, vals[1]);
        res.put(WebcamColumns.LOCATION, vals[2]);
        res.put(WebcamColumns.SOURCE_URL, vals[3]);
        res.put(WebcamColumns.URL, HTTP + vals[4]);
        res.put(WebcamColumns.THUMB_URL, HTTP + vals[5]);
        String[] addedDateVals = vals[6].split("-");
        long dateMillis = new GregorianCalendar(Integer.parseInt(addedDateVals[0]), Integer.parseInt(addedDateVals[1]) - 1, Integer.parseInt(addedDateVals[2]))
                .getTimeInMillis();
        res.put(WebcamColumns.ADDED_DATE, dateMillis);
        res.put(WebcamColumns.TIMEZONE, vals[7]);

        // Referer
        if (vals.length > 8) {
            String referer = vals[8];
            if (!EMPTY.equals(referer)) {
                res.put(WebcamColumns.HTTP_REFERER, HTTP + referer);
            }
        }

        // Resize
        if (vals.length > 9) {
            String resize = vals[9];
            if (!EMPTY.equals(resize)) {
                String[] resizeVals = resize.split("x");
                int width = Integer.parseInt(resizeVals[0]);
                int height = Integer.parseInt(resizeVals[1]);
                res.put(WebcamColumns.RESIZE_WIDTH, width);
                res.put(WebcamColumns.RESIZE_HEIGHT, height);
            }
        }

        // Visibility begin
        if (vals.length > 10) {
            String visibilityBegin = vals[10];
            if (!EMPTY.equals(visibilityBegin)) {
                String[] visibilityBeginVals = visibilityBegin.split(":");
                int beginHour = Integer.parseInt(visibilityBeginVals[0]);
                int beginMin = Integer.parseInt(visibilityBeginVals[1]);
                res.put(WebcamColumns.VISIBILITY_BEGIN_HOUR, beginHour);
                res.put(WebcamColumns.VISIBILITY_BEGIN_MIN, beginMin);
            }
        }

        // Visibility end
        if (vals.length > 11) {
            String visibilityEnd = vals[11];
            if (!EMPTY.equals(visibilityEnd)) {
                String[] visibilityEndVals = visibilityEnd.split(":");
                int endHour = Integer.parseInt(visibilityEndVals[0]);
                int endMin = Integer.parseInt(visibilityEndVals[1]);
                res.put(WebcamColumns.VISIBILITY_END_HOUR, endHour);
                res.put(WebcamColumns.VISIBILITY_END_MIN, endMin);
            }
        }

        // Coordinates
        if (vals.length > 12) {
            String coordinates = vals[12];
            if (!EMPTY.equals(coordinates)) {
                res.put(WebcamColumns.COORDINATES, coordinates);
            }
        }
        res.put(WebcamColumns.TYPE, WebcamType.SERVER);
    }

    public void insertUserWebcam(Context context, String name, String url) {
        ContentValues values = new ContentValues(12);
        values.put(WebcamColumns.PUBLIC_ID, "user_" + System.currentTimeMillis());
        values.put(WebcamColumns.NAME, name);
        values.put(WebcamColumns.URL, HTTP + url);
        values.put(WebcamColumns.ADDED_DATE, System.currentTimeMillis());
        values.put(WebcamColumns.TYPE, WebcamType.USER);
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.insert(WebcamColumns.CONTENT_URI, values);
    }
}
