package com.lubenard.oring_reminder.ui.viewHolders;

import android.content.Context;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.ui.adapters.CalendarItemAdapter;
import com.lubenard.oring_reminder.pages.calendar.CalendarFragment;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "CalendarViewHolder";

    private GridView calendarGridDays;
    private TextView calendarMonth;
    private int todayIndex = -1;
    private FragmentActivity activity;
    private CalendarFragment calendarFragment;

    public CalendarViewHolder(@NonNull View itemView, FragmentActivity activity, Context context, CalendarFragment calendarFragment) {
        super(itemView);

        this.activity = activity;
        calendarGridDays = itemView.findViewById(R.id.calendarGridDays);
        calendarMonth = itemView.findViewById(R.id.calendarMonth);
        this.calendarFragment = calendarFragment;
    }

    /**
     * Update the history element
     * @param date date to base data on
     * @param context context
     */
    public void updateDatas(Calendar date, ArrayList<RingSession> sessions, Context context) {
        Log.d(TAG, "Received date" + date);

        calendarMonth.setText(String.format("%s %d", date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), date.get(Calendar.YEAR)));

        int calendarOffset = getIndexOfFirstDayInMonth(context, date);

        ArrayList<String> num = listOfDatesInMonth(date, calendarOffset);

        Log.d(TAG, "Calendar offset is " + calendarOffset);

        Log.d(TAG, "List of days in month is " + num.size() + " for " + String.format("%s %d", date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), date.get(Calendar.YEAR)));

        if (num.size() > 35) {
            calendarGridDays.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, context.getResources().getDisplayMetrics());
        } else {
            calendarGridDays.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 210, context.getResources().getDisplayMetrics());
        }
        Calendar calendar = Calendar.getInstance();

        List<Pair<Integer, RingSession>> mappedSessions = new ArrayList<>();

        Log.d(TAG, "Have " + sessions.size() + " sessions in this month");

        for (int i = 0; i != sessions.size(); i++) {
            Log.d(TAG, "Adding session numero " + i + " to hashmap, with key: " + calendar.get(Calendar.DAY_OF_MONTH));
            calendar.setTime(sessions.get(i).getDatePutCalendar().getTime());
            Log.d(TAG, "calendar time is now " + calendar.getTime().getTime());
            mappedSessions.add(new Pair<>(calendar.get(Calendar.DAY_OF_MONTH), sessions.get(i)));
        }

        Calendar todayDate = Calendar.getInstance();

        Log.d(TAG, "Lubenard: date say: " + DateUtils.getdateFormatted(date.getTime()) + ", todayDate say: " + DateUtils.getdateFormatted(todayDate.getTime()));

        if (date.get(Calendar.YEAR) == todayDate.get(Calendar.YEAR)
                && date.get(Calendar.MONTH) == todayDate.get(Calendar.MONTH)) {
            todayIndex = todayDate.get(Calendar.DAY_OF_MONTH);
        } else
            todayIndex = -1;

        final CalendarItemAdapter adapter = new CalendarItemAdapter(activity, calendarFragment, context, num, mappedSessions, calendarOffset, todayIndex, date);

        calendarGridDays.setAdapter(adapter);
    }

    ArrayList<String> listOfDatesInMonth(Calendar selectedMonthFirstDay, int calendarOffset) {
        Log.d(TAG, "listOfDatesInMonth: selectedMonthFirstDay is " + selectedMonthFirstDay.getActualMaximum(Calendar.DAY_OF_MONTH));

        ArrayList<String> list = new ArrayList<>();

        Log.d(TAG, "Calendar offset is " + calendarOffset);

        if (calendarOffset > 0)
            for (int j = 0; j != calendarOffset; j++) { list.add("0"); }

        for (int i = 1; i < selectedMonthFirstDay.getActualMaximum(Calendar.DAY_OF_MONTH) + 1; i++) {
            //Log.d(TAG, "Adding " + i + " to list days");
            list.add(String.valueOf(i));
        }

        return (list);
    }

    int getIndexOfFirstDayInMonth(Context context, Calendar currentDate) {
        Log.d(TAG, "currentDate: " + currentDate);

        String[] daysOfWeek = {context.getString(R.string.mon), context.getString(R.string.tue),
                                context.getString(R.string.wed), context.getString(R.string.thu),
                                context.getString(R.string.fri), context.getString(R.string.sat),
                                context.getString(R.string.sun)};

        String day = new SimpleDateFormat("EEE").format(currentDate.getTime()).toUpperCase();
        Log.d(TAG, "current day is " + day + " return " + Arrays.asList(daysOfWeek).indexOf(day));
        return Arrays.asList(daysOfWeek).indexOf(day);
    }
}
