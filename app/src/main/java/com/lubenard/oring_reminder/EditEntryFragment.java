package com.lubenard.oring_reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION.SDK_INT;

public class EditEntryFragment extends Fragment {

    private DbManager dbManager;
    private int entryId;

    private EditText new_entry_date_from;
    private EditText new_entry_time_from;
    private EditText new_entry_date_to;
    private EditText new_entry_time_to;

    private SharedPreferences sharedPreferences;
    private int weared_time;

    /**
     * This will set a alarm that will trigger a notification at alarmDate + time wearing setting
     * @param alarmDate
     */
    private void setAlarm(String alarmDate) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(alarmDate));
            calendar.add(Calendar.HOUR_OF_DAY, 15);
            Log.d("Create new entry", "Setting the alarm for this timstamp in millins " + calendar.getTimeInMillis());

            Intent intent = new Intent(getContext(), NotificationBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, intent, 0);
            AlarmManager am = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);

            if (SDK_INT < Build.VERSION_CODES.KITKAT)
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            else if (Build.VERSION_CODES.KITKAT <= SDK_INT && SDK_INT < Build.VERSION_CODES.M)
                am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            else if (SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void fill_entry_from(String date) {
        String[] slittedDate = date.split(" ");
        new_entry_date_from.setText(slittedDate[0]);
        new_entry_time_from.setText(slittedDate[1]);
    }

    private void fill_entry_to(String date) {
        String[] slittedDate = date.split(" ");
        new_entry_date_to.setText(slittedDate[0]);
        new_entry_time_to.setText(slittedDate[1]);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.edit_entry_fragment, container, false);
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

        dbManager = new DbManager(getContext());

        Bundle bundle = this.getArguments();
        entryId = bundle.getInt("entryId", -1);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));

        if (entryId != -1) {
            ArrayList<String> datas = dbManager.getEntryDetails(entryId);
            fill_entry_from(datas.get(0));
            fill_entry_to(datas.get(1));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        auto_from_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fill_entry_from(dateFormat.format(new Date()));
            }
        });

        new_entry_auto_date_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fill_entry_to(dateFormat.format(new Date()));
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
                        //TODO: Check if a already session is running, and if so, avert user

                        if (entryId != -1)
                            dbManager.updateDatesRing(entryId, formattedDatePut, "NOT SET YET", 1);
                        else
                            dbManager.createNewDatesRing(formattedDatePut, "NOT SET YET", 1);

                        if (sharedPreferences.getBoolean("myring_send_notif_when_session_over", true))
                            setAlarm(formattedDatePut);
                        // Get back to the last element in the fragment stack
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    } else if (Utils.getDateDiff(formattedDatePut, formattedDateRemoved, TimeUnit.MINUTES) > 0) {
                        if (entryId != -1)
                            dbManager.updateDatesRing(entryId, formattedDatePut, formattedDateRemoved, 0);
                        else
                            dbManager.createNewDatesRing(formattedDatePut, formattedDateRemoved, 0);
                        // Get back to the last element in the fragment stack
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    } else {
                        Toast.makeText(getContext(), R.string.error_edit_entry_date, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                default:
                    return false;
            }
        });
    }
}
