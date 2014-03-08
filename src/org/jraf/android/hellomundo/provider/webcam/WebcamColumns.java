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

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.hellomundo.provider.HelloMundoProvider;

/**
 * Columns for the {@code webcam} table.
 */
public interface WebcamColumns extends BaseColumns {
    String TABLE_NAME = "webcam";
    Uri CONTENT_URI = Uri.parse(HelloMundoProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

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