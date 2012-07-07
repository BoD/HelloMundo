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


    public static final HashSet<String> SPECIAL_CAMS = new HashSet<String>(4);

    public static final String PREF_FIRST_RUN = "KEY_FIRST_RUN";
    public static final String PREF_SERVICE_ENABLED = "PREF_SERVICE_ENABLED";
    public static final boolean PREF_SERVICE_ENABLED_DEFAULT = false;
    public static final String PREF_WEBCAM_PUBLIC_ID = "PREF_WEBCAM_PUBLIC_ID";
    public static final String PREF_WEBCAM_PUBLIC_ID_DEFAULT = WEBCAM_PUBLIC_ID_EIFFEL_TOWER;
    public static final String PREF_WEBCAM_PUBLIC_ID_RANDOM = "RANDOM";

    static {
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_EARTH_AMERICA);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_EARTH_EUROPE);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_SUN);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_TRUCK);
    }
}
