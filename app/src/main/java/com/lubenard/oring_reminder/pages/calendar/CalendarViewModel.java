package com.lubenard.oring_reminder.pages.calendar;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;

public class CalendarViewModel extends ViewModel {
    private static final String TAG = "CalendarViewModel";
    private final DbManager dbManager;

    public MutableLiveData<ArrayList<RingSession>> allSessions = new MutableLiveData<>();

    public CalendarViewModel() {
        Log.d(TAG, "Executed normally once");
        dbManager = MainActivity.getDbManager();
    }

    public void loadCalendarInfos() {
        allSessions.setValue(dbManager.getAllDatasForAllEntrys());
    }
}
