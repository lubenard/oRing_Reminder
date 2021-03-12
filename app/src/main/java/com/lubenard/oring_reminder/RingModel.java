package com.lubenard.oring_reminder;

import android.media.Image;

public class RingModel {
    private int id;
    private String datePut;
    private String dateRemoved;

    public RingModel(int id, String datePut, String dateRemoved) {
        this.id = id;
        this.datePut = datePut;
        this.dateRemoved = dateRemoved;
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
}
