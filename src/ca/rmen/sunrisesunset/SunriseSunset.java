package ca.rmen.sunrisesunset;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;

import org.jraf.android.worldtour.Config;
import org.jraf.android.worldtour.Constants;

public class SunriseSunset {
    private static final String TAG = Constants.TAG + SunriseSunset.class.getSimpleName();

    private static final int JULIAN_DATE_2000_01_01 = 2451545;
    private static final double CONST_0009 = 0.0009;
    private static final double CONST_360 = 360;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm z");


    /**
     * Convert an angle from radians to degrees.
     * 
     * @param rad
     *            the angle in radians
     * @return the angle in degrees
     */
    private static double radToDeg(final double rad) {
        return rad * 180 / Math.PI;
    }

    /**
     * Convert an angle from degrees to radians
     * 
     * @param deg
     *            the angle in degrees
     * @return the angle in radians.
     */
    private static double degToRad(final double deg) {
        return deg * Math.PI / 180;
    }

    /**
     * @param gregorianDate
     * @return the Julian date for the given Gregorian date.
     */
    private static double getJulianDate(final Calendar gregorianDate) {
        final int[] ymd = new int[] { gregorianDate.get(Calendar.YEAR), gregorianDate.get(Calendar.MONTH) + 1, gregorianDate.get(Calendar.DAY_OF_MONTH) };
        double julianDate = JulianDate.toJulian(ymd);
        julianDate = julianDate + gregorianDate.get(Calendar.HOUR_OF_DAY) / 24.0 + gregorianDate.get(Calendar.MINUTE) / 60.0;
        return julianDate;
    }

    /**
     * Convert a Julian date to a Gregorian date
     * 
     * @param julianDate
     * @return
     */
    private static Calendar getGregorianDate(final double julianDate) {
        // Get the year, day, and month of the Julian date. (Ignores the hour
        // and minute).
        final int ymd[] = JulianDate.fromJulian(julianDate);

        // Create a gregorian date at noon.
        final Calendar gregorianDate = Calendar.getInstance();
        gregorianDate.set(Calendar.YEAR, ymd[0]);
        gregorianDate.set(Calendar.MONTH, ymd[1] - 1);
        gregorianDate.set(Calendar.DAY_OF_MONTH, ymd[2]);

        // The Julian day starts at noon
        gregorianDate.set(Calendar.HOUR_OF_DAY, 12);
        gregorianDate.set(Calendar.MINUTE, 0);
        gregorianDate.set(Calendar.SECOND, 0);
        gregorianDate.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Get the julian date for this Gregorian date at noon
        final double julianDate2 = getJulianDate(gregorianDate);

        // Compare the julian date at noon and the real julian date. Add the
        // difference (fraction of a day) to the gregorian date.
        final double dayFraction = julianDate - julianDate2;// ex: 0.717
        final int hours = (int) (dayFraction * 24); // ex: 17.2 -> 17
        final int minutes = (int) ((dayFraction * 24 - hours) * 60d); // 17.2-17
        gregorianDate.set(Calendar.HOUR_OF_DAY, hours);
        gregorianDate.set(Calendar.MINUTE, minutes);
        return gregorianDate;
    }

