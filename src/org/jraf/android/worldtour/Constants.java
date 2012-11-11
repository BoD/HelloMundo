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
package org.jraf.android.worldtour;

import java.util.HashSet;

public class Constants {

    public static final String TAG = "WorldTour/";

    public static final String WEBCAM_PUBLIC_ID_EARTH_AMERICA = "3";
    public static final String WEBCAM_PUBLIC_ID_EARTH_EUROPE = "4";
    public static final String WEBCAM_PUBLIC_ID_SUN = "23";
    public static final String WEBCAM_PUBLIC_ID_TRUCK = "63";
    public static final String WEBCAM_PUBLIC_ID_EIFFEL_TOWER = "0";

    public static final long WEBCAM_ID_RANDOM = -1;


    public static final long INTERVAL_1_MINUTE = 60 * 1000;
    public static final long INTERVAL_10_MINUTES = 10 * 60 * 1000;
    public static final long INTERVAL_20_MINUTES = 20 * 60 * 1000;
    public static final long INTERVAL_30_MINUTES = 30 * 60 * 1000;
    public static final long INTERVAL_1_HOUR = 60 * 60 * 1000;

    public static final HashSet<String> SPECIAL_CAMS = new HashSet<String>(4);

    public static final String PREF_FIRST_RUN = "PREF_FIRST_RUN";

    public static final String PREF_AUTO_UPDATE_WALLPAPER = "PREF_AUTO_UPDATE_WALLPAPER";
    public static final boolean PREF_AUTO_UPDATE_WALLPAPER_DEFAULT = false;

    public static final String PREF_SELECTED_WEBCAM_ID = "PREF_SELECTED_WEBCAM_ID";
    /**
     * This constant assumes that the first element (index 0) in the database is always the Eiffel Tower.
     */
    public static final long PREF_SELECTED_WEBCAM_ID_DEFAULT = 1;

    public static final String PREF_CURRENT_WEBCAM_ID = "PREF_CURRENT_WEBCAM_ID";

    public static final String PREF_UPDATE_INTERVAL = "PREF_UPDATE_INTERVAL";
    public static final String PREF_UPDATE_INTERVAL_DEFAULT = String.valueOf(INTERVAL_30_MINUTES);

    public static final String PREF_WALLPAPER_CHANGED_INTERNAL = "PREF_WALLPAPER_CHANGED_INTERNAL";
    public static final boolean PREF_WALLPAPER_CHANGED_INTERNAL_DEFAULT = false;

    public static final String PREF_DATABASE_LAST_DOWNLOAD = "PREF_DATABASE_LAST_DOWNLOAD";

    public static final String PREF_DIMMED = "PREF_DIMMED";
    public static final boolean PREF_DIMMED_DEFAULT = false;

    public static final String PREF_AVOID_NIGHT = "PREF_AVOID_NIGHT";
    public static final boolean PREF_AVOID_NIGHT_DEFAULT = true;

    public static final String PREF_WELCOME_RESUME_INDEX = "PREF_WELCOME_RESUME_INDEX";
    public static final String PREF_SEEN_WELCOME = "PREF_SEEN_WELCOME";

    public static final String FILE_IMAGE = "image";
    public static final String FILE_IMAGE_DIMMED = "image_dimmed";

    static {
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_EARTH_AMERICA);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_EARTH_EUROPE);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_SUN);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_TRUCK);
    }

    public static final String FRAGMENT_DIALOG = "FRAGMENT_DIALOG";

}
