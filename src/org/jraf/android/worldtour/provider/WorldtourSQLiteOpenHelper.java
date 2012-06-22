package org.jraf.android.worldtour.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;

public class WorldtourSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = Constants.TAG + WorldtourSQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "worldtour";
    private static final int DATABASE_VERSION = 1;

    // @formatter:off
    private static final String SQL_CREATE_TABLE_WEBCAM = "CREATE TABLE IF NOT EXISTS "
            + WebcamColumns.TABLE_NAME + " ( "
            + WebcamColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WebcamColumns.TYPE + " INTEGER, "
            + WebcamColumns.PUBLIC_ID + " TEXT, "
            + WebcamColumns.NAME + " TEXT, "
            + WebcamColumns.LOCATION + " TEXT, "
            + WebcamColumns.URL + " TEXT, "
            + WebcamColumns.THUMB_URL + " TEXT, "
            + WebcamColumns.SOURCE_URL + " TEXT, "
            + WebcamColumns.HTTP_REFERER + " TEXT, "
            + WebcamColumns.TIMEZONE + " TEXT, "
            + WebcamColumns.RESIZE_WIDTH + " INTEGER, "
            + WebcamColumns.RESIZE_HEIGHT + " INTEGER, "
            + WebcamColumns.VISIBILITY_BEGIN_HOUR + " INTEGER, "
            + WebcamColumns.VISIBILITY_BEGIN_MIN + " INTEGER, "
            + WebcamColumns.VISIBILITY_END_HOUR + " INTEGER, "
            + WebcamColumns.VISIBILITY_END_MIN + " INTEGER, "
            + WebcamColumns.ADDED_DATE + " INTEGER, "
            + WebcamColumns.EXCLUDE_RANDOM + " INTEGER, "
            + WebcamColumns.COORDINATES + " TEXT "
            + " );";

    // @formatter:on

    public WorldtourSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (Config.LOGD) Log.d(TAG, "onCreate");
        db.execSQL(SQL_CREATE_TABLE_WEBCAM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (Config.LOGD) Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    }
}
