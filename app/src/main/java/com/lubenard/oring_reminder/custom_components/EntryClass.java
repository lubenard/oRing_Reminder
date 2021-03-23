package com.lubenard.oring_reminder.custom_components;

public class EntryClass {
    private int id;
    private int isRunning;
    private String dateTimePut;
    private String dateTimeRemoved;
    private int timeWeared;

    public EntryClass(int id, int isRunning, String dateTimePut, String dateTimeRemoved, int timeWeard) {
        this.id = id;
        this.isRunning = isRunning;
        this.dateTimePut = dateTimePut;
        this.dateTimeRemoved = dateTimeRemoved;
        this.timeWeared = timeWeard;
    }

    public int getId() {
        return id;
    }
    public int getIsRunning() {
        return isRunning;
    }
    public String getDateTimePut() {
        return dateTimePut;
    }
    public String getDateTimeRemoved() {
        return dateTimeRemoved;
    }
    public int getTimeWeared() {
        return timeWeared;
    }
}
