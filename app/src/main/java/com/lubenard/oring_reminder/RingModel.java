package com.lubenard.oring_reminder;

import android.media.Image;

public class RingModel {
    private int id;
    private String datePut;
    private String dateRemoved;
    private int isRunning;
    private int timeWeared;

    public RingModel(int id, String datePut, String dateRemoved, int isRunning, int timeWeared) {
        this.id = id;
        this.datePut = datePut;
        this.dateRemoved = dateRemoved;
        this.isRunning = isRunning;
        this.timeWeared = timeWeared;
    }

    public int getId() {
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
