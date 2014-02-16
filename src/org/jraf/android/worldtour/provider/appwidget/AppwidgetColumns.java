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

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.worldtour.provider.WorldtourProvider;

/**
 * Columns for the {@code appwidget} table.
 */
public interface AppwidgetColumns extends BaseColumns {
    String TABLE_NAME = "appwidget";
    Uri CONTENT_URI = Uri.parse(WorldtourProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    String _ID = BaseColumns._ID;
    String APPWIDGET_ID = "appwidget_id";
    String WEBCAM_ID = "webcam_id";
    String CURRENT_WEBCAM_ID = "current_webcam_id";

    String DEFAULT_ORDER = _ID;

	// @formatter:off
    String[] FULL_PROJECTION = new String[] {
            _ID,
            APPWIDGET_ID,
            WEBCAM_ID,
            CURRENT_WEBCAM_ID
    };
    // @formatter:on
}