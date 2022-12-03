package com.lubenard.oring_reminder.custom_components;

import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class BreakSession {
    enum BreakStatus {
        RUNNING,
        FINISHED
    }

    private long id;
    private String datePut;
    private String dateRemoved;
    private BreakStatus status;
    private int timeRemoved;
    private long sessionId;

    /**
     *
     * When it is a break, datePut and dateRemoved are inverted.
     * Ex:
     * Normal session: id: 1, datePut 2021-04-10 11:42:00, dateRemoved 2021-04-11 02:42:00, isRunning 0, time worn 900 (15h in Minutes)
     * Break:          id: 3, datePut 2021-04-10 16:36:00, dateRemoved 2021-04-10 14:21:00, isRunning 0, time worn 135 (2h15 in Minutes)
     */
    public BreakSession(int id, String dateRemoved, String datePut, int isRunning, int timeWeared, long sessionId) {
        this.id = id;
        this.datePut = datePut;
        this.dateRemoved = dateRemoved;
        this.timeRemoved = timeWeared;
        this.sessionId = sessionId;

        if (isRunning == 1)
            this.status = BreakStatus.RUNNING;
        else
            this.status = BreakStatus.FINISHED;

        if (this.timeRemoved == 0 && this.status == BreakStatus.FINISHED) {
            this.timeRemoved = (int)Utils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES);
        }
    }

    public long getId() {
        return id;
    }

    public String getStartDate() {
        return dateRemoved;
    }

    public String getEndDate() {
        return datePut;
    }

    public Calendar getStartDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(dateRemoved));
        return calendar;
    }

    public Calendar getEndDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(datePut));
        return calendar;
    }

    public long getSessionId() { return sessionId; }

    public boolean getIsRunning() {
        return status == BreakStatus.RUNNING;
    }

    public int getTimeRemoved() {
        return timeRemoved;
    }
}
