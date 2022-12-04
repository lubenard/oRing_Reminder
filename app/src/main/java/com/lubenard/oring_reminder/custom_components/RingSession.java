package com.lubenard.oring_reminder.custom_components;

import com.lubenard.oring_reminder.utils.Utils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class RingSession {
    public enum SessionStatus {
        NOT_RUNNING,
        RUNNING,
        IN_BREAK
    }

    private long id;
    private String datePut;
    private String dateRemoved;
    private SessionStatus status;
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

        if (isRunning == 1)
            this.status = SessionStatus.RUNNING;
        else
            this.status = SessionStatus.NOT_RUNNING;

        if (this.timeWeared == 0 && this.status == SessionStatus.NOT_RUNNING) {
            this.timeWeared = (int)Utils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES);
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(datePut));
        return calendar;
    }

    public Calendar getDateRemovedCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Utils.getdateParsed(dateRemoved));
        return calendar;
    }

    public boolean getIsInBreak() {
        return status == SessionStatus.IN_BREAK;
    }

    public boolean getIsRunning() {
        return status == SessionStatus.RUNNING;
    }

    public int getTimeWeared() {
        return timeWeared;
    }
}
