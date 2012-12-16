package org.jraf.android.worldtour.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class AppwidgetColumns implements BaseColumns {
    public static final String TABLE_NAME = "appwidget";
    public static final Uri CONTENT_URI = Uri.parse(WorldtourProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    public static final String _ID = BaseColumns._ID;

    public static final String APPWIDGET_ID = "appwidget_id";
    public static final String WEBCAM_ID = "webcam_id";

    public static final String DEFAULT_ORDER = _ID;
}