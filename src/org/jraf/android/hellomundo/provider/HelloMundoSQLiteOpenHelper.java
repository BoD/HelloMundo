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
package org.jraf.android.hellomundo.provider;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.DefaultDatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import org.jraf.android.hellomundo.provider.appwidget.AppwidgetColumns;
import org.jraf.android.hellomundo.provider.webcam.WebcamColumns;
import org.jraf.android.latoureiffel.BuildConfig;

public class HelloMundoSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = HelloMundoSQLiteOpenHelper.class.getSimpleName();

    public static final String DATABASE_FILE_NAME = "worldtour.db";
    private static final int DATABASE_VERSION = 2;

    // @formatter:off
    private static final String SQL_CREATE_TABLE_APPWIDGET = "CREATE TABLE IF NOT EXISTS "
            + AppwidgetColumns.TABLE_NAME + " ( "
            + AppwidgetColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + AppwidgetColumns.APPWIDGET_ID + " INTEGER NOT NULL, "
            + AppwidgetColumns.WEBCAM_ID + " INTEGER NOT NULL, "
            + AppwidgetColumns.CURRENT_WEBCAM_ID + " INTEGER "
            + " );";

    private static final String SQL_CREATE_TABLE_WEBCAM = "CREATE TABLE IF NOT EXISTS "
            + WebcamColumns.TABLE_NAME + " ( "
            + WebcamColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WebcamColumns.TYPE + " INTEGER NOT NULL, "
            + WebcamColumns.PUBLIC_ID + " TEXT, "
            + WebcamColumns.NAME + " TEXT NOT NULL, "
            + WebcamColumns.LOCATION + " TEXT, "
            + WebcamColumns.URL + " TEXT NOT NULL, "
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
            + WebcamColumns.ADDED_DATE + " INTEGER NOT NULL, "
            + WebcamColumns.EXCLUDE_RANDOM + " INTEGER, "
            + WebcamColumns.COORDINATES + " TEXT "
            + " );";

    private static final String SQL_UPDATE_TABLE_APPWIDGET_2  = "ALTER TABLE "
            + AppwidgetColumns.TABLE_NAME + " "
            + "ADD COLUMN " + AppwidgetColumns.CURRENT_WEBCAM_ID + " TEXT "
            + " ;";
    
    // @formatter:on

    public static HelloMundoSQLiteOpenHelper newInstance(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return newInstancePreHoneycomb(context);
        }
        return newInstancePostHoneycomb(context);
    }


    /*
     * Pre Honeycomb.
     */

    private static HelloMundoSQLiteOpenHelper newInstancePreHoneycomb(Context context) {
        return new HelloMundoSQLiteOpenHelper(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
    }

    private HelloMundoSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    /*
     * Post Honeycomb.
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static HelloMundoSQLiteOpenHelper newInstancePostHoneycomb(Context context) {
        return new HelloMundoSQLiteOpenHelper(context, DATABASE_FILE_NAME, null, DATABASE_VERSION, new DefaultDatabaseErrorHandler());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private HelloMundoSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
        db.execSQL(SQL_CREATE_TABLE_APPWIDGET);
        db.execSQL(SQL_CREATE_TABLE_WEBCAM);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        if (newVersion == 2) {
            db.execSQL(SQL_UPDATE_TABLE_APPWIDGET_2);
        }
    }
}
