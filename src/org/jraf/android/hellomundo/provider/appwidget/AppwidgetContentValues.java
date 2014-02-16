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
package org.jraf.android.hellomundo.provider.appwidget;

import java.util.Date;

import android.content.ContentResolver;
import android.net.Uri;

import org.jraf.android.hellomundo.provider.base.AbstractContentValues;

/**
 * Content values wrapper for the {@code appwidget} table.
 */
public class AppwidgetContentValues extends AbstractContentValues {
    @Override
    public Uri uri() {
        return AppwidgetColumns.CONTENT_URI;
    }

    /**
     * Update row(s) using the values stored by this object and the given selection.
     * 
     * @param contentResolver The content resolver to use.
     * @param where The selection to use (can be {@code null}).
     */
    public int update(ContentResolver contentResolver, AppwidgetSelection where) {
        return contentResolver.update(uri(), values(), where == null ? null : where.sel(), where == null ? null : where.args());
    }

    public AppwidgetContentValues putAppwidgetId(int value) {
        mContentValues.put(AppwidgetColumns.APPWIDGET_ID, value);
        return this;
    }



    public AppwidgetContentValues putWebcamId(long value) {
        mContentValues.put(AppwidgetColumns.WEBCAM_ID, value);
        return this;
    }



    public AppwidgetContentValues putCurrentWebcamId(Long value) {
        mContentValues.put(AppwidgetColumns.CURRENT_WEBCAM_ID, value);
        return this;
    }

    public AppwidgetContentValues putCurrentWebcamIdNull() {
        mContentValues.putNull(AppwidgetColumns.CURRENT_WEBCAM_ID);
        return this;
    }

}
