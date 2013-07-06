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
package org.jraf.android.worldtour.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.file.FileUtil;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.provider.WebcamType;

public class WebcamInfo {
    public String name;
    public String location;
    public String timeZone;
    public String publicId;
    public String url;
    public String httpReferer;
    public String localBitmapUriStr;
    public int type;

    public String getShareText(Context context) {
        String date = DateFormat.getDateFormat(context).format(new Date()) + ", ";
        boolean specialCam = Constants.SPECIAL_CAMS.contains(publicId) || type == WebcamType.USER;
        if (!specialCam) {
            date += DateTimeUtil.getCurrentTimeForTimezone(context, timeZone);
        } else {
            date += DateFormat.getTimeFormat(context).format(new Date());
        }
        return context.getString(type == WebcamType.USER ? R.string.main_shareText_userWebcam : R.string.main_shareText, name, location, date);
    }

    public String getFileName(Context context) {
        boolean specialCam = Constants.SPECIAL_CAMS.contains(publicId) || type == WebcamType.USER;
        String locationStr;
        if (!specialCam) {
            locationStr = location + " - " + DateTimeUtil.getCurrentTimeForTimezone(context, timeZone);
        } else {
            locationStr = location + " - " + DateFormat.getTimeFormat(context).format(new Date());
        }

        String res = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " - ";
        res += name + " - ";
        res += locationStr;
        res += ".jpg";

        res = FileUtil.getValidFileName(res, '_');
        return res;
    }
}