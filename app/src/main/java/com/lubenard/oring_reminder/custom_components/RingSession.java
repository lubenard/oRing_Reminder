package com.lubenard.oring_reminder.custom_components;

import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RingSession extends Session {

    private final Calendar datePutCalendar;

    private ArrayList<BreakSession> breakList;


    /**
     * RingModel is used to simplify the transport of session in the code, and make it easier to understand

     * Ex:
     * Normal session: id: 1, datePut 2021-04-10 11:42:00, dateRemoved 2021-04-11 02:42:00, isRunning 0, time worn 900 (15h in Minutes)
     */
    public RingSession(int id, String datePut, String dateRemoved, int isRunning, long timeWeared) {
        super(
              id,
              datePut,
              dateRemoved,
              SessionStatus.values()[isRunning],
              (timeWeared == 0 && isRunning == 0) ? DateUtils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES) : timeWeared
        );

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(DateUtils.getdateParsed(datePut));
        this.datePutCalendar = calendarStart;

        Log.d("RingSession", "Creating Ring session with id " + id + " and status " + getStatus());
    }

    public Calendar getDatePutCalendar() {
        return datePutCalendar;
    }

    public Calendar getDateRemovedCalendar() {
        Calendar calendar = Calendar.getInstance();
        if (getStatus() == SessionStatus.RUNNING) {
            Date date = new Date();
            date.setTime(0);
            calendar.setTime(date);
        } else if (getStatus() == SessionStatus.NOT_RUNNING){
            calendar.setTime(DateUtils.getdateParsed(getEndDate()));
        }
        return calendar;
    }

    public boolean getIsInBreak() {
        return getStatus() == SessionStatus.IN_BREAK;
    }

    @Override
    public long getSessionDuration() {
        if (getStatus() == SessionStatus.RUNNING) {
            if (breakList.isEmpty())
                setSessionDuration(DateUtils.getDateDiff(datePutCalendar.getTime(), new Date(), TimeUnit.MINUTES));
            else
                setSessionDuration(DateUtils.getDateDiff(datePutCalendar.getTime(), new Date(), TimeUnit.MINUTES) - computeTotalTimePause());
            return super.getSessionDuration();
        }
        setSessionDuration(DateUtils.getDateDiff(datePutCalendar.getTime(), getDateRemovedCalendar().getTime(), TimeUnit.MINUTES));
        return super.getSessionDuration();
    }

    /**
     * Set the Break list associated to this Session
     * @param breakList a ArrayList of BreakSession
     */
    public void setBreakList(ArrayList<BreakSession> breakList) {
        this.breakList = breakList;
    }

    /**
     * Compute total time break for all breaks registered in the session
     * @return the total time in MINUTES
     */
    public int computeTotalTimePause() {
        int totalTimePause = 0;
        for (int i = 0; i != breakList.size(); i++) {
            if (breakList.get(i).getStatus() == SessionStatus.RUNNING)
                totalTimePause += DateUtils.getDateDiff(breakList.get(i).getStartDateCalendar().getTime(), new Date(), TimeUnit.MINUTES);
            else
                totalTimePause += DateUtils.getDateDiff(breakList.get(i).getStartDateCalendar().getTime(), breakList.get(i).getEndDateCalendar().getTime(), TimeUnit.MINUTES);
        }
        return totalTimePause;
    }

    /**
     * Return the break list
     * @return An ArrayList of BreakSessions
     */
    public ArrayList<BreakSession> getBreakList() { return breakList; }
}
