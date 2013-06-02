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

public class WebcamColumns implements BaseColumns {
    public static final String TABLE_NAME = "webcam";
    public static final Uri CONTENT_URI = Uri.parse(WorldtourProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;

    public static final String TYPE = "type";
    public static final String PUBLIC_ID = "public_id";
    public static final String NAME = "name";
    public static final String LOCATION = "location";
    public static final String URL = "url";
    public static final String THUMB_URL = "thumb_url";
    public static final String SOURCE_URL = "source_url";
    public static final String HTTP_REFERER = "http_referer";
    public static final String TIMEZONE = "timezone";
    public static final String RESIZE_WIDTH = "resize_width";
    public static final String RESIZE_HEIGHT = "resize_height";
    public static final String VISIBILITY_BEGIN_HOUR = "visibility_begin_hour";
    public static final String VISIBILITY_BEGIN_MIN = "visibility_begin_min";
    public static final String VISIBILITY_END_HOUR = "visibility_end_hour";
    public static final String VISIBILITY_END_MIN = "visibility_end_min";
    public static final String ADDED_DATE = "added_date";
    public static final String EXCLUDE_RANDOM = "exclude_random";
    public static final String COORDINATES = "coordinates";

    public static final String DEFAULT_ORDER = _ID;
}