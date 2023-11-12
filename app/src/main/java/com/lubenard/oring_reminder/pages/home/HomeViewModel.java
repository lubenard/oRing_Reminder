package com.lubenard.oring_reminder.pages.home;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.managers.DbManager;
import com.lubenard.oring_reminder.managers.SessionsManager;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeViewModel extends ViewModel {
    private final static String TAG = "HomeViewModel";

    private DbManager dbManager;
    public MutableLiveData<Integer> wearingTimeSinceMidnight = new MutableLiveData<>();
    public MutableLiveData<Integer> last24hWearingTime = new MutableLiveData<>();
    public MutableLiveData<RingSession> currentSession = new MutableLiveData<>();
    public MutableLiveData<List<BreakSession>> sessionBreaks = new MutableLiveData<>(Collections.emptyList());
    public MutableLiveData<Boolean> isThereARunningBreak = new MutableLiveData<>();

    public boolean shouldUpdateDbInstance = false;

    private Handler updateHandler;
    private Runnable updateRunnable;

    public HomeViewModel() {
        Log.d(TAG, "New VM at " + this);
        dbManager = MainActivity.getDbManager();
    }

    void updateDbManager() {
        Log.d(TAG,"Updated DbManager");
        dbManager = MainActivity.getDbManager();
    }

    // TODO: To move in SessionManager
    /**
     * Compute all pause time into interval
     * @param entryId entry for the wanted session
     * @param date24HoursAgo oldest boundaries
     * @param dateNow interval newest boundaries
     * @return the time in Minutes of pauses between the interval
     */
    private int computeTotalTimePauseForId(long entryId, String date24HoursAgo, String dateNow) {
        ArrayList<BreakSession> pausesDatas = dbManager.getAllBreaksForId(entryId, true);
        int totalTimePause = 0;
        for (int i = 0; i < pausesDatas.size(); i++) {
            BreakSession currentBreak = pausesDatas.get(i);
            Log.d(TAG, "BreakSession is " + currentBreak);
            if (!pausesDatas.get(i).getIsRunning()) {
                if (DateUtils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) > 0 &&
                        DateUtils.getDateDiff(currentBreak.getEndDate(), dateNow, TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is added: " + pausesDatas.get(i).getTimeRemoved());
                    totalTimePause += currentBreak.getTimeRemoved();
                } else if (DateUtils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) <= 0 &&
                        DateUtils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "pause at index " + i + " is between the born: " + DateUtils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.SECONDS));
                    totalTimePause += DateUtils.getDateDiff(date24HoursAgo, currentBreak.getEndDate(), TimeUnit.MINUTES);
                }
            } else {
                if (DateUtils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running pause at index " + i + " is added: " + DateUtils.getDateDiff(currentBreak.getStartDate(), dateNow, TimeUnit.SECONDS));
                    totalTimePause += DateUtils.getDateDiff(currentBreak.getStartDate(), dateNow, TimeUnit.MINUTES);
                } else if (DateUtils.getDateDiff(date24HoursAgo, currentBreak.getStartDate(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running pause at index " + i + " is between the born: " + DateUtils.getDateDiff(date24HoursAgo, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimePause += DateUtils.getDateDiff(date24HoursAgo, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                }
            }
        }
        return totalTimePause;
    }

    /**
     * Get all session wearing time with the breaks removed for the last 24 hours.
     * @return time in minute of worn time on the last 24 hours
     */
    public void getLast24hWearingTime() {
        int totalTimeLastDay = 0;
        int pauseTimeForThisEntry;

        LinkedHashMap<Integer, RingSession> entrysDatas = dbManager.getAllDatasForMainList(true);
        ArrayList<RingSession> dataModels = new ArrayList<>(entrysDatas.values());

        Calendar calendar = Calendar.getInstance();
        String todayDate = DateUtils.getdateFormatted(calendar.getTime());

        // We set the calendar 24h earlier
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        String oneDayEarlier = DateUtils.getdateFormatted(calendar.getTime());
        Log.d(TAG, "Computing last 24 hours: interval is between: " + oneDayEarlier + " and " + todayDate);

        RingSession currentModel;
        for (int i = 0; i != (Math.min(dataModels.size(), 5)); i++) {
            currentModel = dataModels.get(i);
            pauseTimeForThisEntry = computeTotalTimePauseForId(currentModel.getId(), oneDayEarlier, todayDate);
            Log.d(TAG, "Session id: " + currentModel.getId()
                    + ", pauseTimeForThisEntry " + pauseTimeForThisEntry
                    + ", getDatePut: " + currentModel.getDatePut()
                    + ", getDateRemoved: " + currentModel.getDateRemoved()
                    + ", datediff datePut: " + DateUtils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) + " seconds, "
                    + ", status:  " + currentModel.getStatus());
            if (!currentModel.getIsRunning() && !currentModel.getIsInBreak()) {
                if (DateUtils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) > 0 &&
                        DateUtils.getDateDiff(currentModel.getDateRemoved(), todayDate, TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is fully in the last 24h (start and end inside 'now' and 'now - 24h')
                    Log.d(TAG, "entry at index " + i + " added " + dataModels.get(i).getTimeWorn() + " to counter");
                    totalTimeLastDay += currentModel.getTimeWorn() - pauseTimeForThisEntry;
                } else if (DateUtils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0 &&
                        DateUtils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(),  TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is half beetween interval (start before before 24h ago and end after interval start)
                    Log.d(TAG, "entry at index " + i + " is between the born: " + DateUtils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(), TimeUnit.SECONDS));
                    totalTimeLastDay += DateUtils.getDateDiff(oneDayEarlier, currentModel.getDateRemoved(), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            } else {
                if (DateUtils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running entry at index " + i + " is added: " + DateUtils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.SECONDS));
                    totalTimeLastDay += DateUtils.getDateDiff(currentModel.getDatePut(), todayDate, TimeUnit.MINUTES) - pauseTimeForThisEntry;
                } else if (DateUtils.getDateDiff(oneDayEarlier, currentModel.getDatePut(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running entry at index " + i + " is between the born: " + DateUtils.getDateDiff(oneDayEarlier, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimeLastDay += DateUtils.getDateDiff(oneDayEarlier, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            }
        }
        Log.d(TAG, "Computed last 24 hours is: " + totalTimeLastDay + "mn");
        last24hWearingTime.setValue(totalTimeLastDay);
    }

    public void computeWearingTimeSinceMidnight() {
        int totalTimeSinceMidnight = 0;
        int pauseTimeForThisEntry;

        LinkedHashMap<Integer, RingSession> entrysDatas = dbManager.getAllDatasForMainList(true);
        ArrayList<RingSession> dataModels = new ArrayList<>(entrysDatas.values());

        Calendar calendar = Calendar.getInstance();
        String todayDate = DateUtils.getdateFormatted(calendar.getTime());

        // We set the calendar at midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        String sinceMidnigt = DateUtils.getdateFormatted(calendar.getTime());
        Log.d(TAG, "Computing since midnight: interval is between: " + sinceMidnigt + " and " + todayDate);

        RingSession currentSession;

        // We estimate that looking at the last 5 sessions is enough to cover at more than 24h
        for (int i = 0; i != (Math.min(dataModels.size(), 5)); i++) {
            currentSession = dataModels.get(i);
            pauseTimeForThisEntry = computeTotalTimePauseForId(currentSession.getId(), sinceMidnigt, todayDate);
            Log.d(TAG, "Session id: " + currentSession.getId()
                    + ", pauseTimeForThisEntry " + pauseTimeForThisEntry
                    + ", getDatePut: " + currentSession.getDatePut()
                    + ", datediff datePut: " + DateUtils.getDateDiff(sinceMidnigt, currentSession.getDatePut(), TimeUnit.SECONDS) + " seconds, ");
            if (!currentSession.getIsRunning() && !currentSession.getIsInBreak()) {
                if (DateUtils.getDateDiff(sinceMidnigt, currentSession.getDatePut(), TimeUnit.SECONDS) > 0 &&
                        DateUtils.getDateDiff(currentSession.getDateRemoved(), todayDate, TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is fully in the last 24h (start and end inside 'now' and 'now - 24h')
                    Log.d(TAG, "entry at index " + i + " added " + dataModels.get(i).getTimeWorn() + " to counter");
                    totalTimeSinceMidnight += currentSession.getTimeWorn() - pauseTimeForThisEntry;
                } else if (DateUtils.getDateDiff(sinceMidnigt, currentSession.getDatePut(), TimeUnit.SECONDS) <= 0 &&
                        DateUtils.getDateDiff(sinceMidnigt, currentSession.getDateRemoved(),  TimeUnit.SECONDS) > 0) {
                    // This case happens if the session is half beetween interval (start before before 24h ago and end after interval start)
                    Log.d(TAG, "entry at index " + i + " is between the born: " + DateUtils.getDateDiff(sinceMidnigt, currentSession.getDateRemoved(), TimeUnit.SECONDS));
                    totalTimeSinceMidnight += DateUtils.getDateDiff(sinceMidnigt, currentSession.getDateRemoved(), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            } else {
                if (DateUtils.getDateDiff(sinceMidnigt, currentSession.getDatePut(), TimeUnit.SECONDS) > 0) {
                    Log.d(TAG, "running entry at index " + i + " is added: " + DateUtils.getDateDiff(currentSession.getDatePut(), todayDate, TimeUnit.SECONDS));
                    totalTimeSinceMidnight += DateUtils.getDateDiff(currentSession.getDatePut(), todayDate, TimeUnit.MINUTES) - pauseTimeForThisEntry;
                } else if (DateUtils.getDateDiff(sinceMidnigt, currentSession.getDatePut(), TimeUnit.SECONDS) <= 0) {
                    Log.d(TAG, "running entry at index " + i + " is between the born: " + DateUtils.getDateDiff(sinceMidnigt, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES));
                    totalTimeSinceMidnight += DateUtils.getDateDiff(sinceMidnigt, DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES) - pauseTimeForThisEntry;
                }
            }
        }
        Log.d(TAG, "Computed last since midnight is: " + totalTimeSinceMidnight + "mn");
        wearingTimeSinceMidnight.setValue(totalTimeSinceMidnight);
    }

    public void getCurrentSession() {
        currentSession.setValue(dbManager.getLastRunningEntry());
        if (currentSession.getValue() != null) {
            sessionBreaks.setValue(dbManager.getAllBreaksForId(currentSession.getValue().getId(), true));
            Log.d(TAG, "getCurrentSession, currentSession isInBreak say" + currentSession.getValue().getIsInBreak() + " if it in in break");
            isThereARunningBreak.setValue(currentSession.getValue().getIsInBreak());
            if (updateHandler == null || updateRunnable == null) {
                Log.d(TAG, "update Handler/Runnable is null, let's create one!");
                updateHandler = new Handler();
                updateRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Updating wearing time");
                        getCurrentSession();
                        computeWearingTimeSinceMidnight();
                        getLast24hWearingTime();
                        // Every minute update the wearing time. No need to do it more often
                        updateHandler.postDelayed(this, 60000);
                    }
                };
                updateRunnable.run();
            }
        }
    }

    public void endSession() {
        dbManager.endSession(dbManager.getLastRunningEntry().getId());
        currentSession.setValue(null);
    }

    // We use the status parameter because the break can be ended as well as the status
    public void endBreak(RingSession.SessionStatus status) {
        dbManager.endPause(dbManager.getLastRunningEntry().getId());
        dbManager.updateDatesRing(dbManager.getLastRunningEntry().getId(), null, null, status.ordinal());
        isThereARunningBreak.setValue(false);
        getCurrentSession();
    }

    public void startBreak(Context context) {
        if (isThereARunningBreak.getValue() != null && isThereARunningBreak.getValue()) {
            Log.d(TAG, "Error: Already a running pause");
            Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
        } else if (isThereARunningBreak.getValue() != null) {
            SessionsManager.startBreak(context);
            sessionBreaks.setValue(dbManager.getAllBreaksForId(currentSession.getValue().getId(), true));
            isThereARunningBreak.setValue(true);
            getCurrentSession();
        }
    }

    public void stopTimer() {
        //updateHandler.removeCallbacks(updateRunnable);
    }

    public void resetInstanceDB() {
        shouldUpdateDbInstance = true;
    }
}
