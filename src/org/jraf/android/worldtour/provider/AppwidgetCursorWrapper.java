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
        Integer index = mColumnIndexes.get("_id");
        if (index == null) {
        	index = getColumnIndexOrThrow("_id");
        	mColumnIndexes.put("type", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public Long getAppwidgetId() {
        Integer index = mColumnIndexes.get("appwidget_id");
        if (index == null) {
        	index = getColumnIndexOrThrow("appwidget_id");
        	mColumnIndexes.put("appwidget_id", index);
        }
        if (isNull(index)) return null;
        return getLong(index);
    }

    public String getWebcamId() {
        Integer index = mColumnIndexes.get("webcam_id");
        if (index == null) {
        	index = getColumnIndexOrThrow("webcam_id");
        	mColumnIndexes.put("webcam_id", index);
        }
        return getString(index);
    }

    public String getCurrentWebcamId() {
        Integer index = mColumnIndexes.get("current_webcam_id");
        if (index == null) {
        	index = getColumnIndexOrThrow("current_webcam_id");
        	mColumnIndexes.put("current_webcam_id", index);
        }
        return getString(index);
    }
}
