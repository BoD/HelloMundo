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
package org.jraf.android.hellomundo.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jraf.android.hellomundo.Config;
import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.provider.webcam.WebcamColumns;
import org.jraf.android.hellomundo.provider.webcam.WebcamContentValues;
import org.jraf.android.hellomundo.provider.webcam.WebcamCursor;
import org.jraf.android.hellomundo.provider.webcam.WebcamSelection;
import org.jraf.android.hellomundo.provider.webcam.WebcamType;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.annotation.Background;
import org.jraf.android.util.annotation.Background.Type;
import org.jraf.android.util.http.HttpUtil;
import org.jraf.android.util.io.IoUtil;

public class WebcamManager {
    private static String TAG = Constants.TAG + WebcamManager.class.getSimpleName();

    private static String URL_DATABASE = "http://lubek.b.free.fr/data7.csv";

    private static String HTTP = "http://";
    private static String EMPTY = "-";

    private static final WebcamManager INSTANCE = new WebcamManager();

    public static WebcamManager get() {
        return INSTANCE;
    }

    private WebcamManager() {}

    @Background
    public void insertWebcamsFromBundledFile(Context context) {
        if (Config.LOGD) Log.d(TAG, "insertWebcamsFromBundledFile");
        InputStream inputStream = context.getResources().openRawResource(R.raw.data7);
        ContentResolver contentResolver = context.getContentResolver();
        try {
            insertWebcams(inputStream, contentResolver);
        } catch (IOException e) {
            Log.e(TAG, "Could not insert webcams from bundled file", e);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(Constants.PREF_DATABASE_LAST_DOWNLOAD, System.currentTimeMillis()).commit();
    }

    @Background
    public void refreshDatabaseFromNetwork(Context context) throws IOException {
        InputStream inputStream = HttpUtil.get(URL_DATABASE).stream();
        ContentResolver contentResolver = context.getContentResolver();
        String[] publicIds = insertWebcams(inputStream, contentResolver);
        WebcamSelection where = new WebcamSelection().type(WebcamType.SERVER).and().publicIdNot(publicIds);
        // Now delete objects that exist locally but not remotely
        where.delete(contentResolver);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(Constants.PREF_DATABASE_LAST_DOWNLOAD, System.currentTimeMillis()).commit();
    }

    @Background({ Type.DISK, Type.NETWORK })
    private String[] insertWebcams(InputStream inputStream, ContentResolver contentResolver) throws IOException {
        ArrayList<String> publicIds = new ArrayList<String>(40);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String lastLine = "";
            boolean firstLine = true;
            ArrayList<WebcamContentValues> valuesList = new ArrayList<WebcamContentValues>(60);
            while ((line = reader.readLine()) != null) {
                // Check for specific first line (avoid networks that return a wifi login page)
                if (firstLine) {
                    if (!line.startsWith("#begin")) {
                        throw new IOException("First line of database file doesn't start with '#begin'");
                    }
                    firstLine = false;
                }
                lastLine = line;
                if (line.startsWith("#")) continue;
                WebcamContentValues values = new WebcamContentValues();
                parseLine(line, values);
                valuesList.add(values);
            }
            // Check for specific last line (avoid any network problems)
            if (!lastLine.startsWith("#end")) {
                throw new IOException("Last line of database file doesn't start with '#end'");
            }

            for (WebcamContentValues values : valuesList) {
                // Already present?
                Long id = null;
                String publicId = values.values().getAsString(WebcamColumns.PUBLIC_ID);
                publicIds.add(publicId);
                String[] projection = { WebcamColumns._ID };
                WebcamSelection where = new WebcamSelection().publicId(publicId);
                WebcamCursor cursor = where.query(contentResolver, projection);
                try {
                    if (cursor.moveToNext()) {
                        id = cursor.getId();
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }

                if (id == null) {
                    // Not found: new object
                    contentResolver.insert(WebcamColumns.CONTENT_URI, values.values());
                } else {
                    // Found: existing object
                    contentResolver.update(WebcamColumns.CONTENT_URI, values.values(), where.sel(), where.args());
                }
            }
            return publicIds.toArray(new String[publicIds.size()]);
        } finally {
            IoUtil.closeSilently(inputStream);
        }
    }

    private void parseLine(String line, WebcamContentValues res) {
        String[] vals = line.split(";");
        res.putPublicId(vals[0]);
        res.putName(vals[1]);
        res.putLocation(vals[2]);
        res.putSourceUrl(vals[3]);
        res.putUrl(HTTP + vals[4]);
        res.putThumbUrl(HTTP + vals[5]);
        String[] addedDateVals = vals[6].split("-");
        long dateMillis = new GregorianCalendar(Integer.parseInt(addedDateVals[0]), Integer.parseInt(addedDateVals[1]) - 1, Integer.parseInt(addedDateVals[2]))
                .getTimeInMillis();
        res.putAddedDate(dateMillis);
        res.putTimezone(vals[7]);

        // Referer
        String referer = vals[8];
        if (!EMPTY.equals(referer)) {
            res.putHttpReferer(HTTP + referer);
        }

        // Resize
        String resize = vals[9];
        if (!EMPTY.equals(resize)) {
            String[] resizeVals = resize.split("x");
            int width = Integer.parseInt(resizeVals[0]);
            int height = Integer.parseInt(resizeVals[1]);
            res.putResizeWidth(width);
            res.putResizeHeight(height);
        }

        // Visibility begin
        String visibilityBegin = vals[10];
        if (!EMPTY.equals(visibilityBegin)) {
            String[] visibilityBeginVals = visibilityBegin.split(":");
            int beginHour = Integer.parseInt(visibilityBeginVals[0]);
            int beginMin = Integer.parseInt(visibilityBeginVals[1]);
            res.putVisibilityBeginHour(beginHour);
            res.putVisibilityBeginMin(beginMin);
        }

        // Visibility end
        String visibilityEnd = vals[11];
        if (!EMPTY.equals(visibilityEnd)) {
            String[] visibilityEndVals = visibilityEnd.split(":");
            int endHour = Integer.parseInt(visibilityEndVals[0]);
            int endMin = Integer.parseInt(visibilityEndVals[1]);
            res.putVisibilityEndHour(endHour);
            res.putVisibilityEndMin(endMin);
        }

        // Coordinates
        String coordinates = vals[12];
        if (!EMPTY.equals(coordinates)) {
            res.putCoordinates(coordinates);
        }

        res.putType(WebcamType.SERVER);
    }

    @Background(Type.DISK)
    public void insertUserWebcam(Context context, String name, String url) {
        WebcamContentValues values = new WebcamContentValues();
        values.putPublicId("user_" + System.currentTimeMillis());
        values.putName(name);
        values.putUrl(HTTP + url);
        values.putAddedDate(System.currentTimeMillis());
        values.putType(WebcamType.USER);
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.insert(WebcamColumns.CONTENT_URI, values.values());
    }

    @Background(Type.DISK)
    public String getPublicId(Context context, long webcamId) {
        if (webcamId == Constants.WEBCAM_ID_RANDOM) return "RANDOM";
        if (webcamId == Constants.WEBCAM_ID_NONE) return "NONE"; // Should never happen

        String[] projection = { WebcamColumns.PUBLIC_ID };
        WebcamSelection where = new WebcamSelection().id(webcamId);
        WebcamCursor cursor = where.query(context.getContentResolver(), projection);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                Log.w(TAG, "getDownloadInfo Could not find webcam with webcamId=" + webcamId);
                return "Unknown";
            }
            return cursor.getPublicId();
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
