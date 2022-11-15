package com.lubenard.oring_reminder.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class DatasFragment extends Fragment {

    private static final String TAG = "DataFragment";

    private DbManager dbManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.datas_fragment, container, false);
    }

    /**
     * Compute all pause time into interval
     * @param entryId entry for the wanted session
     * @param date24HoursAgo oldest boundaries
     * @param dateNow interval newest boundaries
     * @return the time in Minutes of pauses between the interval
     */
    public int computeTotalTimePauseForId(long entryId, String date24HoursAgo, String dateNow) {
        ArrayList<RingSession> pausesDatas = dbManager.getAllPausesForId(entryId, true);
        int totalTimePause = 0;
        for (int i = 0; i < pausesDatas.size(); i++) {
            RingSession currentBreak = pausesDatas.get(i);
            if (!pausesDatas.get(i).getIsRunning()) {
                if (Utils.getDateDiff(date24HoursAgo, currentBreak.getDateRemoved(), TimeUnit.SECONDS) > 0 &&
                        Utils.getDateDiff(currentBreak.getDatePut(), dateNow, TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is added: " + pausesDatas.get(i).getTimeWeared());
                    totalTimePause += currentBreak.getTimeWeared();
                } else if (Utils.getDateDiff(date24HoursAgo, currentBreak.getDateRemoved(), TimeUnit.SECONDS) <= 0 &&
                        Utils.getDateDiff(date24HoursAgo, currentBreak.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is between the born: " + Utils.getDateDiff(date24HoursAgo, currentBreak.getDatePut(), TimeUnit.SECONDS));
                    totalTimePause += Utils.getDateDiff(date24HoursAgo, currentBreak.getDatePut(), TimeUnit.MINUTES);
                }
            } else {
                if (Utils.getDateDiff(date24HoursAgo, currentBreak.getDateRemoved(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running pause at index " + i + " is added: " + Utils.getDateDiff(currentBreak.getDateRemoved(), dateNow, TimeUnit.SECONDS));
                    totalTimePause += Utils.getDateDiff(currentBreak.getDateRemoved(), dateNow, TimeUnit.MINUTES);
                } else if (Utils.getDateDiff(date24HoursAgo, currentBreak.getDateRemoved(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running pause at index " + i + " is between the born: " + Utils.getDateDiff(date24HoursAgo, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimePause += Utils.getDateDiff(date24HoursAgo, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                }
            }
        }
        return totalTimePause;
    }

    private int getSinceMidnightWearingTime() {
        int totalTimeSinceMidnight = 0;
        int pauseTimeForThisEntry;

        LinkedHashMap<Integer, RingSession> entrysDatas = dbManager.getAllDatasForMainList(true);
        ArrayList<RingSession> dataModels = new ArrayList<>(entrysDatas.values());

        Calendar calendar = Calendar.getInstance();
        String todayDate = Utils.getdateFormatted(calendar.getTime());

        // We set the calendar at midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        String sinceMidnigt = Utils.getdateFormatted(calendar.getTime());
        Log.d(TAG, "Computing since midnight: interval is between: " + sinceMidnigt + " and " + todayDate);

        RingSession currentModel;
        for (int i = 0; i != (Math.min(dataModels.size(), 5)); i++) {
            currentModel = dataModels.get(i);
            pauseTimeForThisEntry = computeTotalTimePauseForId(currentModel.getId(), sinceMidnigt, todayDate);
            Log.d(TAG, "Session id: " + currentModel.getId()
                    + ", pauseTimeForThisEntry " + pauseTimeForThisEntry
                    + ", getDatePut: " + currentModel.getDatePut()
                    + ", datediff datePut: " + Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) + " seconds, "
                    + ", datediff dateRemoved: " + Utils.getDateDiff(currentModel.getDateRemoved(), todayDate, TimeUnit.SECONDS));
            if (!currentModel.getIsRunning()) {
                if (Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) > 0 &&
                        Utils.getDateDiff(currentModel.getDateRemoved(), todayDate, TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is fully in the last 24h (start and end inside 'now' and 'now - 24h')
                    Log.d(TAG, "entry at index " + i + " added " + dataModels.get(i).getTimeWeared() + " to counter");
                    totalTimeSinceMidnight += currentModel.getTimeWeared() - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0 &&
                        Utils.getDateDiff(sinceMidnigt, currentModel.getDateRemoved(),  TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is half beetween interval (start before before 24h ago and end after interval start)
                    Log.d(TAG, "entry at index " + i + " is between the born: " + Utils.getDateDiff(sinceMidnigt, currentModel.getDateRemoved(), TimeUnit.SECONDS));
                    totalTimeSinceMidnight += Utils.getDateDiff(sinceMidnigt, currentModel.getDateRemoved(), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            } else {
                if (Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running entry at index " + i + " is added: " + Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.SECONDS));
                    totalTimeSinceMidnight += Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.MINUTES) - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(sinceMidnigt, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running entry at index " + i + " is between the born: " + Utils.getDateDiff(sinceMidnigt, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimeSinceMidnight += Utils.getDateDiff(sinceMidnigt, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            }
        }
        Log.d("DataFragment", "Computed last since midnight is: " + totalTimeSinceMidnight + "mn");
        return totalTimeSinceMidnight;
    }

    /**
     * Get all session wearing time with the breaks removed for the last 24 hours.
     * @return time in minute of worn time on the last 24 hours
     */
    private int getLast24hWearingTime() {
        int totalTimeLastDay = 0;
        int pauseTimeForThisEntry;

        LinkedHashMap<Integer, RingSession> entrysDatas = dbManager.getAllDatasForMainList(true);
        ArrayList<RingSession> dataModels = new ArrayList<>(entrysDatas.values());

        Calendar calendar = Calendar.getInstance();
        String todayDate = Utils.getdateFormatted(calendar.getTime());

        // We set the calendar 24h earlier
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        String oneDayEarlier = Utils.getdateFormatted(calendar.getTime());
        Log.d(TAG, "Computing last 24 hours: interval is between: " + oneDayEarlier + " and " + todayDate);

        RingSession currentModel;
        for (int i = 0; i != (Math.min(dataModels.size(), 5)); i++) {
            currentModel = dataModels.get(i);
            pauseTimeForThisEntry = computeTotalTimePauseForId(currentModel.getId(), oneDayEarlier, todayDate);
            Log.d(TAG, "Session id: " + currentModel.getId()
                    + ", pauseTimeForThisEntry " + pauseTimeForThisEntry
                    + ", getDatePut: " + currentModel.getDatePut()
                    + ", datediff datePut: " + Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) + " seconds, "
                    + ", datediff dateRemoved: " + Utils.getDateDiff(currentModel.getDateRemoved(), todayDate, TimeUnit.SECONDS));
            if (!currentModel.getIsRunning()) {
                if (Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) > 0 &&
                        Utils.getDateDiff(currentModel.getDateRemoved(), todayDate, TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is fully in the last 24h (start and end inside 'now' and 'now - 24h')
                    Log.d(TAG, "entry at index " + i + " added " + dataModels.get(i).getTimeWeared() + " to counter");
                    totalTimeLastDay += currentModel.getTimeWeared() - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0 &&
                        Utils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(),  TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is half beetween interval (start before before 24h ago and end after interval start)
                    Log.d(TAG, "entry at index " + i + " is between the born: " + Utils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(), TimeUnit.SECONDS));
                    totalTimeLastDay += Utils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            } else {
                if (Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running entry at index " + i + " is added: " + Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.SECONDS));
                    totalTimeLastDay += Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.MINUTES) - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running entry at index " + i + " is between the born: " + Utils.getDateDiff(oneDayEarlier, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimeLastDay += Utils.getDateDiff(oneDayEarlier, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            }
        }
        Log.d("DataFragment", "Computed last 24 hours is: " + totalTimeLastDay + "mn");
        return totalTimeLastDay;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getActivity().setTitle(R.string.data_fragment_title);

        dbManager = MainActivity.getDbManager();

        ArrayList<RingSession> datas = dbManager.getAllDatasForAllEntrys();

        TextView numberOfEntries = view.findViewById(R.id.number_of_entries);
        TextView lastEntry = view.findViewById(R.id.last_entry);
        TextView firstEntry = view.findViewById(R.id.first_entry);
        TextView timeBetweenFirstAndLastEntry = view.findViewById(R.id.converted_time_between_first_and_last_entries);
        TextView timeWornSinceMidnight = view.findViewById(R.id.time_worn_since_midnight);
        TextView timeWornLast24h = view.findViewById(R.id.time_worn_last_24h);

        numberOfEntries.setText(getString(R.string.number_of_entries)+ datas.size());
        String lastEntryData;
        String firstEntryData;
        String timeBetweenLastAndFirstData;
        String wornSinceMidnightData;
        String wornLast24hoursData;

        if (datas.size() > 0) {
            lastEntryData = datas.get(datas.size() - 1).getDatePut().split(" ")[0];
            firstEntryData = datas.get(0).getDatePut().split(" ")[0];

            int seconds = (int)Utils.getDateDiff(datas.get(0).getDatePut(), datas.get(datas.size() - 1).getDatePut(), TimeUnit.SECONDS);
            int weeks = seconds / 604800;
            int days = (seconds % 604800) / 86400;
            int hours = ((seconds % 604800) % 86400) / 3600;
            int minutes = (((seconds % 604800) % 86400) % 3600) / 60;
            timeBetweenLastAndFirstData = String.format(getString(R.string.time_worn_appr), weeks, days, hours, minutes);

            int totalTimeSinceMidnight = getSinceMidnightWearingTime();
            wornSinceMidnightData = String.format(getString(R.string.since_midnight_worn_for) + "%dh%02dm", totalTimeSinceMidnight / 60, totalTimeSinceMidnight % 60);

            int totalTimeLastDay = getLast24hWearingTime();
            wornLast24hoursData = String.format(getString(R.string.last_day_string_header) + "%dh%02dm", totalTimeLastDay / 60, totalTimeLastDay % 60);
        } else {
            lastEntryData = getString(R.string.not_set_yet);
            firstEntryData = getString(R.string.not_set_yet);
            timeBetweenLastAndFirstData = getString(R.string.not_set_yet);
            wornSinceMidnightData = getString(R.string.not_set_yet);
            wornLast24hoursData = getString(R.string.not_set_yet);
        }

        lastEntry.setText(getString(R.string.last_entry) + "\n" + lastEntryData);
        firstEntry.setText(getString(R.string.first_entry) + "\n" + firstEntryData);
        timeBetweenFirstAndLastEntry.setText(timeBetweenLastAndFirstData);
        timeWornSinceMidnight.setText(wornSinceMidnightData);
        timeWornLast24h.setText(wornLast24hoursData);
    }
}
