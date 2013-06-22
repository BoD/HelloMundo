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

import android.database.Cursor;

/**
 * Cursor wrapper for the {@code webcam} table.
 */
public class WebcamCursorWrapper extends AbstractCursorWrapper {
    public WebcamCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Long getId() {
        return getLongOrNull(WebcamColumns._ID);
    }

    public Long getType() {
        return getLongOrNull(WebcamColumns.TYPE);
    }

    public String getPublicId() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.PUBLIC_ID);
        return getString(index);
    }

    public String getName() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.NAME);
        return getString(index);
    }

    public String getLocation() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.LOCATION);
        return getString(index);
    }

    public String getUrl() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.URL);
        return getString(index);
    }

    public String getThumbUrl() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.THUMB_URL);
        return getString(index);
    }

    public String getSourceUrl() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.SOURCE_URL);
        return getString(index);
    }

    public String getHttpReferer() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.HTTP_REFERER);
        return getString(index);
    }

    public String getTimezone() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.TIMEZONE);
        return getString(index);
    }

    public Long getResizeWidth() {
        return getLongOrNull(WebcamColumns.RESIZE_WIDTH);
    }

    public Long getResizeHeight() {
        return getLongOrNull(WebcamColumns.RESIZE_HEIGHT);
    }

    public Long getVisibilityBeginHour() {
        return getLongOrNull(WebcamColumns.VISIBILITY_BEGIN_HOUR);
    }

    public Long getVisibilityBeginMin() {
        return getLongOrNull(WebcamColumns.VISIBILITY_BEGIN_MIN);
    }

    public Long getVisibilityEndHour() {
        return getLongOrNull(WebcamColumns.VISIBILITY_END_HOUR);
    }

    public Long getVisibilityEndMin() {
        return getLongOrNull(WebcamColumns.VISIBILITY_END_MIN);
    }

    public Long getAddedDate() {
        return getLongOrNull(WebcamColumns.ADDED_DATE);
    }

    public Long getExcludeRandom() {
        return getLongOrNull(WebcamColumns.EXCLUDE_RANDOM);
    }

    public String getCoordinates() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.COORDINATES);
        return getString(index);
    }
}
