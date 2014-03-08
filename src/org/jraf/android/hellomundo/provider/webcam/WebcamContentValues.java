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

import android.content.ContentResolver;
import android.net.Uri;

import org.jraf.android.hellomundo.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code webcam} table.
 */
public class WebcamContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return WebcamColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     * 
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, WebcamSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public WebcamContentValues putType(WebcamType value) {
        if (value == null) throw new IllegalArgumentException("value for type must not be null");
        mContentValues.put(WebcamColumns.TYPE, value.ordinal());
        return this;
    }



    public WebcamContentValues putPublicId(String value) {
        mContentValues.put(WebcamColumns.PUBLIC_ID, value);
        return this;
    }

    public WebcamContentValues putPublicIdNull() {
        mContentValues.putNull(WebcamColumns.PUBLIC_ID);
        return this;
    }


    public WebcamContentValues putName(String value) {
        if (value == null) throw new IllegalArgumentException("value for name must not be null");
        mContentValues.put(WebcamColumns.NAME, value);
        return this;
    }



    public WebcamContentValues putLocation(String value) {
        mContentValues.put(WebcamColumns.LOCATION, value);
        return this;
    }

    public WebcamContentValues putLocationNull() {
        mContentValues.putNull(WebcamColumns.LOCATION);
        return this;
    }


    public WebcamContentValues putUrl(String value) {
        if (value == null) throw new IllegalArgumentException("value for url must not be null");
        mContentValues.put(WebcamColumns.URL, value);
        return this;
    }



    public WebcamContentValues putThumbUrl(String value) {
        mContentValues.put(WebcamColumns.THUMB_URL, value);
        return this;
    }

    public WebcamContentValues putThumbUrlNull() {
        mContentValues.putNull(WebcamColumns.THUMB_URL);
        return this;
    }


    public WebcamContentValues putSourceUrl(String value) {
        mContentValues.put(WebcamColumns.SOURCE_URL, value);
        return this;
    }

    public WebcamContentValues putSourceUrlNull() {
        mContentValues.putNull(WebcamColumns.SOURCE_URL);
        return this;
    }


    public WebcamContentValues putHttpReferer(String value) {
        mContentValues.put(WebcamColumns.HTTP_REFERER, value);
        return this;
    }

    public WebcamContentValues putHttpRefererNull() {
        mContentValues.putNull(WebcamColumns.HTTP_REFERER);
        return this;
    }


    public WebcamContentValues putTimezone(String value) {
        mContentValues.put(WebcamColumns.TIMEZONE, value);
        return this;
    }

    public WebcamContentValues putTimezoneNull() {
        mContentValues.putNull(WebcamColumns.TIMEZONE);
        return this;
    }


    public WebcamContentValues putResizeWidth(Integer value) {
        mContentValues.put(WebcamColumns.RESIZE_WIDTH, value);
        return this;
    }

    public WebcamContentValues putResizeWidthNull() {
        mContentValues.putNull(WebcamColumns.RESIZE_WIDTH);
        return this;
    }


    public WebcamContentValues putResizeHeight(Integer value) {
        mContentValues.put(WebcamColumns.RESIZE_HEIGHT, value);
        return this;
    }

    public WebcamContentValues putResizeHeightNull() {
        mContentValues.putNull(WebcamColumns.RESIZE_HEIGHT);
        return this;
    }


    public WebcamContentValues putVisibilityBeginHour(Integer value) {
        mContentValues.put(WebcamColumns.VISIBILITY_BEGIN_HOUR, value);
        return this;
    }

    public WebcamContentValues putVisibilityBeginHourNull() {
        mContentValues.putNull(WebcamColumns.VISIBILITY_BEGIN_HOUR);
        return this;
    }


    public WebcamContentValues putVisibilityBeginMin(Integer value) {
        mContentValues.put(WebcamColumns.VISIBILITY_BEGIN_MIN, value);
        return this;
    }

    public WebcamContentValues putVisibilityBeginMinNull() {
        mContentValues.putNull(WebcamColumns.VISIBILITY_BEGIN_MIN);
        return this;
    }


    public WebcamContentValues putVisibilityEndHour(Integer value) {
        mContentValues.put(WebcamColumns.VISIBILITY_END_HOUR, value);
        return this;
    }

    public WebcamContentValues putVisibilityEndHourNull() {
        mContentValues.putNull(WebcamColumns.VISIBILITY_END_HOUR);
        return this;
    }


    public WebcamContentValues putVisibilityEndMin(Integer value) {
        mContentValues.put(WebcamColumns.VISIBILITY_END_MIN, value);
        return this;
    }

    public WebcamContentValues putVisibilityEndMinNull() {
        mContentValues.putNull(WebcamColumns.VISIBILITY_END_MIN);
        return this;
    }


    public WebcamContentValues putAddedDate(long value) {
        mContentValues.put(WebcamColumns.ADDED_DATE, value);
        return this;
    }



    public WebcamContentValues putExcludeRandom(Boolean value) {
        mContentValues.put(WebcamColumns.EXCLUDE_RANDOM, value);
        return this;
    }

    public WebcamContentValues putExcludeRandomNull() {
        mContentValues.putNull(WebcamColumns.EXCLUDE_RANDOM);
        return this;
    }


    public WebcamContentValues putCoordinates(String value) {
        mContentValues.put(WebcamColumns.COORDINATES, value);
        return this;
    }

    public WebcamContentValues putCoordinatesNull() {
        mContentValues.putNull(WebcamColumns.COORDINATES);
        return this;
    }

}
