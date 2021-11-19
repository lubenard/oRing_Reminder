package com.lubenard.oring_reminder.custom_components;

import android.net.Uri;

public class Spermograms {
    private long id;
    private Uri fileAddr;
    private String dateAdded;

    public Spermograms(int id, String dateAdded, Uri fileAddr) {
        this.id = id;
        this.dateAdded = dateAdded;
        this.fileAddr = fileAddr;
    }

    public long getId() {
        return id;
    }

    /**
     * filepath is formatted with 'file://'
     * @return return the filepath of pdf file
     */
    public Uri getFileAddr() {
        return fileAddr;
    }

    public String getDateAdded() {
        return dateAdded;
    }
}
