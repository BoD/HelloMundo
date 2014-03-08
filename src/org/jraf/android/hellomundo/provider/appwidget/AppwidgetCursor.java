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

import android.database.Cursor;

import org.jraf.android.hellomundo.provider.base.AbstractCursor;

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
