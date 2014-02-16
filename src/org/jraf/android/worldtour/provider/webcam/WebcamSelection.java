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
package org.jraf.android.worldtour.provider.webcam;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.jraf.android.worldtour.provider.base.AbstractSelection;

/**
 * Selection for the {@code webcam} table.
 */
public class WebcamSelection extends AbstractSelection<WebcamSelection> {
    @Override
    public Uri uri() {
        return WebcamColumns.CONTENT_URI;
    }
    
    /**
     * Query the given content resolver using this selection.
     * 
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code WebcamCursor} object, which is positioned before the first entry, or null.
     */
    public WebcamCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new WebcamCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null}.
     */
    public WebcamCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null}.
     */
    public WebcamCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }
    
    
    public WebcamSelection id(long... value) {
        addEquals(WebcamColumns._ID, toObjectArray(value));
        return this;
    }

    public WebcamSelection type(WebcamType... value) {
        addEquals(WebcamColumns.TYPE, value);
        return this;
    }
    
    public WebcamSelection typeNot(WebcamType... value) {
        addNotEquals(WebcamColumns.TYPE, value);
        return this;
    }


    public WebcamSelection publicId(String... value) {
        addEquals(WebcamColumns.PUBLIC_ID, value);
        return this;
    }
    
    public WebcamSelection publicIdNot(String... value) {
        addNotEquals(WebcamColumns.PUBLIC_ID, value);
        return this;
    }


    public WebcamSelection name(String... value) {
        addEquals(WebcamColumns.NAME, value);
        return this;
    }
    
    public WebcamSelection nameNot(String... value) {
        addNotEquals(WebcamColumns.NAME, value);
        return this;
    }


    public WebcamSelection location(String... value) {
        addEquals(WebcamColumns.LOCATION, value);
        return this;
    }
    
    public WebcamSelection locationNot(String... value) {
        addNotEquals(WebcamColumns.LOCATION, value);
        return this;
    }


    public WebcamSelection url(String... value) {
        addEquals(WebcamColumns.URL, value);
        return this;
    }
    
    public WebcamSelection urlNot(String... value) {
        addNotEquals(WebcamColumns.URL, value);
        return this;
    }


    public WebcamSelection thumbUrl(String... value) {
        addEquals(WebcamColumns.THUMB_URL, value);
        return this;
    }
    
    public WebcamSelection thumbUrlNot(String... value) {
        addNotEquals(WebcamColumns.THUMB_URL, value);
        return this;
    }


    public WebcamSelection sourceUrl(String... value) {
        addEquals(WebcamColumns.SOURCE_URL, value);
        return this;
    }
    
    public WebcamSelection sourceUrlNot(String... value) {
        addNotEquals(WebcamColumns.SOURCE_URL, value);
        return this;
    }


    public WebcamSelection httpReferer(String... value) {
        addEquals(WebcamColumns.HTTP_REFERER, value);
        return this;
    }
    
    public WebcamSelection httpRefererNot(String... value) {
        addNotEquals(WebcamColumns.HTTP_REFERER, value);
        return this;
    }


    public WebcamSelection timezone(String... value) {
        addEquals(WebcamColumns.TIMEZONE, value);
        return this;
    }
    
    public WebcamSelection timezoneNot(String... value) {
        addNotEquals(WebcamColumns.TIMEZONE, value);
        return this;
    }


    public WebcamSelection resizeWidth(Integer... value) {
        addEquals(WebcamColumns.RESIZE_WIDTH, value);
        return this;
    }
    
    public WebcamSelection resizeWidthNot(Integer... value) {
        addNotEquals(WebcamColumns.RESIZE_WIDTH, value);
        return this;
    }

    public WebcamSelection resizeWidthGt(int value) {
        addGreaterThan(WebcamColumns.RESIZE_WIDTH, value);
        return this;
    }

    public WebcamSelection resizeWidthGtEq(int value) {
        addGreaterThanOrEquals(WebcamColumns.RESIZE_WIDTH, value);
        return this;
    }

    public WebcamSelection resizeWidthLt(int value) {
        addLessThan(WebcamColumns.RESIZE_WIDTH, value);
        return this;
    }

    public WebcamSelection resizeWidthLtEq(int value) {
        addLessThanOrEquals(WebcamColumns.RESIZE_WIDTH, value);
        return this;
    }

    public WebcamSelection resizeHeight(Integer... value) {
        addEquals(WebcamColumns.RESIZE_HEIGHT, value);
        return this;
    }
    
    public WebcamSelection resizeHeightNot(Integer... value) {
        addNotEquals(WebcamColumns.RESIZE_HEIGHT, value);
        return this;
    }

    public WebcamSelection resizeHeightGt(int value) {
        addGreaterThan(WebcamColumns.RESIZE_HEIGHT, value);
        return this;
    }

    public WebcamSelection resizeHeightGtEq(int value) {
        addGreaterThanOrEquals(WebcamColumns.RESIZE_HEIGHT, value);
        return this;
    }

    public WebcamSelection resizeHeightLt(int value) {
        addLessThan(WebcamColumns.RESIZE_HEIGHT, value);
        return this;
    }

    public WebcamSelection resizeHeightLtEq(int value) {
        addLessThanOrEquals(WebcamColumns.RESIZE_HEIGHT, value);
        return this;
    }

    public WebcamSelection visibilityBeginHour(Integer... value) {
        addEquals(WebcamColumns.VISIBILITY_BEGIN_HOUR, value);
        return this;
    }
    
    public WebcamSelection visibilityBeginHourNot(Integer... value) {
        addNotEquals(WebcamColumns.VISIBILITY_BEGIN_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityBeginHourGt(int value) {
        addGreaterThan(WebcamColumns.VISIBILITY_BEGIN_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityBeginHourGtEq(int value) {
        addGreaterThanOrEquals(WebcamColumns.VISIBILITY_BEGIN_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityBeginHourLt(int value) {
        addLessThan(WebcamColumns.VISIBILITY_BEGIN_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityBeginHourLtEq(int value) {
        addLessThanOrEquals(WebcamColumns.VISIBILITY_BEGIN_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityBeginMin(Integer... value) {
        addEquals(WebcamColumns.VISIBILITY_BEGIN_MIN, value);
        return this;
    }
    
    public WebcamSelection visibilityBeginMinNot(Integer... value) {
        addNotEquals(WebcamColumns.VISIBILITY_BEGIN_MIN, value);
        return this;
    }

    public WebcamSelection visibilityBeginMinGt(int value) {
        addGreaterThan(WebcamColumns.VISIBILITY_BEGIN_MIN, value);
        return this;
    }

    public WebcamSelection visibilityBeginMinGtEq(int value) {
        addGreaterThanOrEquals(WebcamColumns.VISIBILITY_BEGIN_MIN, value);
        return this;
    }

    public WebcamSelection visibilityBeginMinLt(int value) {
        addLessThan(WebcamColumns.VISIBILITY_BEGIN_MIN, value);
        return this;
    }

    public WebcamSelection visibilityBeginMinLtEq(int value) {
        addLessThanOrEquals(WebcamColumns.VISIBILITY_BEGIN_MIN, value);
        return this;
    }

    public WebcamSelection visibilityEndHour(Integer... value) {
        addEquals(WebcamColumns.VISIBILITY_END_HOUR, value);
        return this;
    }
    
    public WebcamSelection visibilityEndHourNot(Integer... value) {
        addNotEquals(WebcamColumns.VISIBILITY_END_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityEndHourGt(int value) {
        addGreaterThan(WebcamColumns.VISIBILITY_END_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityEndHourGtEq(int value) {
        addGreaterThanOrEquals(WebcamColumns.VISIBILITY_END_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityEndHourLt(int value) {
        addLessThan(WebcamColumns.VISIBILITY_END_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityEndHourLtEq(int value) {
        addLessThanOrEquals(WebcamColumns.VISIBILITY_END_HOUR, value);
        return this;
    }

    public WebcamSelection visibilityEndMin(Integer... value) {
        addEquals(WebcamColumns.VISIBILITY_END_MIN, value);
        return this;
    }
    
    public WebcamSelection visibilityEndMinNot(Integer... value) {
        addNotEquals(WebcamColumns.VISIBILITY_END_MIN, value);
        return this;
    }

    public WebcamSelection visibilityEndMinGt(int value) {
        addGreaterThan(WebcamColumns.VISIBILITY_END_MIN, value);
        return this;
    }

    public WebcamSelection visibilityEndMinGtEq(int value) {
        addGreaterThanOrEquals(WebcamColumns.VISIBILITY_END_MIN, value);
        return this;
    }

    public WebcamSelection visibilityEndMinLt(int value) {
        addLessThan(WebcamColumns.VISIBILITY_END_MIN, value);
        return this;
    }

    public WebcamSelection visibilityEndMinLtEq(int value) {
        addLessThanOrEquals(WebcamColumns.VISIBILITY_END_MIN, value);
        return this;
    }

    public WebcamSelection addedDate(long... value) {
        addEquals(WebcamColumns.ADDED_DATE, toObjectArray(value));
        return this;
    }
    
    public WebcamSelection addedDateNot(long... value) {
        addNotEquals(WebcamColumns.ADDED_DATE, toObjectArray(value));
        return this;
    }

    public WebcamSelection addedDateGt(long value) {
        addGreaterThan(WebcamColumns.ADDED_DATE, value);
        return this;
    }

    public WebcamSelection addedDateGtEq(long value) {
        addGreaterThanOrEquals(WebcamColumns.ADDED_DATE, value);
        return this;
    }

    public WebcamSelection addedDateLt(long value) {
        addLessThan(WebcamColumns.ADDED_DATE, value);
        return this;
    }

    public WebcamSelection addedDateLtEq(long value) {
        addLessThanOrEquals(WebcamColumns.ADDED_DATE, value);
        return this;
    }

    public WebcamSelection excludeRandom(Boolean... value) {
        addEquals(WebcamColumns.EXCLUDE_RANDOM, value);
        return this;
    }
    
    public WebcamSelection excludeRandomNot(Boolean... value) {
        addNotEquals(WebcamColumns.EXCLUDE_RANDOM, value);
        return this;
    }


    public WebcamSelection coordinates(String... value) {
        addEquals(WebcamColumns.COORDINATES, value);
        return this;
    }
    
    public WebcamSelection coordinatesNot(String... value) {
        addNotEquals(WebcamColumns.COORDINATES, value);
        return this;
    }

}
