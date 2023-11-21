package com.lubenard.oring_reminder.utils;

import android.content.res.Resources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    private static String current_language;

    /**
     * Format date from Date to string using "yyyy-MM-dd HH:mm:ss" format
     * @param date The date to format
     * @return The string formatted
     */
    public static String getdateFormatted(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * Parse a string date into a Date object
     * @param date the string date to parse
     * @return The Date format parsed
     */
    public static Date getdateParsed(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getCalendarParsed(Calendar calendar) {
        return getdateFormatted(calendar.getTime());
    }

    /**
     * Compute the diff between two given dates
     * The formula is date2 - date1
     * @param sDate1 First date in the form of a string
     * @param sDate2 Second date in the form of a string
     * @param timeUnit The timeUnit we want to return (Mostly minutes)
     * @return the time in unit between two dates in the form of a long
     */
    public static long getDateDiff(String sDate1, String sDate2, TimeUnit timeUnit) {
        Date date1 = getdateParsed(sDate1);
        Date date2 = getdateParsed(sDate2);

        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * getDateDiff methord overload. Same as abovfe, but take 2 dates instead of 2 Strings
     * Compute the diff between two given dates
     * The formula is date2 - date1
     * @param date1 First date in the form of a Date
     * @param date2 Second date in the form of a Date
     * @param timeUnit The timeUnit we want to return (Mostly minutes)
     * @return the time in minutes between two dates
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * Convert from int in Minute into a readable 'hour:minutes' format
     * @param timeWeared timeWeared is in minutes
     * @return a string containing the time the user weared the protection
     */
    public static String convertIntIntoReadableDate(int timeWeared) {
        return String.format("%dh%02dmn", timeWeared / 60, timeWeared % 60);
    }

    /**
     * Convert date into readable one:
     * Example : 2021-10-12 -> 12 October 2021
     * @param s the 'raw' date
     * @param shorterVersion If true, return under format: '12 Oct 2021'
     * @return A 'readable' date
     */
    public static String convertDateIntoReadable(String s, boolean shorterVersion) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat simpleDateFormat;

        if (current_language == null)
            current_language = Resources.getSystem().getConfiguration().locale.getLanguage();

        if (shorterVersion)
            simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale(current_language));
        else
            simpleDateFormat = new SimpleDateFormat("dd LLLL yyyy", new Locale(current_language));

        return simpleDateFormat.format(date);
    }

    /**
     * Convert date into readable one:
     * Example : 2021-10-12 -> 12 October 2021
     * @param calendar
     * @return a String containing converted date
     */
    public static String convertDateIntoReadable(Calendar calendar, boolean shorterVersion) {
        SimpleDateFormat simpleDateFormat;

        if (current_language == null)
            current_language = Resources.getSystem().getConfiguration().locale.getLanguage();

        if (shorterVersion)
            simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale(current_language));
        else
            simpleDateFormat = new SimpleDateFormat("dd LLLL yyyy", new Locale(current_language));

        return simpleDateFormat.format(calendar.getTime());
    }

    public static void setAppLocale(String localeCode) {
        current_language = localeCode;
    }

    public static Date getTimeParsed(String toString) {
        try {
            return new SimpleDateFormat("HH:mm:ss").parse(toString);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Check if the input string is valid
     * @param text the given input string
     * @return 1 if the string is valid, else 0
     */
    //TODO: Refactor this method to make return boolean
    public static boolean isDateSane(String text) {
        if (text.equals("") || text.equals("NOT SET YET") || getdateParsed(text) == null)
            return false;
        return true;
    }
}
