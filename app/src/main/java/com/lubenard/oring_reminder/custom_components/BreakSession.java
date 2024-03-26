package com.lubenard.oring_reminder.custom_components;

import static com.lubenard.oring_reminder.custom_components.Session.SessionStatus.NOT_RUNNING;
import static com.lubenard.oring_reminder.custom_components.Session.SessionStatus.RUNNING;

import com.lubenard.oring_reminder.utils.DateUtils;

import java.util.concurrent.TimeUnit;

public class BreakSession extends Session {

    private final long sessionId;

    /**
     *
     * When it is a break, datePut and dateRemoved are inverted.
     * Ex:
     * Normal session: id: 1, datePut 2021-04-10 11:42:00, dateRemoved 2021-04-11 02:42:00, isRunning 0, time worn 900 (15h in Minutes)
     * Break:          id: 3, datePut 2021-04-10 16:36:00, dateRemoved 2021-04-10 14:21:00, isRunning 0, time worn 135 (2h15 in Minutes)
     */
    public BreakSession(long id, String dateRemoved, String datePut, int isRunning, long timeRemoved, long sessionId) {
        super(
              id,
              dateRemoved,
              datePut,
              (isRunning == 1) ?  RUNNING : NOT_RUNNING,
              (timeRemoved == 0 && isRunning == 0) ? DateUtils.Companion.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES) : timeRemoved
        );
        this.sessionId = sessionId;
    }

    public long getSessionId() { return sessionId; }
}
