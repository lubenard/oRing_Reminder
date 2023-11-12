package com.lubenard.oring_reminder.custom_components;

import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RingSession {
    public enum SessionStatus {
        NOT_RUNNING,
        RUNNING,
        IN_BREAK
    }

    private long id;
    private String datePut;
    private Calendar datePutCalendar;
    private String dateRemoved;
    private SessionStatus status;

    private ArrayList<BreakSession> breakList;

    // This value is in MINUTES
    private int timeWeared;

    /**
     * RingModel is used to simplify the transport of session in the code, and make it easier to understand

     * Ex:
     * Normal session: id: 1, datePut 2021-04-10 11:42:00, dateRemoved 2021-04-11 02:42:00, isRunning 0, time worn 900 (15h in Minutes)
     */
    public RingSession(int id, String datePut, String dateRemoved, int isRunning, int timeWeared) {
        this.id = id;
        this.datePut = datePut;
        this.dateRemoved = dateRemoved;
        this.timeWeared = timeWeared;

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(DateUtils.getdateParsed(datePut));
        this.datePutCalendar = calendarStart;

        this.status = SessionStatus.values()[isRunning];
        Log.d("RingSession", "Creating Ring session with id " + id + " and status " + this.status);

        if (this.timeWeared == 0 && this.status == SessionStatus.NOT_RUNNING) {
            this.timeWeared = (int) DateUtils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES);
        }
    }

    public long getId() {
        return id;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public String getDatePut() {
        return datePut;
    }

    public String getDateRemoved() {
        return dateRemoved;
    }

    public Calendar getDatePutCalendar() {
        return datePutCalendar;
    }

    public Calendar getDateRemovedCalendar() {
        Calendar calendar = Calendar.getInstance();
        if (this.status == SessionStatus.RUNNING) {
            Date date = new Date();
            date.setTime(0);
            calendar.setTime(date);
        } else if (this.status == SessionStatus.NOT_RUNNING){
            calendar.setTime(DateUtils.getdateParsed(dateRemoved));
        }
        return calendar;
    }

    public boolean getIsInBreak() {
        return status == SessionStatus.IN_BREAK;
    }

    public boolean getIsRunning() {
        return status == SessionStatus.RUNNING;
    }

    /**
     * Get the time worn for the session
     * Better use getSessionDuration, it update the timeWeared variable
     * @return the session time worn in minute
     */
    @Deprecated
    public int getTimeWorn() {
        return timeWeared;
    }

    public int getSessionDuration() {
        if (getIsRunning()) {
            timeWeared = (int) DateUtils.getDateDiff(datePutCalendar.getTime(), new Date(), TimeUnit.MINUTES);
            return timeWeared;
        }
        timeWeared = (int) DateUtils.getDateDiff(datePutCalendar.getTime(), getDateRemovedCalendar().getTime(), TimeUnit.MINUTES);
        return timeWeared;
    }

    public void setBreakList(ArrayList<BreakSession> breakList) {
        this.breakList = breakList;
    }

    public int computeTotalTimePause() {
        int totalTimePause = 0;
        for (int i = 0; i != breakList.size(); i++) {
            if (breakList.get(i).getIsRunning())
                totalTimePause += DateUtils.getDateDiff(breakList.get(i).getStartDateCalendar().getTime(), new Date(), TimeUnit.MINUTES);
            else
                totalTimePause += DateUtils.getDateDiff(breakList.get(i).getStartDateCalendar().getTime(), breakList.get(i).getEndDateCalendar().getTime(), TimeUnit.MINUTES);
        }
        return totalTimePause;
    }

    public void setStatus(SessionStatus newStatus) {
        status = newStatus;
    }
}
