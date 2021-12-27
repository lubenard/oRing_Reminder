package com.lubenard.oring_reminder.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.CurrentSessionWidgetProvider;
import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.broadcast_receivers.NotificationSenderBroadcastReceiver;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION.SDK_INT;

public class EditEntryFragment extends Fragment {

    public static final String TAG = "EditEntryFragment";

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

    private SharedPreferences sharedPreferences;
    private int weared_time;
    private boolean should_warn_user;

    private Context context;
    HashMap <Integer, String> runningSessions;
    private static boolean shouldUpdateMainList;

    abstract class LightTextWatcher implements TextWatcher {
        @Override public final void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override public final void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    }

    /**
     * This will set a alarm that will trigger a notification at alarmDate + time wearing setting
     * @param alarmDate The date of the alarm in the form 2020-12-30 10:42:00
     * @param entryId the id entry of the entry to update
     */
    public static void setAlarm(Context context, String alarmDate, long entryId, boolean cancelOldAlarm) {
        // From the doc, just create the exact same intent, and cancel it.
        // https://developer.android.com/reference/android/app/AlarmManager.html#cancel(android.app.PendingIntent)
        Intent intent = new Intent(context, NotificationSenderBroadcastReceiver.class)
                .putExtra("action", 1)
                .putExtra("entryId", entryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (cancelOldAlarm)
            am.cancel(pendingIntent);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(alarmDate));

        if (SDK_INT < Build.VERSION_CODES.M)
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        else
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void setUpdateMainList(boolean newStatus) {
        shouldUpdateMainList = newStatus;
    }

    /**
     * Used to have headless fragment from mainFragment
     * @param context context used to get get db
     */
    public EditEntryFragment(Context context) {
        this.entryId = -1;
        this.context = context;
        dbManager = MainActivity.getDbManager();
        if (dbManager == null)
            dbManager = new DbManager(context);
        runningSessions = dbManager.getAllRunningSessions();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));
        should_warn_user = sharedPreferences.getBoolean("myring_prevent_me_when_started_session", true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.edit_entry_fragment, container, false);
    }

    /**
     * Save entry into db
     * @param formattedDatePut DatePut
     * @param shouldGoBack if we need to bo back after saving entry
     */
    private void saveEntry(String formattedDatePut, boolean shouldGoBack) {
        if (entryId != -1)
            dbManager.updateDatesRing(entryId, formattedDatePut, "NOT SET YET", 1);
        else {
            long newlyInsertedEntry = dbManager.createNewDatesRing(formattedDatePut, "NOT SET YET", 1);
            // Set alarm only for new entry
            if (sharedPreferences.getBoolean("myring_send_notif_when_session_over", true)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.getdateParsed(formattedDatePut));
                calendar.add(Calendar.HOUR_OF_DAY, weared_time);
                Log.d(TAG, "New entry: setting alarm at " + calendar.getTimeInMillis());
                setAlarm(context, Utils.getdateFormatted(calendar.getTime()), newlyInsertedEntry, false);
            }
        }
        // We do not need to go back if this is a long click on '+' on mainFragment
        if (shouldGoBack)
            // Get back to the last element in the fragment stack
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        // We should update listmainview if long click.
        // We could have merged with the condition above, but i wanted to have better granular control
        // if needed
        /*if (shouldUpdateMainList)
            OldMainFragment.updateElementList(true);*/
    }

    /**
     * Used to help inserting entry when long click from '+'
     * @param formattedDatePut formatted using utils tools string from date
     * @param shouldGoBack if inside new entryFragment, go back, else no
     */
    public void insertNewEntry(String formattedDatePut, boolean shouldGoBack) {
            if (!runningSessions.isEmpty() && should_warn_user) {
                new AlertDialog.Builder(context).setTitle(R.string.alertdialog_multiple_running_session_title)
                        .setMessage(R.string.alertdialog_multiple_running_session_body)
                        .setPositiveButton(R.string.alertdialog_multiple_running_session_choice1, (dialog, which) -> {
                            for (Map.Entry<Integer, String> sessions : runningSessions.entrySet()) {
                                Log.d(TAG, "Set session " + sessions.getKey() + " to finished");
                                dbManager.updateDatesRing(sessions.getKey(), sessions.getValue(), Utils.getdateFormatted(new Date()), 0);
                            }
                            saveEntry(formattedDatePut, shouldGoBack);
                        })
                        .setNegativeButton(R.string.alertdialog_multiple_running_session_choice2, (dialog, which) -> saveEntry(formattedDatePut, shouldGoBack))
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            } else {
                saveEntry(formattedDatePut, shouldGoBack);
            }
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            filling_textview.setText(hourOfDay + ":" + minute + ":00");
                }
            }, mHour, mMinute, false);
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

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        context = getContext();

        new_entry_date_from = view.findViewById(R.id.new_entry_date_from);
        new_entry_time_from = view.findViewById(R.id.new_entry_hour_from);

        new_entry_date_to = view.findViewById(R.id.new_entry_date_to);
        new_entry_time_to = view.findViewById(R.id.new_entry_hour_to);

        getItOnBeforeTextView = view.findViewById(R.id.get_it_on_before);

        Button auto_from_button = view.findViewById(R.id.new_entry_auto_date_from);
        Button new_entry_auto_date_to = view.findViewById(R.id.new_entry_auto_date_to);

        new_entry_datepicker_from = view.findViewById(R.id.new_entry_datepicker_from);
        new_entry_timepicker_from = view.findViewById(R.id.new_entry_timepicker_from);
        new_entry_datepicker_to = view.findViewById(R.id.new_entry_datepicker_to);
        new_entry_timepicker_to =view.findViewById(R.id.new_entry_timepicker_to);

        new_entry_datepicker_from.setOnClickListener(v -> openCalendarPicker(new_entry_date_from));
        new_entry_timepicker_from.setOnClickListener(v -> openTimePicker(new_entry_time_from));

        new_entry_datepicker_to.setOnClickListener(v -> openCalendarPicker(new_entry_date_to));
        new_entry_timepicker_to.setOnClickListener(v -> openTimePicker(new_entry_time_to));

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
            String[] datetime_formatted = Utils.getdateFormatted(new Date()).split(" ");
            new_entry_date_from.setText(datetime_formatted[0]);
            new_entry_time_from.setText(datetime_formatted[1]);
            computeTimeBeforeGettingItAgain();
        });

        new_entry_auto_date_to.setOnClickListener(view12 -> {
            String[] datetime_formatted = Utils.getdateFormatted(new Date()).split(" ");
            new_entry_date_to.setText(datetime_formatted[0]);
            new_entry_time_to.setText(datetime_formatted[1]);
            computeTimeBeforeGettingItAgain();
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

        // If new_entry_datetime_from is valid but new_entry_datetime_to is not valid
        if (is_new_entry_datetime_to_valid == 0 && Utils.checkDateInputSanity(datetime_from) == 1) {
            calendar.setTime(Utils.getdateParsed(datetime_from));
            calendar.add(Calendar.HOUR_OF_DAY, weared_time + 9);
            getItOnBeforeTextView.setText(getString(R.string.get_it_on_before) + " " + Utils.getdateFormatted(calendar.getTime()));
        } else if (is_new_entry_datetime_to_valid == 1) {
            // Only if new_entry_datetime_to is valid (meaning a session is supposed to have a end date)
            calendar.setTime(Utils.getdateParsed(datetime_to));
            calendar.add(Calendar.HOUR_OF_DAY, 9);
            getItOnBeforeTextView.setText(getString(R.string.get_it_on_before) + " " + Utils.getdateFormatted(calendar.getTime()));
        } else
            getItOnBeforeTextView.setText(R.string.not_enough_datas_to_compute_get_it_on);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_entry, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_validate:

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
                            setAlarm(context, Utils.getdateFormatted(calendar.getTime()) , entryId,true);
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
                            cancelAlarm(context, entryId);
                            getActivity().getSupportFragmentManager().popBackStackImmediate();
                        } else {
                            Log.d(TAG, "DateFormat wrong check 2");
                            showToastBadFormattedDate();
                        }
                    }
                } else {
                    if (formattedDateRemoved.length() == 1) {
                        if (Utils.checkDateInputSanity(formattedDatePut) == 1) {
                            insertNewEntry(formattedDatePut, true);
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
                return true;
            default:
                return false;
        }
    }

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

    /**
     * Only cancel alarm for given entryId
     */
    public static void cancelAlarm(Context context, long entryId) {
        // From the doc, just create the exact same intent, and cancel it.
        // https://developer.android.com/reference/android/app/AlarmManager.html#cancel(android.app.PendingIntent)
        Intent intent = new Intent(context, NotificationSenderBroadcastReceiver.class)
                .putExtra("action", 1)
                .putExtra("entryId", entryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}