    /**
     * Calculate the sunrise and sunset times for the given date and given location. This is based on the Wikipedia
     * article on the Sunrise equation: http://en.wikipedia.org/wiki/Sunrise_equation
     * 
     * @param day
     *            The day for which to calculate sunrise and sunset
     * @param latitude
     *            the latitude of the location in degrees.
     * @param longitude
     *            the longitude of the location in degrees (West is negative)
     * @return a two-element Gregorian Calendar array. The first element is the sunrise, the second element is the
     *         sunset.
     */
    public static Calendar[] getSunriseSunset(final Calendar day, final double latitude, double longitude) {
        if (Config.LOGD) Log.d(TAG, "getSunriseSunset day=" + DATE_FORMAT.format(day.getTime()) + " latitude=" + latitude + " longitude=" + longitude);
        final double latitudeRad = degToRad(latitude);

        longitude = -longitude;

        // Get the given date as a Julian date.
        final double julianDate = getJulianDate(day);

        // Calculate current Julian cycle (number of days since 2000-01-01).
        final double nstar = julianDate - JULIAN_DATE_2000_01_01 - CONST_0009 - longitude / CONST_360;
        final double n = Math.round(nstar);

        // Approximate solar noon
        final double jstar = JULIAN_DATE_2000_01_01 + CONST_0009 + longitude / CONST_360 + n;
        // Solar mean anomaly
        final double m = degToRad((357.5291 + 0.98560028 * (jstar - JULIAN_DATE_2000_01_01)) % CONST_360);

        // Equation of center
        final double c = 1.9148 * Math.sin(m) + 0.0200 * Math.sin(2 * m) + 0.0003 * Math.sin(3 * m);

        // Ecliptic longitude
        final double lambda = degToRad((radToDeg(m) + 102.9372 + c + 180) % CONST_360);

        // Solar transit (hour angle for solar noon)
        final double jtransit = jstar + 0.0053 * Math.sin(m) - 0.0069 * Math.sin(2 * lambda);

        // Declination of the sun.
        final double delta = Math.asin(Math.sin(lambda) * Math.sin(degToRad(23.45)));

        // Hour angle
        final double omega = Math.acos((Math.sin(degToRad(-0.83)) - Math.sin(latitudeRad) * Math.sin(delta)) / (Math.cos(latitudeRad) * Math.cos(delta)));

        // Sunset
        final double jset = JULIAN_DATE_2000_01_01 + CONST_0009
                + ((radToDeg(omega) + longitude) / CONST_360 + n + 0.0053 * Math.sin(m) - 0.0069 * Math.sin(2 * lambda));

        // Sunrise
        final double jrise = jtransit - (jset - jtransit);
        // Convert sunset and sunrise to Gregorian dates.
        final Calendar gregRise = getGregorianDate(jrise);

        final Calendar gregSet = getGregorianDate(jset);
        if (Config.LOGD) Log.d(TAG, "getSunriseSunset res=[" + DATE_FORMAT.format(gregRise.getTime()) + " ,  " + DATE_FORMAT.format(gregSet.getTime()) + "]");
        return new Calendar[] { gregRise, gregSet };
    }

    /**
     * Test the main SunriseSunset method.
     * 
     * @param args
     *            : date in the format yyyyMMdd, latitude in degrees, longitude in degrees, the timeZone to display the
     *            result
     * @throws ParseException
     */
    public static void main(final String args[]) throws ParseException {
        if (args.length != 4) {
            System.err.println("Usage: " + SunriseSunset.class.getName()
                    + " <date in yyyyMMdd> <latitude in degrees> <longitude in degrees (Est=positive)> <Java timezone>");
            System.err.println("Ex: " + SunriseSunset.class.getName() + " 20090602 34 118 America/Los_Angeles");
            System.exit(-1);
        }
        final SunriseSunset me = new SunriseSunset();
        final String dateformat = "yyyyMMdd";
        final SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
        int idx = 0;
        final Date date = sdf.parse(args[idx++]);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final double latitude = Double.parseDouble(args[idx++]);
        final double longitude = Double.parseDouble(args[idx++]);
        final TimeZone timeZone = TimeZone.getTimeZone(args[idx++]);
        final SimpleDateFormat sdfResult = new SimpleDateFormat("yyyy-MM-dd HH:mm z");
        sdfResult.setTimeZone(timeZone);
        final Calendar[] sunriseSunset = SunriseSunset.getSunriseSunset(cal, latitude, longitude);
        System.out.println("On " + sdf.format(date) + " at (" + latitude + "," + longitude + ", " + timeZone.getDisplayName() + "), the sun will be up from "
                + sdfResult.format(sunriseSunset[0].getTime()) + " to " + sdfResult.format(sunriseSunset[1].getTime()));

    }

    public static boolean isNight(String coordinates) {
        if (coordinates == null) return false;
        String[] coordStr = coordinates.split(",");
        double lat = Double.valueOf(coordStr[0]);
        double lon = Double.valueOf(coordStr[1]);
        Calendar today = Calendar.getInstance();
        //        today.set(Calendar.HOUR_OF_DAY, 0);
        //        today.set(Calendar.MINUTE, 0);
        //        today.set(Calendar.SECOND, 0);
        //        today.set(Calendar.MILLISECOND, 0);
        Calendar[] sunriseSunset = getSunriseSunset(today, lat, lon);
        Calendar sunrise = sunriseSunset[0];
        Calendar sunset = sunriseSunset[1];
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        if (Config.LOGD) Log.d(TAG, "isNight now=" + DATE_FORMAT.format(now.getTime()));
        return now.before(sunrise) || now.after(sunset);
    }
}
