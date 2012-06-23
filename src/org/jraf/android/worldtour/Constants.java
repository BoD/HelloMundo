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

    public static final HashSet<String> SPECIAL_CAMS = new HashSet<String>(4);

    static {
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_EARTH_AMERICA);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_EARTH_EUROPE);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_SUN);
        SPECIAL_CAMS.add(WEBCAM_PUBLIC_ID_TRUCK);
    }
}
