/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2009-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.hellomundo.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;

import org.jraf.android.hellomundo.Constants;
import org.jraf.android.hellomundo.provider.webcam.WebcamType;
import org.jraf.android.latoureiffel.R;
import org.jraf.android.util.datetime.DateTimeUtil;
import org.jraf.android.util.file.FileUtil;

public class WebcamInfo {
    public String name;
    public String location;
    public String timeZone;
    public String publicId;
    public String url;
    public String httpReferer;
    public String localBitmapUriStr;
    public WebcamType type;

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