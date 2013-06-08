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

import java.util.HashMap;

import android.database.Cursor;
import android.database.CursorWrapper;

public class AppwidgetCursorWrapper extends CursorWrapper {
	private HashMap<String, Integer> mColumnIndexes = new HashMap<String, Integer>();
	
    public AppwidgetCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Long getId() {
        Integer index = mColumnIndexes.get(AppwidgetColumns._ID);
        if (index == null) {
        	index = getColumnIndexOrThrow(AppwidgetColumns._ID);
        	mColumnIndexes.put(AppwidgetColumns._ID, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getAppwidgetId() {
        Integer index = mColumnIndexes.get(AppwidgetColumns.APPWIDGET_ID);
        if (index == null) {
        	index = getColumnIndexOrThrow(AppwidgetColumns.APPWIDGET_ID);
        	mColumnIndexes.put(AppwidgetColumns.APPWIDGET_ID, index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public String getWebcamId() {
        Integer index = mColumnIndexes.get(AppwidgetColumns.WEBCAM_ID);
        if (index == null) {
        	index = getColumnIndexOrThrow(AppwidgetColumns.WEBCAM_ID);
        	mColumnIndexes.put(AppwidgetColumns.WEBCAM_ID, index);
        }
        return getString(index);
    }

    public String getCurrentWebcamId() {
        Integer index = mColumnIndexes.get(AppwidgetColumns.CURRENT_WEBCAM_ID);
        if (index == null) {
        	index = getColumnIndexOrThrow(AppwidgetColumns.CURRENT_WEBCAM_ID);
        	mColumnIndexes.put(AppwidgetColumns.CURRENT_WEBCAM_ID, index);
        }
        return getString(index);
    }
}
