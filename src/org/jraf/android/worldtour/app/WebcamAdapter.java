/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright 2012 Benoit 'BoD' Lubek (BoD@JRAF.org).  All Rights Reserved.
 */
package org.jraf.android.worldtour.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.worldtour.util.ViewHolder;

public class WebcamAdapter extends ResourceCursorAdapter {
    public WebcamAdapter(Context context) {
        super(context, R.layout.cell_webcam, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final TextView txtName = (TextView) ViewHolder.get(view, R.id.txtName);
        txtName.setText(cursor.getString(1));
    }

}
