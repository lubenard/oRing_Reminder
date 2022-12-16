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
    public static void openTimePicker(Context context, TextView filling_textview, boolean showTextViewDate) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour;
        int mMinute;
        if (showTextViewDate) {
            String[] timePutSplitted = filling_textview.getText().toString().split(":");
            mHour = Integer.parseInt(timePutSplitted[0]);
            mMinute = Integer.parseInt(timePutSplitted[1]);
        } else {
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
        }
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> filling_textview.setText(hourOfDay + ":" + minute + ":00"), mHour, mMinute, DateFormat.is24HourFormat(context));
        timePickerDialog.show();
    }

    /**
     * Open Calendar picker
     * @param filling_textview
     */
    public static void openCalendarPicker(Context context, TextView filling_textview, boolean showTextViewDate) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear;
        int mMonth;
        int mDay;
        if (showTextViewDate) {
            String[] timePutSplitted = filling_textview.getText().toString().split("-");
            mYear = Integer.parseInt(timePutSplitted[0]);
            mMonth = Integer.parseInt(timePutSplitted[1]);
            mDay = Integer.parseInt(timePutSplitted[2]);
        } else {
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, year, monthOfYear, dayOfMonth) -> filling_textview.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth), mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
