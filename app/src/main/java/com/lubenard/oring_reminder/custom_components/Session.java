package com.lubenard.oring_reminder.custom_components;

import com.lubenard.oring_reminder.utils.DateUtils;

import java.util.Calendar;

public class Session {

    public enum SessionStatus {
        NOT_RUNNING,
        RUNNING,
        IN_BREAK
    }

    private long id;
    private String dateStart;
    private String dateEnd;

    private long sessionDuration;

    private SessionStatus status;

    public Session(long id, String dateStart, String dateEnd, SessionStatus status, long sessionDuration) {
        this.id = id;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.status = status;
        this.sessionDuration = sessionDuration;
    }

    // Id (Only a getter, a id cannot be changed after session is created)
    public long getId() { return id; }

    // Start date (Only a getter, a id cannot be changed after session is created)
    public String getStartDate() { return dateStart; }

    public Calendar getStartDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.getdateParsed(dateStart));
        return calendar;
    }

    // End date (Only a getter, a id cannot be changed after session is created)
    public String getEndDate() { return dateEnd; }

    public Calendar getEndDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.getdateParsed(dateEnd));
        return calendar;
    }

    // Session duration
    public long getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(long sessionDuration) { this.sessionDuration = sessionDuration; }

    // Status
    public SessionStatus getStatus() { return status; }

    public void setStatus(RingSession.SessionStatus newStatus) { status = newStatus; }
}
