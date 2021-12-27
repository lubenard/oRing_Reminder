package com.lubenard.oring_reminder.custom_components;

public class RingSession {
    private long id;
    private String datePut;
    private String dateRemoved;
    private int isRunning;
    private int timeWeared;

    /**
     * RingModel is used to simplify the transport of session in the code, and make it easier to understand
     *
     * RingModel is also used to manage break, with only 1 difference:
     * When it is a break, datePut and dateRemoved are inverted.
     * Ex:
     * Normal session: id: 1, datePut 2021-04-10 11:42:00, dateRemoved 2021-04-11 02:42:00, isRunning 0, time worn 900 (15h in Minutes)
     * Break:          id: 3, datePut 2021-04-10 16:36:00, dateRemoved 2021-04-10 14:21:00, isRunning 0, time worn 135 (2h15 in Minutes)
     */
    public RingSession(int id, String datePut, String dateRemoved, int isRunning, int timeWeared) {
        this.id = id;
        this.datePut = datePut;
        this.dateRemoved = dateRemoved;
        this.isRunning = isRunning;
        this.timeWeared = timeWeared;
    }

    public long getId() {
        return id;
    }

    public String getDatePut() {
        return datePut;
    }

    public String getDateRemoved() {
        return dateRemoved;
    }

    public int getIsRunning() {
        return isRunning;
    }

    public int getTimeWeared() {
        return timeWeared;
    }
}
