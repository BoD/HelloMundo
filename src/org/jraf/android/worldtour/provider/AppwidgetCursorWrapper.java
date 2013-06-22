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
 * Cursor wrapper for the {@code appwidget} table.
 */
public class AppwidgetCursorWrapper extends AbstractCursorWrapper {
    public AppwidgetCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Long getId() {
        return getLongOrNull(AppwidgetColumns._ID);
    }

    public Long getAppwidgetId() {
        return getLongOrNull(AppwidgetColumns.APPWIDGET_ID);
    }

    public String getWebcamId() {
        Integer index = getCachedColumnIndexOrThrow(AppwidgetColumns.WEBCAM_ID);
        return getString(index);
    }

    public String getCurrentWebcamId() {
        Integer index = getCachedColumnIndexOrThrow(AppwidgetColumns.CURRENT_WEBCAM_ID);
        return getString(index);
    }
}
