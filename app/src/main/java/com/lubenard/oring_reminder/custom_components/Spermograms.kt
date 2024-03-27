package com.lubenard.oring_reminder.custom_components

import android.net.Uri

class Spermograms(id: Long, dateAdded: String, fileAddr: Uri) {

    private val id: Long
    private val fileAddr: Uri
    private val dateAdded: String

    /**
     * Class used to represent a Spermogram
     * @param id internal id
     * @param dateAdded date of the Spermogram
     * @param fileAddr Local file address (e.g: file:///sdcard/0/Documents/Spermo123.pdf)
     */
    init {
        this.id = id
        this.dateAdded = dateAdded
        this.fileAddr = fileAddr
    }

    fun getId(): Long {
        return id
    }

    /**
     * filepath is formatted with 'file://'
     * @return return the filepath of pdf file
     */
    fun getFileAddr(): Uri {
        return fileAddr
    }

    fun getDateAdded(): String {
        return dateAdded
    }
}
