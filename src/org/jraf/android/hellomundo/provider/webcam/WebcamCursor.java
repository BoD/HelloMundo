/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2009-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.hellomundo.provider.webcam;

import android.database.Cursor;

import org.jraf.android.hellomundo.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code webcam} table.
 */
public class WebcamCursor extends AbstractCursor {
    public WebcamCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Get the {@code type} value.
     * Cannot be {@code null}.
     */
    public WebcamType getType() {
        Integer intValue = getIntegerOrNull(WebcamColumns.TYPE);
        if (intValue == null) return null;
        return WebcamType.values()[intValue];
    }

    /**
     * Get the {@code public_id} value.
     * Can be {@code null}.
     */
    public String getPublicId() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.PUBLIC_ID);
        return getString(index);
    }

    /**
     * Get the {@code name} value.
     * Cannot be {@code null}.
     */
    public String getName() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.NAME);
        return getString(index);
    }

    /**
     * Get the {@code location} value.
     * Can be {@code null}.
     */
    public String getLocation() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.LOCATION);
        return getString(index);
    }

    /**
     * Get the {@code url} value.
     * Cannot be {@code null}.
     */
    public String getUrl() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.URL);
        return getString(index);
    }

    /**
     * Get the {@code thumb_url} value.
     * Can be {@code null}.
     */
    public String getThumbUrl() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.THUMB_URL);
        return getString(index);
    }

    /**
     * Get the {@code source_url} value.
     * Can be {@code null}.
     */
    public String getSourceUrl() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.SOURCE_URL);
        return getString(index);
    }

    /**
     * Get the {@code http_referer} value.
     * Can be {@code null}.
     */
    public String getHttpReferer() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.HTTP_REFERER);
        return getString(index);
    }

    /**
     * Get the {@code timezone} value.
     * Can be {@code null}.
     */
    public String getTimezone() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.TIMEZONE);
        return getString(index);
    }

    /**
     * Get the {@code resize_width} value.
     * Can be {@code null}.
     */
    public Integer getResizeWidth() {
        return getIntegerOrNull(WebcamColumns.RESIZE_WIDTH);
    }

    /**
     * Get the {@code resize_height} value.
     * Can be {@code null}.
     */
    public Integer getResizeHeight() {
        return getIntegerOrNull(WebcamColumns.RESIZE_HEIGHT);
    }

    /**
     * Get the {@code visibility_begin_hour} value.
     * Can be {@code null}.
     */
    public Integer getVisibilityBeginHour() {
        return getIntegerOrNull(WebcamColumns.VISIBILITY_BEGIN_HOUR);
    }

    /**
     * Get the {@code visibility_begin_min} value.
     * Can be {@code null}.
     */
    public Integer getVisibilityBeginMin() {
        return getIntegerOrNull(WebcamColumns.VISIBILITY_BEGIN_MIN);
    }

    /**
     * Get the {@code visibility_end_hour} value.
     * Can be {@code null}.
     */
    public Integer getVisibilityEndHour() {
        return getIntegerOrNull(WebcamColumns.VISIBILITY_END_HOUR);
    }

    /**
     * Get the {@code visibility_end_min} value.
     * Can be {@code null}.
     */
    public Integer getVisibilityEndMin() {
        return getIntegerOrNull(WebcamColumns.VISIBILITY_END_MIN);
    }

    /**
     * Get the {@code added_date} value.
     */
    public long getAddedDate() {
        return getLongOrNull(WebcamColumns.ADDED_DATE);
    }

    /**
     * Get the {@code exclude_random} value.
     * Can be {@code null}.
     */
    public Boolean getExcludeRandom() {
        return getBoolean(WebcamColumns.EXCLUDE_RANDOM);
    }

    /**
     * Get the {@code coordinates} value.
     * Can be {@code null}.
     */
    public String getCoordinates() {
        Integer index = getCachedColumnIndexOrThrow(WebcamColumns.COORDINATES);
        return getString(index);
    }
}
