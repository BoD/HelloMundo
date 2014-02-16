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

import android.database.Cursor;

import org.jraf.android.worldtour.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code appwidget} table.
 */
public class AppwidgetCursor extends AbstractCursor {
    public AppwidgetCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Get the {@code appwidget_id} value.
     */
    public int getAppwidgetId() {
        return getIntegerOrNull(AppwidgetColumns.APPWIDGET_ID);
    }

    /**
     * Get the {@code webcam_id} value.
     */
    public long getWebcamId() {
        return getLongOrNull(AppwidgetColumns.WEBCAM_ID);
    }

    /**
     * Get the {@code current_webcam_id} value.
     * Can be {@code null}.
     */
    public Long getCurrentWebcamId() {
        return getLongOrNull(AppwidgetColumns.CURRENT_WEBCAM_ID);
    }
}
