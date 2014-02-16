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

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.worldtour.provider.WorldtourProvider;

/**
 * Columns for the {@code webcam} table.
 */
public interface WebcamColumns extends BaseColumns {
    String TABLE_NAME = "webcam";
    Uri CONTENT_URI = Uri.parse(WorldtourProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    String _ID = BaseColumns._ID;
    String TYPE = "type";
    String PUBLIC_ID = "public_id";
    String NAME = "name";
    String LOCATION = "location";
    String URL = "url";
    String THUMB_URL = "thumb_url";
    String SOURCE_URL = "source_url";
    String HTTP_REFERER = "http_referer";
    String TIMEZONE = "timezone";
    String RESIZE_WIDTH = "resize_width";
    String RESIZE_HEIGHT = "resize_height";
    String VISIBILITY_BEGIN_HOUR = "visibility_begin_hour";
    String VISIBILITY_BEGIN_MIN = "visibility_begin_min";
    String VISIBILITY_END_HOUR = "visibility_end_hour";
    String VISIBILITY_END_MIN = "visibility_end_min";
    String ADDED_DATE = "added_date";
    String EXCLUDE_RANDOM = "exclude_random";
    String COORDINATES = "coordinates";

    String DEFAULT_ORDER = _ID;

	// @formatter:off
    String[] FULL_PROJECTION = new String[] {
            _ID,
            TYPE,
            PUBLIC_ID,
            NAME,
            LOCATION,
            URL,
            THUMB_URL,
            SOURCE_URL,
            HTTP_REFERER,
            TIMEZONE,
            RESIZE_WIDTH,
            RESIZE_HEIGHT,
            VISIBILITY_BEGIN_HOUR,
            VISIBILITY_BEGIN_MIN,
            VISIBILITY_END_HOUR,
            VISIBILITY_END_MIN,
            ADDED_DATE,
            EXCLUDE_RANDOM,
            COORDINATES
    };
    // @formatter:on
}