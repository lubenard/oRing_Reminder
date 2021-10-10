package com.lubenard.oring_reminder.ui;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.datas_fragment, container, false);
    }

    /**
     * Compute all pause time into interval
     * @param dbManager The database manager, avoiding to create a new instance
     * @param entryId entry for the wanted session
     * @param date24HoursAgo oldest boundaries
     * @param dateNow interval newest boundaries
     * @return the time in Minutes of pauses between the interval
     */
    public static int computeTotalTimePauseForId(DbManager dbManager, long entryId, String date24HoursAgo, String dateNow) {
        ArrayList<RingSession> pausesDatas = dbManager.getAllPausesForId(entryId, true);
        int totalTimePause = 0;
        for (int i = 0; i < pausesDatas.size(); i++) {
            RingSession currentBreak = pausesDatas.get(i);
            if (pausesDatas.get(i).getIsRunning() == 0) {
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

    /**
     * Recompute last 24 h textView, removing break from the computation
     */
    private static int recomputeLastWearingTime(int hour_since, int minute_since) {
        ArrayList<RingSession> dataModels = new ArrayList<>();
        int totalTimeLastDay = 0;
        int pauseTimeForThisEntry = 0;
        DbManager dbManager = MainActivity.getDbManager();

        LinkedHashMap<Integer, RingSession> entrysDatas = dbManager.getAllDatasForMainList(true);
        for (LinkedHashMap.Entry<Integer, RingSession> oneElemData : entrysDatas.entrySet())
            dataModels.add(oneElemData.getValue());

        Calendar calendar = Calendar.getInstance();
        String todayDate = Utils.getdateFormatted(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, hour_since);
        if (minute_since > 0)
            calendar.add(Calendar.MINUTE, minute_since);
        String last24Hours = Utils.getdateFormatted(calendar.getTime());
        Log.d(TAG, "Computing last 24 hours: interval is between: " + last24Hours + " and " + todayDate);
        RingSession currentModel;
        for (int i = 0; i != ((dataModels.size() > 5) ? 5 : dataModels.size()); i++) {
            currentModel = dataModels.get(i);
            pauseTimeForThisEntry = computeTotalTimePauseForId(dbManager, currentModel.getId(), last24Hours, todayDate);
            if (currentModel.getIsRunning() == 0) {
                if (Utils.getDateDiff(last24Hours, currentModel.getDatePut(), TimeUnit.SECONDS) > 0 &&
                        Utils.getDateDiff(currentModel.getDateRemoved(), todayDate, TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "entry at index " + i + " is added: " + dataModels.get(i).getTimeWeared());
                    totalTimeLastDay += currentModel.getTimeWeared() - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(last24Hours, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0 &&
                        Utils.getDateDiff(last24Hours, currentModel.getDateRemoved(),  TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "entry at index " + i + " is between the born: " + Utils.getDateDiff(last24Hours, currentModel.getDateRemoved(), TimeUnit.SECONDS));
                    totalTimeLastDay += Utils.getDateDiff(last24Hours, currentModel.getDateRemoved(), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            } else {
                if (Utils.getDateDiff(last24Hours, currentModel.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running entry at index " + i + " is added: " + Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.SECONDS));
                    totalTimeLastDay += Utils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.MINUTES) - pauseTimeForThisEntry;
                } else if (Utils.getDateDiff(last24Hours, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running entry at index " + i + " is between the born: " + Utils.getDateDiff(last24Hours, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimeLastDay += Utils.getDateDiff(last24Hours, Utils.getdateFormatted(new Date()), TimeUnit.MINUTES) - pauseTimeForThisEntry;
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

        DbManager dbManager = MainActivity.getDbManager();

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
            timeBetweenLastAndFirstData = String.format("Time worn approximately:\n" + weeks + " weeks,\n "+ days + " days,\n " + hours + " hours,\n " + minutes + " minutes");

            Calendar c = Calendar.getInstance();

            int totalTimeSinceMidnight = recomputeLastWearingTime(c.get(Calendar.HOUR_OF_DAY) * -1, c.get(Calendar.MINUTE) * -1);
            wornSinceMidnightData = getString(R.string.since_midnight_worn_for) + String.format("%dh%02dm", totalTimeSinceMidnight / 60, totalTimeSinceMidnight % 60);

            int totalTimeLastDay = recomputeLastWearingTime(-24, 0);
            wornLast24hoursData = getString(R.string.last_day_string_header) + String.format("%dh%02dm", totalTimeLastDay / 60, totalTimeLastDay % 60);
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
