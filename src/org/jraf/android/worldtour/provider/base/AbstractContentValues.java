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
package org.jraf.android.worldtour.provider.base;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public abstract class AbstractContentValues {
    protected ContentValues mContentValues = new ContentValues();

    /**
     * Returns the {@code uri} argument to pass to the {@code ContentResolver} methods.
     */
    public abstract Uri uri();

    /**
     * Returns the {@code ContentValues} wrapped by this object.
     */
    public ContentValues values() {
        return mContentValues;
    }

    /**
     * Inserts a row into a table using the values stored by this object.
     * 
     * @param contentResolver The content resolver to use.
     */
    public Uri insert(ContentResolver contentResolver) {
        return contentResolver.insert(uri(), values());
    }
}