package com.lubenard.oring_reminder.utils

import android.content.res.Resources

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DateUtils {

    companion object {
        /**
         * Format date from Date to string using "yyyy-MM-dd HH:mm:ss" format
         * @param date The date to format
         * @return The string formatted
         */
        fun getdateFormatted(date: Date): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
        }

        /**
         * Parse a string date into a Date object
         * @param date the string date to parse
         * @return The Date format parsed
         */
        fun getdateParsed(date: String): Date? {
            try {
                return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
            } catch (e: Exception) {
                Log.e("DateUtils", "Failed to parse the date: ", e)
                return null
            }
        }

        fun getTimeParsed(toString: String): Date? {
            try {
                return SimpleDateFormat("HH:mm:ss").parse(toString)
            } catch (e: Exception) {
                Log.e("DateUtils", "Failed to parse the time: ", e)
                return null
            }
        }

        fun getCalendarParsed(calendar: Calendar): String {
            return getdateFormatted(calendar.time)
        }

        /**
         * Compute the diff between two given dates
         * The formula is date2 - date1
         * @param sDate1 First date in the form of a string
         * @param sDate2 Second date in the form of a string
         * @param timeUnit The timeUnit we want to return (Mostly minutes)
         * @return the time in unit between two dates in the form of a long
         */
        fun getDateDiff(sDate1: String, sDate2: String, timeUnit: TimeUnit): Long {
            val date1 = getdateParsed(sDate1)
            val date2 = getdateParsed(sDate2)

            return if (date1 != null && date2 != null) {
                val diffInMillis = date2.time - date1.time
                timeUnit.convert(diffInMillis, TimeUnit.MILLISECONDS)
            } else {
                Log.e("DateUtils", "Error: date1 $date1, date2: $date2")
                0
            }
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
        fun getDateDiff(date1: Date, date2: Date, timeUnit: TimeUnit): Long {
            val diffInMillis = date2.time - date1.time
            return timeUnit.convert(diffInMillis, TimeUnit.MILLISECONDS)
        }

        /**
         * Convert from int in Minute into a readable 'hour:minutes' format
         * @param timeWeared timeWeared is in minutes
         * @return a string containing the time the user weared the protection
         */
        fun convertIntIntoReadableDate(timeWeared: Int): String {
            return String.format("%dh%02dmn", timeWeared / 60, timeWeared % 60)
        }

        /**
         * Convert date into readable one:
         * Example : 2021-10-12 -> 12 October 2021
         * @param s the 'raw' date
         * @param shorterVersion If true, return under format: '12 Oct 2021'
         * @return A 'readable' date
         */
        fun convertDateIntoReadable(s: String, shorterVersion: Boolean): String? {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd").parse(s)

                val current_language = Resources.getSystem().configuration.locale.language

                val simpleDateFormat: SimpleDateFormat = if (shorterVersion)
                    SimpleDateFormat("dd MMM yyyy", Locale(current_language))
                else
                    SimpleDateFormat("dd LLLL yyyy", Locale(current_language))

                return simpleDateFormat.format(date!!)
            } catch (e: Exception) {
                Log.d("DateUtils", "Error while parsing date $s: ", e)
            }
            return null
        }

        /**
         * Convert date into readable one:
         * Example : 2021-10-12 -> 12 October 2021
         * Same as above, but take a Calendar as parameter
         * @param calendar
         * @return a String containing converted date
         */
        fun convertDateIntoReadable(calendar: Calendar, shorterVersion: Boolean): String {

            val current_language = Resources.getSystem().configuration.locale.language

            val simpleDateFormat: SimpleDateFormat = if (shorterVersion)
                SimpleDateFormat("dd MMM yyyy", Locale(current_language))
            else
                SimpleDateFormat("dd LLLL yyyy", Locale(current_language))

            return simpleDateFormat.format(calendar.time)
        }

        fun setAppLocale(localeCode: String) {
            // TODO: Broken due to change to KT
            // Idea: Register it inside sharedPref
            //current_language = localeCode
        }

        /**
         * Check if the input string is valid
         * @param text the given input string
         * @return 1 if the string is valid, else 0
         */
        fun isDateSane(text: String): Boolean {
            return !(text == "" || text == "NOT SET YET" || getdateParsed(text) == null)
        }
    }
}
