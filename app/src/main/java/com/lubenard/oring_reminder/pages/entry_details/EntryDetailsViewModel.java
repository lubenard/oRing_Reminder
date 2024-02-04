package com.lubenard.oring_reminder.pages.entry_details;

import android.os.Handler;

import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.SessionsUtils;

import java.util.Calendar;
import java.util.Date;

public class EntryDetailsViewModel extends ViewModel {
    private static final String TAG = "EntryDetailsViewModel";
    private final DbManager dbManager;
    private long entryId;
    MutableLiveData<RingSession> session = new MutableLiveData<>();
    MutableLiveData<Long> wornTime = new MutableLiveData<>();
    MutableLiveData<Calendar> estimatedEnd = new MutableLiveData<>();
    MutableLiveData<Integer> progressPercentage = new MutableLiveData<>();
    int progressColor;
    int wearingTimePref;
    Boolean isThereARunningPause = false;
    Runnable updateRunnable;
    Handler updateHandler;

    public EntryDetailsViewModel() {
        Log.d(TAG, "Executed normally once");
        dbManager = MainActivity.getDbManager();
    }


    public void loadSession() {
        session.setValue(dbManager.getEntryDetails(entryId));
        session.getValue().setBreakList(dbManager.getAllBreaksForId(entryId, true));
        isThereARunningPause = session.getValue().getIsInBreak();
    }

    public void loadCurrentSession(long entryId) {
        if (entryId >= 0) {
            this.entryId = entryId;
            wearingTimePref = MainActivity.getSettingsManager().getWearingTimeInt();
            loadSession();
            computeProgressBarDatas();
            wornTime.setValue(SessionsUtils.computeWornTime(session.getValue()));
            estimatedEnd.setValue(SessionsUtils.computeEstimatedEnd(session.getValue()));
            updateHandler = new Handler();
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Updating wearing time");
                    wornTime.setValue(SessionsUtils.computeWornTime(session.getValue()));
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
        Pair<Integer, Integer> pbDatas = SessionsUtils.computeProgressBarDatas(session.getValue(), wearingTimePref);

        progressPercentage.setValue(pbDatas.first);
        progressColor = pbDatas.second;
    }

    public void endSession() {
        dbManager.endSession(entryId);
        session.getValue().setEndDate(new Date());
        session.getValue().setStatus(Session.SessionStatus.NOT_RUNNING);
        computeProgressBarDatas();
        session.setValue(session.getValue());
    }

    public void stopTimer() {
        updateHandler.removeCallbacks(updateRunnable);
    }
}
