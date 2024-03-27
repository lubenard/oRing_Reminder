package com.lubenard.oring_reminder.custom_components

import com.lubenard.oring_reminder.custom_components.Session.SessionStatus.NOT_RUNNING
import com.lubenard.oring_reminder.custom_components.Session.SessionStatus.RUNNING

import com.lubenard.oring_reminder.utils.DateUtils

import java.util.concurrent.TimeUnit

class BreakSession(
    id: Long,
    dateRemoved: String,
    datePut: String,
    isRunning: Int,
    timeRemoved: Long,
    sessionId: Long
): Session(
    id,
    dateRemoved,
    datePut,
    if (isRunning == 1) RUNNING else NOT_RUNNING,
    if (timeRemoved == 0L && isRunning == 0) DateUtils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES) else timeRemoved
) {

    private val sessionId: Long

    /**
     *
     * When it is a break, datePut and dateRemoved are inverted.
     * Ex:
     * Normal session: id: 1, datePut 2021-04-10 11:42:00, dateRemoved 2021-04-11 02:42:00, isRunning 0, time worn 900 (15h in Minutes)
     * Break:          id: 3, datePut 2021-04-10 16:36:00, dateRemoved 2021-04-10 14:21:00, isRunning 0, time worn 135 (2h15 in Minutes)
     */
    init {
        this.sessionId = sessionId
    }

    fun getSessionId(): Long { return sessionId }
}
