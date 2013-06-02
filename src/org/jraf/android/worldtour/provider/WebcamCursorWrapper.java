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
        Integer index = mColumnIndexes.get("_id");
        if (index == null) {
        	index = getColumnIndexOrThrow("_id");
        	mColumnIndexes.put("type", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getType() {
        Integer index = mColumnIndexes.get("type");
        if (index == null) {
        	index = getColumnIndexOrThrow("type");
        	mColumnIndexes.put("type", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public String getPublicId() {
        Integer index = mColumnIndexes.get("public_id");
        if (index == null) {
        	index = getColumnIndexOrThrow("public_id");
        	mColumnIndexes.put("public_id", index);
        }
        return getString(index);
    }

    public String getName() {
        Integer index = mColumnIndexes.get("name");
        if (index == null) {
        	index = getColumnIndexOrThrow("name");
        	mColumnIndexes.put("name", index);
        }
        return getString(index);
    }

    public String getLocation() {
        Integer index = mColumnIndexes.get("location");
        if (index == null) {
        	index = getColumnIndexOrThrow("location");
        	mColumnIndexes.put("location", index);
        }
        return getString(index);
    }

    public String getUrl() {
        Integer index = mColumnIndexes.get("url");
        if (index == null) {
        	index = getColumnIndexOrThrow("url");
        	mColumnIndexes.put("url", index);
        }
        return getString(index);
    }

    public String getThumbUrl() {
        Integer index = mColumnIndexes.get("thumb_url");
        if (index == null) {
        	index = getColumnIndexOrThrow("thumb_url");
        	mColumnIndexes.put("thumb_url", index);
        }
        return getString(index);
    }

    public String getSourceUrl() {
        Integer index = mColumnIndexes.get("source_url");
        if (index == null) {
        	index = getColumnIndexOrThrow("source_url");
        	mColumnIndexes.put("source_url", index);
        }
        return getString(index);
    }

    public String getHttpReferer() {
        Integer index = mColumnIndexes.get("http_referer");
        if (index == null) {
        	index = getColumnIndexOrThrow("http_referer");
        	mColumnIndexes.put("http_referer", index);
        }
        return getString(index);
    }

    public String getTimezone() {
        Integer index = mColumnIndexes.get("timezone");
        if (index == null) {
        	index = getColumnIndexOrThrow("timezone");
        	mColumnIndexes.put("timezone", index);
        }
        return getString(index);
    }

    public Long getResizeWidth() {
        Integer index = mColumnIndexes.get("resize_width");
        if (index == null) {
        	index = getColumnIndexOrThrow("resize_width");
        	mColumnIndexes.put("resize_width", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getResizeHeight() {
        Integer index = mColumnIndexes.get("resize_height");
        if (index == null) {
        	index = getColumnIndexOrThrow("resize_height");
        	mColumnIndexes.put("resize_height", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getVisibilityBeginHour() {
        Integer index = mColumnIndexes.get("visibility_begin_hour");
        if (index == null) {
        	index = getColumnIndexOrThrow("visibility_begin_hour");
        	mColumnIndexes.put("visibility_begin_hour", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getVisibilityBeginMin() {
        Integer index = mColumnIndexes.get("visibility_begin_min");
        if (index == null) {
        	index = getColumnIndexOrThrow("visibility_begin_min");
        	mColumnIndexes.put("visibility_begin_min", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getVisibilityEndHour() {
        Integer index = mColumnIndexes.get("visibility_end_hour");
        if (index == null) {
        	index = getColumnIndexOrThrow("visibility_end_hour");
        	mColumnIndexes.put("visibility_end_hour", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getVisibilityEndMin() {
        Integer index = mColumnIndexes.get("visibility_end_min");
        if (index == null) {
        	index = getColumnIndexOrThrow("visibility_end_min");
        	mColumnIndexes.put("visibility_end_min", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getAddedDate() {
        Integer index = mColumnIndexes.get("added_date");
        if (index == null) {
        	index = getColumnIndexOrThrow("added_date");
        	mColumnIndexes.put("added_date", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getExcludeRandom() {
        Integer index = mColumnIndexes.get("exclude_random");
        if (index == null) {
        	index = getColumnIndexOrThrow("exclude_random");
        	mColumnIndexes.put("exclude_random", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public String getCoordinates() {
        Integer index = mColumnIndexes.get("coordinates");
        if (index == null) {
        	index = getColumnIndexOrThrow("coordinates");
        	mColumnIndexes.put("coordinates", index);
        }
        return getString(index);
    }
}
