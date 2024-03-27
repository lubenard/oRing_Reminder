package com.lubenard.oring_reminder.custom_components

import com.lubenard.oring_reminder.utils.DateUtils
import com.lubenard.oring_reminder.utils.Log
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class RingSession(
    id: Long,
    datePut: String,
    dateRemoved: String,
    isRunning: Int,
    timeWeared: Long
): Session(
    id,
    datePut,
    dateRemoved,
    SessionStatus.entries[isRunning],
    if (timeWeared == 0L && isRunning == 0) DateUtils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES) else timeWeared
) {

    private val datePutCalendar: Calendar

    private var breakList: List<BreakSession> = emptyList()


    /**
     * RingModel is used to simplify the transport of session in the code, and make it easier to understand

     * Ex:
     * Normal session: id: 1, datePut 2021-04-10 11:42:00, dateRemoved 2021-04-11 02:42:00, isRunning 0, time worn 900 (15h in Minutes)
     */
    init {
        val calendarStart = Calendar.getInstance()
        calendarStart.setTime(DateUtils.getdateParsed(datePut))
        this.datePutCalendar = calendarStart

        Log.d("RingSession", "Creating Ring session with id " + id + " and status " + getStatus())
    }

    fun getDatePutCalendar(): Calendar {
        return datePutCalendar
    }

    fun getDateRemovedCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        if (getStatus() == SessionStatus.RUNNING) {
            val date = Date()
            date.setTime(0)
            calendar.setTime(date)
        } else if (getStatus() == SessionStatus.NOT_RUNNING){
            calendar.setTime(DateUtils.getdateParsed(getEndDate()))
        }
        return calendar
    }

    fun getIsInBreak(): Boolean {
        return getStatus() == SessionStatus.IN_BREAK
    }

    override fun getSessionDuration(): Long {
        if (getStatus() == SessionStatus.RUNNING) {
            if (breakList.isEmpty())
                setSessionDuration(DateUtils.getDateDiff(datePutCalendar.time,  Date(), TimeUnit.MINUTES))
            else
                setSessionDuration(DateUtils.getDateDiff(datePutCalendar.time,  Date(), TimeUnit.MINUTES) - computeTotalTimePause())
            return super.getSessionDuration()
        }
        setSessionDuration(DateUtils.getDateDiff(datePutCalendar.time, getDateRemovedCalendar().time, TimeUnit.MINUTES))
        return super.getSessionDuration()
    }

    /**
     * Set the Break list associated to this Session
     * @param breakList a ArrayList of BreakSession
     */
    fun setBreakList(breakList: ArrayList<BreakSession>) {
        this.breakList = breakList
    }

    /**
     * Compute total time break for all breaks registered in the session
     * @return the total time in MINUTES
     */
    fun computeTotalTimePause(): Int {
        var totalTimePause = 0
        breakList.forEach { breakSession ->
            totalTimePause += if (breakSession.getStatus() == SessionStatus.RUNNING)
                DateUtils.getDateDiff(breakSession.getStartDateCalendar().time, Date(), TimeUnit.MINUTES).toInt()
            else
                DateUtils.getDateDiff(breakSession.getStartDateCalendar().time, breakSession.getEndDateCalendar().time, TimeUnit.MINUTES).toInt()
        }
        return totalTimePause
    }

    /**
     * Return the break list
     * @return An ArrayList of BreakSessions
     */
    fun getBreakList(): List<BreakSession> { return breakList }
}
