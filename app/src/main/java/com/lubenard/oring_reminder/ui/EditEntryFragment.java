package com.lubenard.oring_reminder.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.NotificationSenderBroadcastReceiver;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private EditText new_entry_datetime_from;
    private EditText new_entry_datetime_to;

    private TextView getItOnBeforeTextView;

    private SharedPreferences sharedPreferences;
    private int weared_time;
    private boolean should_warn_user;

    private Context context;
    HashMap <Integer, String> runningSessions;
    private static boolean shouldUpdateMainList;

    /**
     * This will set a alarm that will trigger a notification at alarmDate + time wearing setting
     * @param alarmDate The date of the alarm in the form 2020-12-30 10:42:00
     * @param entryId the id entry of the entry to update
     */
    public static void setAlarm(Context context, String alarmDate, long entryId, int wearing_time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(alarmDate));
        calendar.add(Calendar.HOUR_OF_DAY, wearing_time);
        Log.d(TAG, "Setting the alarm for this timstamp in millis " + calendar.getTimeInMillis());
        Log.d(TAG, "setAlarm receive id: " + entryId);
        Intent intent = new Intent(context, NotificationSenderBroadcastReceiver.class).putExtra("entryId", entryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M)
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        else if (SDK_INT >= Build.VERSION_CODES.M)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void setUpddateMainList(boolean newStatus) {
        shouldUpdateMainList = newStatus;
    }

    /**
     * Used to have headless fragment from mainFragment
     * @param context context used to get get db
     */
    public EditEntryFragment(Context context) { //  // If entry already exist in the db.If entry already exist in the db.
        this.entryId = -1;
        this.context = context;
        dbManager = new DbManager(context);
        runningSessions = dbManager.getAllRunningSessions();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));
        should_warn_user = sharedPreferences.getBoolean("myring_prevent_me_when_started_session", true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.edit_entry_fragment, container, false);
    } // If entry already exist in the db. // If en // If entry already exist in the db.try already exist in the db.

    private void saveEntry(String formattedDatePut, boolean shouldGoBack) {
        if (entryId != -1)
            dbManager.updateDatesRing(entryId, formattedDatePut, "NOT SET YET", 1);
        else {
            long newlyInsertedEntry = dbManager.createNewDatesRing(formattedDatePut, "NOT SET YET", 1);
            // Set alarm only for new entry
            if (sharedPreferences.getBoolean("myring_send_notif_when_session_over", true))
                setAlarm(context, formattedDatePut, newlyInsertedEntry, weared_time);
        }
        // We do not need to go back if this is a long click on '+' on mainFragment
        if (shouldGoBack)
            // Get back to the last element in the fragment stack
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        // We should update listmainview if long click.
        // We could have merged with the condition above, but i wanted to have better granular control
        // if needed
        if (shouldUpdateMainList) {
            MainFragment.updateElementList();
        }
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
                        .setPositiveButton(R.string.alertdialog_multiple_running_session_choice1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                for (Map.Entry<Integer, String> sessions : runningSessions.entrySet()) {
                                    Log.d(TAG, "Set session " + sessions.getKey() + " to finished");
                                    dbManager.updateDatesRing(sessions.getKey(), sessions.getValue(), Utils.getdateFormatted(new Date()), 0);
                                }
                                saveEntry(formattedDatePut, shouldGoBack);
                            }
                        })
                        .setNegativeButton(R.string.alertdialog_multiple_running_session_choice2, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                saveEntry(formattedDatePut, shouldGoBack);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            } else {
                saveEntry(formattedDatePut, shouldGoBack);
            }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new_entry_datetime_from = view.findViewById(R.id.new_entry_date_from);
        new_entry_datetime_to = view.findViewById(R.id.new_entry_date_to);
        getItOnBeforeTextView = view.findViewById(R.id.get_it_on_before);

        Button auto_from_button = view.findViewById(R.id.new_entry_auto_date_from);
        Button new_entry_auto_date_to = view.findViewById(R.id.new_entry_auto_date_to);

        context = getContext();
        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        if (entryId != -1) {
            RingModel data = dbManager.getEntryDetails(entryId);
            new_entry_datetime_from.setText(data.getDatePut());
            new_entry_datetime_to.setText(data.getDateRemoved());
            getActivity().setTitle(R.string.action_edit);
        } else {
            getActivity().setTitle(R.string.create_new_entry);
        }

        auto_from_button.setOnClickListener(view1 -> {
            new_entry_datetime_from.setText(Utils.getdateFormatted(new Date()));
            computeTimeBeforeGettingItAgain();
        });

        new_entry_auto_date_to.setOnClickListener(view12 -> {
            new_entry_datetime_to.setText(Utils.getdateFormatted(new Date()));
            computeTimeBeforeGettingItAgain();
        });

        new_entry_datetime_from.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable s) {
                computeTimeBeforeGettingItAgain();
            }
        });

        new_entry_datetime_to.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable s) {
                computeTimeBeforeGettingItAgain();
            }
        });

        computeTimeBeforeGettingItAgain();
    }

    /**
     * Check if the input string is valid
     * @param text the given input string
     * @return 1 if the string is valid, else 0
     */
    private int checkInputSanity(String text) {
        if (text.equals("") || text.equals("NOT SET YET") || Utils.getdateParsed(text) == null)
            return 0;
        return 1;
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
        if (checkInputSanity(new_entry_datetime_to.getText().toString()) == 0 &&
                checkInputSanity(new_entry_datetime_from.getText().toString()) == 1) {
            calendar.setTime(Utils.getdateParsed(new_entry_datetime_from.getText().toString()));
            calendar.add(Calendar.HOUR_OF_DAY, weared_time + 9);
            getItOnBeforeTextView.setText("Get it on before " + Utils.getdateFormatted(calendar.getTime()));
        } else if (checkInputSanity(new_entry_datetime_to.getText().toString()) == 1) {
            calendar.setTime(Utils.getdateParsed(new_entry_datetime_to.getText().toString()));
            calendar.add(Calendar.HOUR_OF_DAY, 9);
            getItOnBeforeTextView.setText("Get it on before " + Utils.getdateFormatted(calendar.getTime()));
        } else
            getItOnBeforeTextView.setText("Not enought datas to compute when you should wear it again");
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

                String formattedDatePut = new_entry_datetime_from.getText().toString();
                String formattedDateRemoved = new_entry_datetime_to.getText().toString();

                // If entry already exist in the db.
                if (entryId != -1) {
                    if (formattedDateRemoved.isEmpty() || formattedDateRemoved.equals("NOT SET YET")) {
                        if (checkInputSanity(formattedDatePut) == 0) {
                            dbManager.updateDatesRing(entryId, formattedDatePut, "NOT SET YET", 1);
                            // Recompute alarm if the entry already exist, but has no ending time
                            recomputeAlarm(Utils.getDateDiff(formattedDatePut, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES), true);
                        } else
                            showToastBadFormattedDate();
                    } else {
                        if (checkInputSanity(formattedDatePut) == 0 && checkInputSanity(formattedDateRemoved) == 0) {
                            dbManager.updateDatesRing(entryId, formattedDatePut, formattedDateRemoved, 0);
                            // if the entry has a ending time, just canceled it (mean it has been finished by user manually)
                            recomputeAlarm(-1, false);
                        } else
                            showToastBadFormattedDate();
                    }
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                } else {
                    if (formattedDateRemoved.isEmpty())
                        if (checkInputSanity(formattedDatePut) == 0) {
                            insertNewEntry(formattedDatePut, true);
                        } else
                            showToastBadFormattedDate();
                    else if (Utils.getDateDiff(formattedDatePut, formattedDateRemoved, TimeUnit.MINUTES) > 0) {
                        if (checkInputSanity(formattedDatePut) == 1 && checkInputSanity(formattedDateRemoved) == 1) {
                            dbManager.createNewDatesRing(formattedDatePut, formattedDateRemoved, 0);
                            // Get back to the last element in the fragment stack
                            getActivity().getSupportFragmentManager().popBackStackImmediate();
                        } else
                            showToastBadFormattedDate();
                    } else
                        // If the diff time is too short, trigger this error
                        Toast.makeText(context, R.string.error_edit_entry_date, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return false;
        }
    }

    private void showToastBadFormattedDate() {
        Toast.makeText(context, R.string.bad_date_format, Toast.LENGTH_SHORT).show();
    }

    /**
     * Recompute alarm date, and set a new one if needed
     * @param newAlarmDate The new alarm date
     * @param reSetAlarm if the alarm should be re set (else it is just canceled)
     */
    private void recomputeAlarm(long newAlarmDate, boolean reSetAlarm) {
        // From the doc, just create the exact same intent, and cancel it.
        // https://developer.android.com/reference/android/app/AlarmManager.html#cancel(android.app.PendingIntent)
        Intent intent = new Intent(context, NotificationSenderBroadcastReceiver.class).putExtra("entryId", entryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) entryId, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.cancel(pendingIntent);

        if (reSetAlarm) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Utils.getdateParsed(dbManager.getEntryDetails(entryId).getDatePut()));
            calendar.add(Calendar.MINUTE, (int)newAlarmDate);
            Log.d(TAG, "Alarm has been reschedule by user at " + calendar.getTime());
            if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M)
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            else if (SDK_INT >= Build.VERSION_CODES.M)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
