package com.lubenard.oring_reminder.custom_components;

import android.net.Uri;

public class Spermograms {
    private final long id;
    private final Uri fileAddr;
    private final String dateAdded;

    /**
     * Class used to represent a Spermogram
     * @param id internal id
     * @param dateAdded date of the Spermogram
     * @param fileAddr Local file address (e.g: file:///sdcard/0/Documents/Spermo123.pdf)
     */
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
