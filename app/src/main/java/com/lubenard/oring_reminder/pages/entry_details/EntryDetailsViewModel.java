package com.lubenard.oring_reminder.pages.entry_details;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.SessionsUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EntryDetailsViewModel extends ViewModel {
    private static final String TAG = "EntryDetailsViewModel";
    private final DbManager dbManager;
    private long entryId;
    MutableLiveData<RingSession> session = new MutableLiveData<>();
    MutableLiveData<Long> wornTime = new MutableLiveData<>();
    MutableLiveData<Calendar> estimatedEnd = new MutableLiveData<>();
    MutableLiveData<ArrayList<BreakSession>> sessionBreaks = new MutableLiveData<>();
    MutableLiveData<Integer> progressPercentage = new MutableLiveData<>();
    MutableLiveData<Integer> progressColor = new MutableLiveData<>();
    MutableLiveData<Integer> progressBarColor = new MutableLiveData<>();
    MutableLiveData<Boolean> isSessionRunning = new MutableLiveData<>();
    int wearingTimePref;
    Boolean isThereARunningPause = false;
    Runnable updateRunnable;
    Handler updateHandler;

    public EntryDetailsViewModel() {
        Log.d(TAG, "Executed normally once");
        dbManager = MainActivity.getDbManager();
    }

    public void loadBreaks() {
        sessionBreaks.setValue(dbManager.getAllBreaksForId(entryId, true));
        session.getValue().setBreakList(sessionBreaks.getValue());
    }

    public void loadSession() {
        session.setValue(dbManager.getEntryDetails(entryId));
        isSessionRunning.setValue(session.getValue().getIsRunning());
        isThereARunningPause = session.getValue().getIsInBreak();
    }

    public void loadCurrentSession(long entryId) {
        if (entryId > 0) {
            this.entryId = entryId;
            wearingTimePref = MainActivity.getSettingsManager().getWearingTimeInt();
            loadSession();
            loadBreaks();
            computeWearingTime();
            computeProgressBarDatas();
            updateHandler = new Handler();
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Updating wearing time");
                    computeWearingTime();
                    computeProgressBarDatas();
                    // Every minute update the wearing time. No need to do it more often
                    updateHandler.postDelayed(this, 60000);
                }
            };
            updateRunnable.run();
        }
    }

    public void deleteSession() {
        Log.d(TAG, "Deleting entry with id " + entryId);
        dbManager.deleteEntry(entryId);
    }

    void computeProgressBarDatas() {
        HashMap<Integer, Integer> pbDatas = SessionsUtils.computeProgressBarDatas(session.getValue(), wearingTimePref);

        progressColor.setValue(SessionsUtils.computeTextColor(session.getValue(), wearingTimePref));

        // As weird as it can sound, pbDatas only have one key value in it
        for (Integer key: pbDatas.keySet()) { progressPercentage.setValue(key); }

        // Same here
        for (Integer value: pbDatas.values()) { progressBarColor.setValue(value); }
    }

    /**
     * Compute when user get it off according to breaks.
     * If the user made a 1h30 break, then he should wear it 1h30 more
     */
    void computeWearingTime() {
        long oldTimeWeared;
        // If session is running,
        // OldTimeWeared is the time in minute between the starting of the entry and the current Date
        // Else, oldTimeWeared is the time between the start of the entry and it's pause
        if (session.getValue().getIsRunning())
            oldTimeWeared = DateUtils.getDateDiff(session.getValue().getDatePutCalendar().getTime(), new Date(), TimeUnit.MINUTES);
        else
            oldTimeWeared = DateUtils.getDateDiff(session.getValue().getDatePut(), session.getValue().getDateRemoved(), TimeUnit.MINUTES);

        long totalTimePause = session.getValue().computeTotalTimePause();
        long newComputedTime;

        Log.d(TAG, "oldTimeWeared is:" + oldTimeWeared + " ,totalTimePause is:" + totalTimePause);

        // Avoid having more time pause than weared time
        if (totalTimePause > oldTimeWeared)
            totalTimePause = oldTimeWeared;

        newComputedTime = oldTimeWeared - totalTimePause;
        Log.d(TAG, "Compute newWearingTime = " + oldTimeWeared + " - " + totalTimePause + " = " + newComputedTime);
        wornTime.setValue(newComputedTime);

        // Time is computed as:
        // number_of_hour_defined_in_settings + total_time_in_pause
        int newAlarmDate = (int) (MainActivity.getSettingsManager().getWearingTimeInt() + totalTimePause);
        Log.d(TAG, "New alarm date = " + newAlarmDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(session.getValue().getDatePutCalendar().getTime());
        calendar.add(Calendar.MINUTE, newAlarmDate);
        estimatedEnd.setValue(calendar);
    }

    public void endSession() {
        dbManager.endSession(entryId);
        isSessionRunning.setValue(false);
    }

    public void stopTimer() {
        updateHandler.removeCallbacks(updateRunnable);
    }
}
