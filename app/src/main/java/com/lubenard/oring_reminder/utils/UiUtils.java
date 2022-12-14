package com.lubenard.oring_reminder.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lubenard.oring_reminder.R;

import java.util.Calendar;

public class UiUtils {
    /**
     * Show toast if a date has been malformed
     */
    public static void showToastBadFormattedDate(Context context) {
        Toast.makeText(context, R.string.bad_date_format, Toast.LENGTH_SHORT).show();
    }

    public static void enableEditText(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
    }

    public static void disableEditText(EditText editText) {
        editText.setFocusable(false);
    }


    /**
     * Open time picker
     * @param filling_textview
     */
    public static void openTimePicker(Context context, TextView filling_textview) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> filling_textview.setText(hourOfDay + ":" + minute + ":00"), mHour, mMinute, DateFormat.is24HourFormat(context));
        timePickerDialog.show();
    }

    /**
     * Open Calendar picker
     * @param filling_textview
     */
    public static void openCalendarPicker(Context context, TextView filling_textview) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, year, monthOfYear, dayOfMonth) -> filling_textview.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth), mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
