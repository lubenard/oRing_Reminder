package com.lubenard.oring_reminder.pages.entry_details;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EntryDetailsViewModel extends ViewModel {
    private static final String TAG = "EntryDetailsViewModel";
    private final DbManager dbManager;
    private long entryId;
    MutableLiveData<RingSession> session = new MutableLiveData<>();
    MutableLiveData<Long> wornTime = new MutableLiveData<>();
    MutableLiveData<ArrayList<BreakSession>> sessionBreaks = new MutableLiveData<>();
    MutableLiveData<Float> progressPercentage = new MutableLiveData<>();
    MutableLiveData<Integer> progressColor = new MutableLiveData<>();

    public EntryDetailsViewModel() {
        Log.d(TAG, "Executed normally once");
        dbManager = MainActivity.getDbManager();
    }

    public void loadCurrentSession(long entryId) {
        if (entryId > 0) {
            this.entryId = entryId;
            session.postValue(dbManager.getEntryDetails(entryId));
            recomputeWearingTime();
            //progressPercentage.postValue();
            sessionBreaks.postValue(dbManager.getAllBreaksForId(entryId, true));
        }
    }

    public void deleteSession() {
        Log.d(TAG, "Deleting entry with id " + entryId);
        dbManager.deleteEntry(entryId);
    }

    /**
     * Compute when user an get it off according to breaks.
     * If the user made a 1h30 break, then he should wear it 1h30 more
     */
    void recomputeWearingTime() {
        long oldTimeWeared;
        // If session is running,
        // OldTimeWeared is the time in minute between the starting of the entry and the current Date
        // Else, oldTimeWeared is the time between the start of the entry and it's pause
        if (session.getValue().getIsRunning())
            oldTimeWeared = DateUtils.getDateDiff(session.getValue().getDatePutCalendar().getTime(), new Date(), TimeUnit.MINUTES);
        else
            oldTimeWeared = DateUtils.getDateDiff(session.getValue().getDatePut(), session.getValue().getDateRemoved(), TimeUnit.MINUTES);

        long totalTimePause = SessionsManager.computeTotalTimePause(dbManager, entryId);
        long newComputedTime;

        // Avoid having more time pause than weared time
        if (totalTimePause > oldTimeWeared)
            totalTimePause = oldTimeWeared;

        newComputedTime = oldTimeWeared - totalTimePause;
        Log.d(TAG, "Compute newWearingTime = " + oldTimeWeared + " - " + totalTimePause + " = " + newComputedTime);
        wornTime.postValue(newComputedTime);

        // Time is computed as:
        // Date of put + number_of_hour_defined_in settings + total_time_in_pause
        newAlarmDate = (int) (weared_time * 60 + totalTimePause);
        Log.d(TAG, "New alarm date = " + newAlarmDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(entryDetailsViewModel.session.getValue().getDatePutCalendar().getTime());
        calendar.add(Calendar.MINUTE, newAlarmDate);
        updateAbleToGetItOffUI(calendar);
    }
}
