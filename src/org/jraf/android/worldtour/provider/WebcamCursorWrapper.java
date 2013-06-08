/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright 2013 Benoit 'BoD' Lubek (BoD@JRAF.org).  All Rights Reserved.
 */
package org.jraf.android.worldtour.provider;

import java.util.HashMap;

import android.database.Cursor;
import android.database.CursorWrapper;

public class WebcamCursorWrapper extends CursorWrapper {
	private HashMap<String, Integer> mColumnIndexes = new HashMap<String, Integer>();
	
    public WebcamCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Long getId() {
        Integer index = mColumnIndexes.get(WebcamColumns._ID);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns._ID);
        	mColumnIndexes.put(WebcamColumns._ID, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getType() {
        Integer index = mColumnIndexes.get(WebcamColumns.TYPE);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.TYPE);
        	mColumnIndexes.put(WebcamColumns.TYPE, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public String getPublicId() {
        Integer index = mColumnIndexes.get(WebcamColumns.PUBLIC_ID);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.PUBLIC_ID);
        	mColumnIndexes.put(WebcamColumns.PUBLIC_ID, index);
        }
        return getString(index);
    }

    public String getName() {
        Integer index = mColumnIndexes.get(WebcamColumns.NAME);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.NAME);
        	mColumnIndexes.put(WebcamColumns.NAME, index);
        }
        return getString(index);
    }

    public String getLocation() {
        Integer index = mColumnIndexes.get(WebcamColumns.LOCATION);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.LOCATION);
        	mColumnIndexes.put(WebcamColumns.LOCATION, index);
        }
        return getString(index);
    }

    public String getUrl() {
        Integer index = mColumnIndexes.get(WebcamColumns.URL);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.URL);
        	mColumnIndexes.put(WebcamColumns.URL, index);
        }
        return getString(index);
    }

    public String getThumbUrl() {
        Integer index = mColumnIndexes.get(WebcamColumns.THUMB_URL);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.THUMB_URL);
        	mColumnIndexes.put(WebcamColumns.THUMB_URL, index);
        }
        return getString(index);
    }

    public String getSourceUrl() {
        Integer index = mColumnIndexes.get(WebcamColumns.SOURCE_URL);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.SOURCE_URL);
        	mColumnIndexes.put(WebcamColumns.SOURCE_URL, index);
        }
        return getString(index);
    }

    public String getHttpReferer() {
        Integer index = mColumnIndexes.get(WebcamColumns.HTTP_REFERER);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.HTTP_REFERER);
        	mColumnIndexes.put(WebcamColumns.HTTP_REFERER, index);
        }
        return getString(index);
    }

    public String getTimezone() {
        Integer index = mColumnIndexes.get(WebcamColumns.TIMEZONE);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.TIMEZONE);
        	mColumnIndexes.put(WebcamColumns.TIMEZONE, index);
        }
        return getString(index);
    }

    public Long getResizeWidth() {
        Integer index = mColumnIndexes.get(WebcamColumns.RESIZE_WIDTH);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.RESIZE_WIDTH);
        	mColumnIndexes.put(WebcamColumns.RESIZE_WIDTH, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getResizeHeight() {
        Integer index = mColumnIndexes.get(WebcamColumns.RESIZE_HEIGHT);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.RESIZE_HEIGHT);
        	mColumnIndexes.put(WebcamColumns.RESIZE_HEIGHT, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getVisibilityBeginHour() {
        Integer index = mColumnIndexes.get(WebcamColumns.VISIBILITY_BEGIN_HOUR);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.VISIBILITY_BEGIN_HOUR);
        	mColumnIndexes.put(WebcamColumns.VISIBILITY_BEGIN_HOUR, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getVisibilityBeginMin() {
        Integer index = mColumnIndexes.get(WebcamColumns.VISIBILITY_BEGIN_MIN);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.VISIBILITY_BEGIN_MIN);
        	mColumnIndexes.put(WebcamColumns.VISIBILITY_BEGIN_MIN, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getVisibilityEndHour() {
        Integer index = mColumnIndexes.get(WebcamColumns.VISIBILITY_END_HOUR);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.VISIBILITY_END_HOUR);
        	mColumnIndexes.put(WebcamColumns.VISIBILITY_END_HOUR, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getVisibilityEndMin() {
        Integer index = mColumnIndexes.get(WebcamColumns.VISIBILITY_END_MIN);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.VISIBILITY_END_MIN);
        	mColumnIndexes.put(WebcamColumns.VISIBILITY_END_MIN, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getAddedDate() {
        Integer index = mColumnIndexes.get(WebcamColumns.ADDED_DATE);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.ADDED_DATE);
        	mColumnIndexes.put(WebcamColumns.ADDED_DATE, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getExcludeRandom() {
        Integer index = mColumnIndexes.get(WebcamColumns.EXCLUDE_RANDOM);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.EXCLUDE_RANDOM);
        	mColumnIndexes.put(WebcamColumns.EXCLUDE_RANDOM, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public String getCoordinates() {
        Integer index = mColumnIndexes.get(WebcamColumns.COORDINATES);
        if (index == null) {
        	index = getColumnIndexOrThrow(WebcamColumns.COORDINATES);
        	mColumnIndexes.put(WebcamColumns.COORDINATES, index);
        }
        return getString(index);
    }
}
