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
package org.jraf.android.hellomundo.provider.appwidget;

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.hellomundo.provider.HelloMundoProvider;

/**
 * Columns for the {@code appwidget} table.
 */
public interface AppwidgetColumns extends BaseColumns {
    String TABLE_NAME = "appwidget";
    Uri CONTENT_URI = Uri.parse(HelloMundoProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

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