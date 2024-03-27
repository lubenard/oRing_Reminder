package com.lubenard.oring_reminder.custom_components

import com.lubenard.oring_reminder.utils.DateUtils

import java.util.Calendar
import java.util.Date

open class Session(id: Long, dateStart: String, dateEnd: String, status: SessionStatus, sessionDuration: Long) {

    enum class SessionStatus {
        NOT_RUNNING,
        RUNNING,
        IN_BREAK
    }

    private val id: Long
    private val dateStart: String
    private var dateEnd: String

    private var sessionDuration: Long

    private var status: SessionStatus

    /**
     * This class is a template used to create Ring AND Break sessions.
     * Theses share many properties in common.
     * Contains all the basics properties: id, start date, end date, status, duration
     */
    init {
        this.id = id
        this.dateStart = dateStart
        this.dateEnd = dateEnd
        this.status = status
        this.sessionDuration = sessionDuration
    }

    // Id (Only a getter, a id cannot be changed after session is created)
    fun getId(): Long { return id }

    // Start date (Only a getter, a start date cannot be changed after session is created)
    //TODO: change this behavior ?
    fun getStartDate(): String { return dateStart }

    fun getStartDateCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.setTime(DateUtils.getdateParsed(dateStart))
        return calendar
    }

    // End date
    fun getEndDate(): String { return dateEnd }

    fun getEndDateCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.setTime(DateUtils.getdateParsed(dateEnd))
        return calendar
    }

    fun setEndDate(date: Date) {
        dateEnd = DateUtils.getdateFormatted(date)
    }

    // Session duration
    open fun getSessionDuration(): Long { return sessionDuration }
    fun setSessionDuration(sessionDuration: Long) { this.sessionDuration = sessionDuration }

    // Status
    fun getStatus(): SessionStatus { return status }

    fun setStatus(newStatus: SessionStatus) { status = newStatus }
}
