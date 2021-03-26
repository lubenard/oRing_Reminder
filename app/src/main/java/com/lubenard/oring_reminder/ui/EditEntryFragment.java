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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.NotificationSenderBroadcastReceiver;
import com.lubenard.oring_reminder.R;
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

    private EditText new_entry_date_from;
    private EditText new_entry_time_from;
    private EditText new_entry_date_to;
    private EditText new_entry_time_to;

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
    private void setAlarm(String alarmDate, long entryId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(alarmDate));
        calendar.add(Calendar.HOUR_OF_DAY, weared_time);
        Log.d(TAG, "Setting the alarm for this timstamp in millis " + calendar.getTimeInMillis());
        Log.d(TAG, "setAlarm receive id: " + entryId);
        Intent intent = new Intent(context, NotificationSenderBroadcastReceiver.class).putExtra("entryId", entryId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M)
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        else if (SDK_INT >= Build.VERSION_CODES.M)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    /**
     * Fill the entry "from" with the right datas
     * @param date the date to set in input
     */
    private void fill_entry_from(String date) {
        String[] slittedDate = date.split(" ");
        new_entry_date_from.setText(slittedDate[0]);
        new_entry_time_from.setText(slittedDate[1]);
    }

    /**
     * Fill the entry "to" with the right datas
     * @param date the date to set in input
     */
    private void fill_entry_to(String date) {
        String[] slittedDate = date.split(" ");
        new_entry_date_to.setText(slittedDate[0]);
        new_entry_time_to.setText(slittedDate[1]);
    }

    public static void setUpddateMainList(boolean newStatus) {
        shouldUpdateMainList = newStatus;
    }

    /**
     * Used to have headless fragment from mainFragment
     * @param context context used to get get db
     */
    public EditEntryFragment(Context context) {
        this.entryId = -1;
        this.context = context;
        dbManager = new DbManager(context);
        runningSessions = dbManager.getRunningSessions();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));
        should_warn_user = sharedPreferences.getBoolean("myring_prevent_me_when_started_session", true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.edit_entry_fragment, container, false);
    }

    private void saveEntry(String formattedDatePut, boolean shouldGoBack) {
        if (entryId != -1)
            dbManager.updateDatesRing(entryId, formattedDatePut, "NOT SET YET", 1);
        else {
            long newlyInsertedEntry = dbManager.createNewDatesRing(formattedDatePut, "NOT SET YET", 1);
            // Set alarm only for new entry
            if (sharedPreferences.getBoolean("myring_send_notif_when_session_over", true))
                setAlarm(formattedDatePut, newlyInsertedEntry);
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

        new_entry_date_from = view.findViewById(R.id.new_entry_date_from);
        new_entry_time_from = view.findViewById(R.id.new_entry_time_from);

        new_entry_date_to = view.findViewById(R.id.new_entry_date_to);
        new_entry_time_to = view.findViewById(R.id.new_entry_time_to);

        Button auto_from_button = view.findViewById(R.id.new_entry_auto_date_from);
        Button new_entry_auto_date_to = view.findViewById(R.id.new_entry_auto_date_to);

        context = getContext();
        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        if (entryId != -1) {
            ArrayList<String> datas = dbManager.getEntryDetails(entryId);
            fill_entry_from(datas.get(0));
            fill_entry_to(datas.get(1));
        }

        auto_from_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fill_entry_from(Utils.getdateFormatted(new Date()));
            }
        });

        new_entry_auto_date_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fill_entry_to(Utils.getdateFormatted(new Date()));
            }
        });

        Toolbar toolbar = view.findViewById(R.id.edit_entry_toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_validate:
                    String dateRemoved = new_entry_date_to.getText().toString();
                    String timeRemoved = new_entry_time_to.getText().toString();

                    String formattedDatePut = String.format("%s %s", new_entry_date_from.getText(), new_entry_time_from.getText());
                    String formattedDateRemoved = String.format("%s %s", dateRemoved, timeRemoved);

                    if (dateRemoved.isEmpty() && timeRemoved.isEmpty()) {
                        insertNewEntry(formattedDatePut, true);
                    } else if (Utils.getDateDiff(formattedDatePut, formattedDateRemoved, TimeUnit.MINUTES) > 0) {
                        if (entryId != -1)
                            dbManager.updateDatesRing(entryId, formattedDatePut, formattedDateRemoved, 0);
                        else
                            dbManager.createNewDatesRing(formattedDatePut, formattedDateRemoved, 0);
                        // Get back to the last element in the fragment stack
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    } else {
                        // If the diff time is too short, trigger this error
                        Toast.makeText(context, R.string.error_edit_entry_date, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                default:
                    return false;
            }
        });
    }
}
