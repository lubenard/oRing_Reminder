package com.lubenard.oring_reminder.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.lubenard.oring_reminder.R

import java.util.Calendar

class UiUtils {
    companion object {

        /**
         * Show toast if a date has been malformed
         */
        fun showToastBadFormattedDate(context: Context) {
            Toast.makeText(context, R.string.bad_date_format, Toast.LENGTH_SHORT).show()
        }

        fun enableEditText(editText: EditText) {
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
        }

        fun disableEditText(editText: EditText) {
            editText.isFocusable = false
        }

        /**
         * Open time picker
         * @param filling_textview
         */
        fun openTimePicker(
            context: Context,
            filling_textview: TextView,
            showTextViewDate: Boolean
        ) {
            // Get Current Time
            val calendar = Calendar.getInstance()
            val mHour: Int
            val mMinute: Int
            val timePutSplitted = filling_textview.text.toString().split(":")

            if (showTextViewDate && timePutSplitted.size == 2) {
                mHour = Integer.parseInt(timePutSplitted[0])
                mMinute = Integer.parseInt(timePutSplitted[1])
            } else {
                mHour = calendar.get(Calendar.HOUR_OF_DAY)
                mMinute = calendar.get(Calendar.MINUTE)
            }
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    filling_textview.text = "$hourOfDay:$minute:00"
                },
                mHour,
                mMinute,
                DateFormat.is24HourFormat(context)
            )
            timePickerDialog.show()
        }

        /**
         * Open Calendar picker
         * @param filling_textview
         */
        fun openCalendarPicker(
            context: Context,
            filling_textview: TextView,
            showTextViewDate: Boolean
        ) {
            // Get Current Date
            val calendar = Calendar.getInstance()
            val mYear: Int
            val mMonth: Int
            val mDay: Int
            val timePutSplitted = filling_textview.text.toString().split("-")
            if (showTextViewDate && timePutSplitted.size == 3) {
                mYear = Integer.parseInt(timePutSplitted[0])
                mMonth = Integer.parseInt(timePutSplitted[1])
                mDay = Integer.parseInt(timePutSplitted[2])
            } else {
                mYear = calendar.get(Calendar.YEAR)
                mMonth = calendar.get(Calendar.MONTH)
                mDay = calendar.get(Calendar.DAY_OF_MONTH)
            }

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, monthOfYear, dayOfMonth ->
                    filling_textview.text =
                        "$year-${if (monthOfYear < 10) "0" else ""}${(monthOfYear + 1)}-$dayOfMonth"
                },
                mYear, mMonth - 1, mDay
            )
            datePickerDialog.show()
        }
    }
}
