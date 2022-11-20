package com.lubenard.oring_reminder.ui.fragments;

import static android.os.Build.VERSION.SDK_INT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.CurrentSessionWidgetProvider;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBroadcastReceiver;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.managers.SettingsManager;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditEntryFragment extends DialogFragment {

    private static final String TAG = "EditEntryFragment";

    private DbManager dbManager;
    private long entryId;

    private EditText new_entry_date_from;
    private EditText new_entry_time_from;

    private EditText new_entry_date_to;
    private EditText new_entry_time_to;

    private ImageButton new_entry_datepicker_from;
    private ImageButton new_entry_timepicker_from;
    private ImageButton new_entry_datepicker_to;
    private ImageButton new_entry_timepicker_to;

    private TextView getItOnBeforeTextView;

    private Context context;

    abstract class LightTextWatcher implements TextWatcher {
        @Override public final void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override public final void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.edit_entry_fragment, container, false);
    }

    /**
     * Open time picker
     * @param filling_textview
     */
    private void openTimePicker(TextView filling_textview) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> filling_textview.setText(hourOfDay + ":" + minute + ":00"), mHour, mMinute, DateFormat.is24HourFormat(getContext()));
            timePickerDialog.show();
    }

    /**
     * Open Calendar picker
     * @param filling_textview
     */
    private void openCalendarPicker(TextView filling_textview) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, monthOfYear, dayOfMonth) -> filling_textview.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth), mYear, mMonth, mDay);
            datePickerDialog.show();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        context = getContext();

        new_entry_date_from = view.findViewById(R.id.new_entry_date_from);
        new_entry_datepicker_from = view.findViewById(R.id.new_entry_datepicker_from);

        new_entry_time_from = view.findViewById(R.id.new_entry_hour_from);
        new_entry_timepicker_from = view.findViewById(R.id.new_entry_timepicker_from);

        new_entry_date_to = view.findViewById(R.id.new_entry_date_to);
        new_entry_datepicker_to = view.findViewById(R.id.new_entry_datepicker_to);

        new_entry_time_to = view.findViewById(R.id.new_entry_hour_to);
        new_entry_timepicker_to =view.findViewById(R.id.new_entry_timepicker_to);

        getItOnBeforeTextView = view.findViewById(R.id.get_it_on_before);

        Button auto_from_button = view.findViewById(R.id.new_entry_auto_date_from);
        Button new_entry_auto_date_to = view.findViewById(R.id.new_entry_auto_date_to);

        new_entry_datepicker_from.setOnClickListener(v -> openCalendarPicker(new_entry_date_from));
        new_entry_timepicker_from.setOnClickListener(v -> openTimePicker(new_entry_time_from));

        new_entry_datepicker_to.setOnClickListener(v -> openCalendarPicker(new_entry_date_to));
        new_entry_timepicker_to.setOnClickListener(v -> openTimePicker(new_entry_time_to));

        view.findViewById(R.id.create_new_session_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.create_new_session_save).setOnClickListener(v -> {
            String formattedDatePut = new_entry_date_from.getText().toString() + " " + new_entry_time_from.getText().toString();
            String formattedDateRemoved = new_entry_date_to.getText().toString() + " " + new_entry_time_to.getText().toString();

            Log.d(TAG, "formattedDatePut: '" + formattedDatePut + "' formattedDateRemoved: '" + formattedDateRemoved + "'");

            // If entry already exist in the db.
            if (entryId != -1) {
                if (formattedDateRemoved.length() == 1 || formattedDateRemoved.equals("NOT SET")) {
                    if (Utils.checkDateInputSanity(formattedDatePut) == 1) {
                        dbManager.updateDatesRing(entryId, formattedDatePut, "NOT SET YET", 1);
                        updateWidget(context);
                        // Recompute alarm if the entry already exist, but has no ending time
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.MINUTE, (int) Utils.getDateDiff(formattedDatePut, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                        SessionsAlarmsManager.setAlarm(context, Utils.getdateFormatted(calendar.getTime()) , entryId,true);
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    } else {
                        Log.d(TAG, "DateFormat wrong check 1");
                        showToastBadFormattedDate();
                    }
                } else {
                    if (Utils.checkDateInputSanity(formattedDatePut) == 1 && Utils.checkDateInputSanity(formattedDateRemoved) == 1) {
                        dbManager.updateDatesRing(entryId, formattedDatePut, formattedDateRemoved, 0);
                        dbManager.endPause(entryId);
                        updateWidget(context);
                        // if the entry has a ending time, just canceled it (mean it has been finished by user manually)
                        SessionsAlarmsManager.cancelAlarm(context, entryId);
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    } else {
                        Log.d(TAG, "DateFormat wrong check 2");
                        showToastBadFormattedDate();
                    }
                }
            } else {
                if (formattedDateRemoved.length() == 1) {
                    if (Utils.checkDateInputSanity(formattedDatePut) == 1) {
                        SessionsManager.insertNewEntry(context, formattedDatePut);
                        updateWidget(context);
                    } else {
                        Log.d(TAG, "DateFormat wrong check 3");
                        showToastBadFormattedDate();
                    }
                } else if (Utils.getDateDiff(formattedDatePut, formattedDateRemoved, TimeUnit.MINUTES) > 0) {
                    if (Utils.checkDateInputSanity(formattedDatePut) == 1 && Utils.checkDateInputSanity(formattedDateRemoved) == 1) {
                        dbManager.createNewDatesRing(formattedDatePut, formattedDateRemoved, 0);
                        updateWidget(context);
                        // Get back to the last element in the fragment stack
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    } else {
                        Log.d(TAG, "DateFormat wrong check 4");
                        showToastBadFormattedDate();
                    }
                } else
                    // If the diff time is too short, trigger this error
                    Toast.makeText(context, R.string.error_edit_entry_date, Toast.LENGTH_SHORT).show();
            }
            HomeFragment.updateDesign();
            dismiss();
        });

        // Fill datas into new fields
        if (entryId != -1) {
            RingSession data = dbManager.getEntryDetails(entryId);

            new_entry_date_from.setText(data.getDatePut().split(" ")[0]);
            new_entry_time_from.setText(data.getDatePut().split(" ")[1]);

            new_entry_date_to.setText(data.getDateRemoved().split(" ")[0]);
            new_entry_time_to.setText(data.getDateRemoved().split(" ")[1]);
            getActivity().setTitle(R.string.action_edit);
        } else
            getActivity().setTitle(R.string.create_new_entry);

        auto_from_button.setOnClickListener(view1 -> {
            preFillStartDatas();
        });

        new_entry_auto_date_to.setOnClickListener(view12 -> {
            preFillEndDatas();
        });

        new_entry_date_from.addTextChangedListener(new LightTextWatcher() {
            public void afterTextChanged(Editable e) {
                computeTimeBeforeGettingItAgain();
            }
        });

        new_entry_time_from.addTextChangedListener(new LightTextWatcher() {
            public void afterTextChanged(Editable s) {
                computeTimeBeforeGettingItAgain();
            }
        });

        new_entry_date_to.addTextChangedListener(new LightTextWatcher() {
            public void afterTextChanged(Editable s) {
                computeTimeBeforeGettingItAgain();
            }
        });

        new_entry_time_to.addTextChangedListener(new LightTextWatcher() {
            public void afterTextChanged(Editable s) {
                computeTimeBeforeGettingItAgain();
            }
        });

        dbManager = MainActivity.getDbManager();

        Window window = getDialog().getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = 1000;
        lp.height = WRAP_CONTENT;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(lp);
        window.setGravity(Gravity.BOTTOM);

        preFillStartDatas();
    }

    private void preFillStartDatas() {
        String[] datetime_formatted = Utils.getdateFormatted(new Date()).split(" ");
        new_entry_date_from.setText(datetime_formatted[0]);
        new_entry_time_from.setText(datetime_formatted[1]);
        computeTimeBeforeGettingItAgain();
    }

    private void preFillEndDatas() {
        String[] datetime_formatted = Utils.getdateFormatted(new Date()).split(" ");
        new_entry_date_to.setText(datetime_formatted[0]);
        new_entry_time_to.setText(datetime_formatted[1]);
        computeTimeBeforeGettingItAgain();
    }

    /**
     * Recompute time for Textview saying when user should wear it again
     * This is computed in the following way:
     * If "to" editText is empty -> "from" textview + user-defined-wearing-time + 9
     * If "to" editText is not empty -> "to" editText + 9
     * Else, no sufficient datas is given to compute it
     */
    private void computeTimeBeforeGettingItAgain() {
        Calendar calendar = Calendar.getInstance();

        String datetime_from = new_entry_date_from.getText().toString() + " " + new_entry_time_from.getText().toString();
        String datetime_to = new_entry_date_to.getText().toString() + " " + new_entry_time_to.getText().toString();

        Log.d(TAG, "datetime_from is " + datetime_from + " datetime_to is " + datetime_to);

        int is_new_entry_datetime_to_valid = Utils.checkDateInputSanity(datetime_to);

        SettingsManager settingsManager = new SettingsManager(context);

        // If new_entry_datetime_from is valid but new_entry_datetime_to is not valid
        if (is_new_entry_datetime_to_valid == 0 && Utils.checkDateInputSanity(datetime_from) == 1) {
            calendar.setTime(Utils.getdateParsed(datetime_from));
            calendar.add(Calendar.HOUR_OF_DAY, settingsManager.getWearingTimeInt() + 9);
            getItOnBeforeTextView.setText(getString(R.string.get_it_on_before) + " " + Utils.getdateFormatted(calendar.getTime()));
        } else if (is_new_entry_datetime_to_valid == 1) {
            // Only if new_entry_datetime_to is valid (meaning a session is supposed to have a end date)
            calendar.setTime(Utils.getdateParsed(datetime_to));
            calendar.add(Calendar.HOUR_OF_DAY, 9);
            getItOnBeforeTextView.setText(getString(R.string.get_it_on_before) + " " + Utils.getdateFormatted(calendar.getTime()));
        } else
            getItOnBeforeTextView.setText(R.string.not_enough_datas_to_compute_get_it_on);
    }

    //TODO: To move elsewhere
    /**
     * Instantly update the widget
     * @param context
     */
    public static void updateWidget(Context context) {
        //if (CurrentSessionWidgetProvider.isThereAWidget) {
            Log.d(TAG, "Updating Widget");
            Intent intent = new Intent(context, CurrentSessionWidgetProvider.class);
            context.sendBroadcast(intent);
        //}
    }

    /**
     * Show toast if a date has been malformed
     */
    private void showToastBadFormattedDate() {
        Toast.makeText(context, R.string.bad_date_format, Toast.LENGTH_SHORT).show();
    }
}
