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

import android.net.Uri;
import android.provider.BaseColumns;

public class AppwidgetColumns implements BaseColumns {
    public static final String TABLE_NAME = "appwidget";
    public static final Uri CONTENT_URI = Uri.parse(WorldtourProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;

    public static final String APPWIDGET_ID = "appwidget_id";
    public static final String WEBCAM_ID = "webcam_id";
    public static final String CURRENT_WEBCAM_ID = "current_webcam_id";

    public static final String DEFAULT_ORDER = _ID;
}