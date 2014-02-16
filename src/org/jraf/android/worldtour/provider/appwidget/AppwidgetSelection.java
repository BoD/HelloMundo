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
package org.jraf.android.worldtour.provider.appwidget;

import java.util.Date;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.jraf.android.worldtour.provider.base.AbstractSelection;

/**
 * Selection for the {@code appwidget} table.
 */
public class AppwidgetSelection extends AbstractSelection<AppwidgetSelection> {
    @Override
    public Uri uri() {
        return AppwidgetColumns.CONTENT_URI;
    }
    
    /**
     * Query the given content resolver using this selection.
     * 
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort
     *            order, which may be unordered.
     * @return A {@code AppwidgetCursor} object, which is positioned before the first entry, or null.
     */
    public AppwidgetCursor query(ContentResolver contentResolver, String[] projection, String sortOrder) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), sortOrder);
        if (cursor == null) return null;
        return new AppwidgetCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null}.
     */
    public AppwidgetCursor query(ContentResolver contentResolver, String[] projection) {
        return query(contentResolver, projection, null);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, projection, null, null}.
     */
    public AppwidgetCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null, null);
    }
    
    
    public AppwidgetSelection id(long... value) {
        addEquals(AppwidgetColumns._ID, toObjectArray(value));
        return this;
    }

    public AppwidgetSelection appwidgetId(int... value) {
        addEquals(AppwidgetColumns.APPWIDGET_ID, toObjectArray(value));
        return this;
    }
    
    public AppwidgetSelection appwidgetIdNot(int... value) {
        addNotEquals(AppwidgetColumns.APPWIDGET_ID, toObjectArray(value));
        return this;
    }

    public AppwidgetSelection appwidgetIdGt(int value) {
        addGreaterThan(AppwidgetColumns.APPWIDGET_ID, value);
        return this;
    }

    public AppwidgetSelection appwidgetIdGtEq(int value) {
        addGreaterThanOrEquals(AppwidgetColumns.APPWIDGET_ID, value);
        return this;
    }

    public AppwidgetSelection appwidgetIdLt(int value) {
        addLessThan(AppwidgetColumns.APPWIDGET_ID, value);
        return this;
    }

    public AppwidgetSelection appwidgetIdLtEq(int value) {
        addLessThanOrEquals(AppwidgetColumns.APPWIDGET_ID, value);
        return this;
    }

    public AppwidgetSelection webcamId(long... value) {
        addEquals(AppwidgetColumns.WEBCAM_ID, toObjectArray(value));
        return this;
    }
    
    public AppwidgetSelection webcamIdNot(long... value) {
        addNotEquals(AppwidgetColumns.WEBCAM_ID, toObjectArray(value));
        return this;
    }

    public AppwidgetSelection webcamIdGt(long value) {
        addGreaterThan(AppwidgetColumns.WEBCAM_ID, value);
        return this;
    }

    public AppwidgetSelection webcamIdGtEq(long value) {
        addGreaterThanOrEquals(AppwidgetColumns.WEBCAM_ID, value);
        return this;
    }

    public AppwidgetSelection webcamIdLt(long value) {
        addLessThan(AppwidgetColumns.WEBCAM_ID, value);
        return this;
    }

    public AppwidgetSelection webcamIdLtEq(long value) {
        addLessThanOrEquals(AppwidgetColumns.WEBCAM_ID, value);
        return this;
    }

    public AppwidgetSelection currentWebcamId(Long... value) {
        addEquals(AppwidgetColumns.CURRENT_WEBCAM_ID, value);
        return this;
    }
    
    public AppwidgetSelection currentWebcamIdNot(Long... value) {
        addNotEquals(AppwidgetColumns.CURRENT_WEBCAM_ID, value);
        return this;
    }

    public AppwidgetSelection currentWebcamIdGt(long value) {
        addGreaterThan(AppwidgetColumns.CURRENT_WEBCAM_ID, value);
        return this;
    }

    public AppwidgetSelection currentWebcamIdGtEq(long value) {
        addGreaterThanOrEquals(AppwidgetColumns.CURRENT_WEBCAM_ID, value);
        return this;
    }

    public AppwidgetSelection currentWebcamIdLt(long value) {
        addLessThan(AppwidgetColumns.CURRENT_WEBCAM_ID, value);
        return this;
    }

    public AppwidgetSelection currentWebcamIdLtEq(long value) {
        addLessThanOrEquals(AppwidgetColumns.CURRENT_WEBCAM_ID, value);
        return this;
    }
}
