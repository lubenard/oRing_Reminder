package com.lubenard.oring_reminder.pages.entry_details;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.managers.SessionsAlarmsManager;
import com.lubenard.oring_reminder.ui.fragments.EditBreakFragment;
import com.lubenard.oring_reminder.ui.fragments.EditEntryFragment;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EntryDetailsFragment extends Fragment {

    private static final String TAG = "EntryDetailsFragment";

    private TextView whenGetItOff;
    private TextView textview_progress;
    private TextView textview_total_time;
    private TextView textview_percentage_progression;
    private FloatingActionButton stopSessionButton;
    private CircularProgressIndicator progressBar;
    private LinearLayout break_layout;
    private LinearLayout end_session;
    private LinearLayout estimated_end;
    private TextView put;
    private TextView removed;
    private TextView estimated_end_date;
    private TextView total_breaks;
    private TextView total_time_breaks;

    private EntryDetailsViewModel entryDetailsViewModel;
    private Context context;
    private FragmentManager fragmentManager;

    private long entryId = -1;

    private final MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.menu_entry_details, menu);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.action_edit_entry:
                    EditEntryFragment fragment = new EditEntryFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong("entryId", entryId);
                    fragment.setArguments(bundle);
                    getChildFragmentManager().setFragmentResultListener("EditEntryFragmentResult", getViewLifecycleOwner(), (requestKey, bundle1) -> {
                        boolean result = bundle1.getBoolean("shouldUpdateParent", true);
                        Log.d(TAG, "got result from fragment: " + result);
                        if (result)
                            entryDetailsViewModel.loadCurrentSession(entryId);
                    });
                    fragment.show(getChildFragmentManager(), null);
                    return true;
                case R.id.action_delete_entry:
                    // Warn user then delete entry in the db
                    new AlertDialog.Builder(context)
                        .setTitle(R.string.alertdialog_delete_entry)
                        .setMessage(R.string.alertdialog_delete_contact_body)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            entryDetailsViewModel.deleteSession();
                            fragmentManager.popBackStackImmediate();
                        }).show();
                    return true;
                default:
                    return false;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.entry_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = getContext();
        fragmentManager = getActivity().getSupportFragmentManager();

        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        if (entryId < 0) {
            // Trigger an error if the entryId is wrong, then go back to main list
            Toast.makeText(context, context.getString(R.string.error_bad_id_entry_details) + entryId, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: Wrong Id: " + entryId);
            requireActivity().onBackPressed();
        }

        break_layout = view.findViewById(R.id.listview_pauses);

        stopSessionButton = view.findViewById(R.id.button_finish_session);

        end_session = view.findViewById(R.id.details_entry_end);
        estimated_end = view.findViewById(R.id.details_entry_estimated);

        put = view.findViewById(R.id.details_entry_put);
        removed = view.findViewById(R.id.details_entry_removed);
        estimated_end_date = view.findViewById(R.id.details_entry_estimated_removed);
        total_breaks = view.findViewById(R.id.details_entry_break_number);
        total_time_breaks = view.findViewById(R.id.details_entry_total_break_time);

        textview_progress = view.findViewById(R.id.text_view_progress);
        textview_percentage_progression = view.findViewById(R.id.details_percentage_completion);
        textview_total_time = view.findViewById(R.id.text_view_total_progress);
        progressBar = view.findViewById(R.id.details_progress_bar);
        whenGetItOff = view.findViewById(R.id.details_entry_when_get_it_off);
        ImageButton pauseButton = view.findViewById(R.id.new_pause_button);

        entryDetailsViewModel = new ViewModelProvider(requireActivity()).get(EntryDetailsViewModel.class);
        entryDetailsViewModel.loadCurrentSession(entryId);

        textview_total_time.setText(String.format("/ %s", DateUtils.convertIntIntoReadableDate(entryDetailsViewModel.wearingTimePref)));

        // Progress bar related
        entryDetailsViewModel.progressPercentage.observe(getViewLifecycleOwner(), progressPercentage -> {
            Log.d(TAG, "Progress is supposed to be at " + progressPercentage);
            progressBar.setIndicatorColor(context.getResources().getColor(entryDetailsViewModel.progressColor));
            textview_progress.setTextColor(getResources().getColor(entryDetailsViewModel.progressColor));
            progressBar.setProgress(progressPercentage);
            textview_percentage_progression.setText(String.format("%d%%", progressPercentage));
        });

        // Session related
        entryDetailsViewModel.session.observe(getViewLifecycleOwner(), session -> {
            if (session.getStatus() == Session.SessionStatus.RUNNING) {
                removed.setText(entryDetailsViewModel.session.getValue().getEndDate());
                stopSessionButton.setVisibility(View.VISIBLE);
                end_session.setVisibility(View.GONE);
                estimated_end.setVisibility(View.VISIBLE);
            } else {
                removed.setText(DateUtils.convertDateIntoReadable(entryDetailsViewModel.session.getValue().getDateRemovedCalendar(), false) + "\n" + entryDetailsViewModel.session.getValue().getEndDate().split(" ")[1]);
                // If the session is finished, no need to show the ableToGetItOff textView.
                // This textview is only used to warn user when he will be able to get it off
                whenGetItOff.setVisibility(View.GONE);
                stopSessionButton.setVisibility(View.GONE);
                estimated_end.setVisibility(View.GONE);
                end_session.setVisibility(View.VISIBLE);
            }

            if (session.getSessionDuration() < 60)
                textview_progress.setText(entryDetailsViewModel.session.getValue().getSessionDuration() + getString(R.string.minute_with_M_uppercase));
            else
                textview_progress.setText(String.format("%dh%02dm", session.getSessionDuration() / 60, session.getSessionDuration() % 60));
            Log.d(TAG, "Break datas are size " + session.getBreakList().size());

            total_breaks.setText(String.valueOf(session.getBreakList().size()));
            total_time_breaks.setText(DateUtils.convertIntIntoReadableDate(entryDetailsViewModel.session.getValue().computeTotalTimePause()));
            updateBreakList(session.getBreakList());
            put.setText(DateUtils.convertDateIntoReadable(session.getDatePutCalendar(), false) + "\n" + session.getStartDate().split(" ")[1]);
        });

        entryDetailsViewModel.estimatedEnd.observe(getViewLifecycleOwner(), estimatedEnd-> {
            int texteResourceWhenGetItOff;

            String[] ableToGetItOffStringDate = DateUtils.getdateFormatted(estimatedEnd.getTime()).split(" ");
            estimated_end_date.setText(DateUtils.convertDateIntoReadable(ableToGetItOffStringDate[0], false) + "\n" + ableToGetItOffStringDate[1]);

            long timeBeforeRemove = DateUtils.getDateDiff(new Date(), estimatedEnd.getTime(), TimeUnit.MINUTES);

            if (timeBeforeRemove >= 0)
                texteResourceWhenGetItOff = R.string.in_about_entry_details;
            else {
                texteResourceWhenGetItOff = R.string.when_get_it_off_negative;
                timeBeforeRemove *= -1;
            }
            whenGetItOff.setText(String.format(getString(texteResourceWhenGetItOff), timeBeforeRemove / 60, timeBeforeRemove % 60));
        });

        // UI related
        stopSessionButton.setOnClickListener(view13 -> {
            entryDetailsViewModel.endSession();

            Utils.updateWidget(context);
        });

        pauseButton.setOnClickListener(view1 -> showPauseEditBreakFragment(null));
        pauseButton.setOnLongClickListener(view12 -> {
            if (entryDetailsViewModel.isThereARunningPause) {
                Log.d(TAG, "Error: Already a running pause");
                Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
            } else if (entryDetailsViewModel.session.getValue().getStatus() == Session.SessionStatus.RUNNING) {
                String date = DateUtils.getdateFormatted(new Date());
                long id = MainActivity.getDbManager().createNewPause(entryId, date, "NOT SET YET", 1);
                // Cancel alarm until breaks are set as finished.
                // Only then set a new alarm date
                Log.d(TAG, "Cancelling alarm for entry: " + entryId);
                SessionsAlarmsManager.cancelAlarm(context, entryId);
                SessionsAlarmsManager.setBreakAlarm(context ,DateUtils.getdateFormatted(new Date()), entryId);
                Utils.updateWidget(getContext());
            } else
                Toast.makeText(context, R.string.no_pause_session_is_not_running, Toast.LENGTH_SHORT).show();
            return true;
        });
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.CREATED);
    }

    /**
     * Update the listView by fetching all elements from the db
     * Yes i thought i could use a ListView or RecyclerView, but they do not fit inside of a scrollView
     * <a href="https://stackoverflow.com/a/3496042">https://stackoverflow.com/a/3496042</a>
     */
    private void updateBreakList(ArrayList<BreakSession> sessionBreaks) {
        break_layout.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i != sessionBreaks.size(); i++) {
            Log.d(TAG, "Inflating breaks");
            View breakLayout = inflater.inflate(R.layout.details_break_one_elem, break_layout, false);
            breakLayout.setTag(Integer.toString(i));

            String[] dateRemoved = sessionBreaks.get(i).getStartDate().split(" ");

            TextView wornForTextView = breakLayout.findViewById(R.id.worn_for_history);
            wornForTextView.setText(R.string.removed_during);

            TextView textView_date = breakLayout.findViewById(R.id.main_history_date);
            textView_date.setText(DateUtils.convertDateIntoReadable(dateRemoved[0], false));

            TextView textView_hour_from = breakLayout.findViewById(R.id.custom_view_date_weared_from);
            textView_hour_from.setText(dateRemoved[1]);

            TextView textView_hour_to = breakLayout.findViewById(R.id.custom_view_date_weared_to);

            TextView textView_worn_for = breakLayout.findViewById(R.id.custom_view_date_time_weared);

            if (!(sessionBreaks.get(i).getStatus() == Session.SessionStatus.RUNNING)) {
                String[] datePut = sessionBreaks.get(i).getEndDate().split(" ");
                textView_hour_to.setText(datePut[1]);
                if (!dateRemoved[0].equals(datePut[0]))
                    textView_date.setText(DateUtils.convertDateIntoReadable(dateRemoved[0], false) + " -> " + DateUtils.convertDateIntoReadable(datePut[0], false));
                textView_worn_for.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
                textView_worn_for.setText(DateUtils.convertIntIntoReadableDate((int) sessionBreaks.get(i).getSessionDuration()));
            } else {
                long timeworn = DateUtils.getDateDiff(sessionBreaks.get(i).getStartDate(), DateUtils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                textView_worn_for.setTextColor(getContext().getResources().getColor(R.color.yellow));
                textView_worn_for.setText(String.format("%dh%02dm", timeworn / 60, timeworn % 60));
            }

            breakLayout.setOnClickListener(clickInLinearLayout());
            breakLayout.setOnLongClickListener(v -> {
                new AlertDialog.Builder(context).setTitle(R.string.alertdialog_delete_entry)
                        .setMessage(R.string.alertdialog_delete_contact_body)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            int position = Integer.parseInt(v.getTag().toString());
                            BreakSession object = sessionBreaks.get(position);
                            Log.d(TAG, "pauseDatas size ?? " + sessionBreaks.size());
                            sessionBreaks.remove(object);
                            Log.d(TAG, "pauseDatas size " + sessionBreaks.size());
                            Log.d(TAG, "delete pause with id: " + object.getId() + " and index " + position);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
                return true;
            });
            break_layout.addView(breakLayout);
        }
    }

    /**
     * Show user pause Edit break fragment
     * Also compute if pause it in the session interval
     * @param dataModel If the pause already exist, give it datas to load
     */
    private void showPauseEditBreakFragment(BreakSession dataModel) {
        if (entryDetailsViewModel.isThereARunningPause && dataModel == null) {
            Log.d(TAG, "Error: Already a running pause");
            Toast.makeText(context, context.getString(R.string.already_running_pause), Toast.LENGTH_SHORT).show();
        } else {
            EditBreakFragment fragment = new EditBreakFragment();
            Bundle bundle = new Bundle();
            long breakId = (dataModel != null) ? dataModel.getId() : -1;
            bundle.putLong("breakId", breakId);
            bundle.putLong("sessionId", entryId);
            fragment.setArguments(bundle);
            getChildFragmentManager().setFragmentResultListener("EditBreakFragmentResult", this, (requestKey, bundle1) -> {
                boolean result = bundle1.getBoolean("shouldUpdateBreakList", true);
                Log.d(TAG, "got result from fragment: " + result);
                if (result)
                    entryDetailsViewModel.loadSession();
            });
            fragment.show(getChildFragmentManager(), null);
        }
    }

    private View.OnClickListener clickInLinearLayout() {
        return v -> {
            int position = Integer.parseInt(v.getTag().toString());
            Log.d(TAG, "Clicked item at position: " + position);

            showPauseEditBreakFragment(entryDetailsViewModel.session.getValue().getBreakList().get(position));
        };
    }

    @Override
    public void onDestroyView() {
        entryDetailsViewModel.stopTimer();
        super.onDestroyView();
    }
}
