package com.lubenard.oring_reminder.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.DbManager;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.CustomListAdapter;
import com.lubenard.oring_reminder.custom_components.CustomListPausesAdapter;
import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class EntryDetailsFragment extends Fragment {

    public static final String TAG = "EntryDetailsFragment";

    private long entryId = -1;
    private DbManager dbManager;
    private int weared_time;
    private View view;
    private Context context;
    private FragmentManager fragmentManager;
    private ListView listView;
    private CustomListPausesAdapter adapter;
    private ArrayList<RingModel> dataModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.entry_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
        fragmentManager = getActivity().getSupportFragmentManager();
        this.view = view;

        dbManager = new DbManager(context);
        dataModels = new ArrayList<>();

        Bundle bundle = this.getArguments();
        entryId = bundle.getLong("entryId", -1);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));

        Toolbar toolbar = view.findViewById(R.id.entry_details_toolbar);

        listView = view.findViewById(R.id.listview_pauses);

        // This can block the listview from scrolling, but i cannot integrate listview inside
        // scrollview without it acting weird (only showing one row)
        /*listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true; // Indicates that this has been handled by you and will not be forwarded further.
                }
                return false;
            }
        });*/

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.popBackStackImmediate();
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_edit_entry:
                    EditEntryFragment fragment = new EditEntryFragment(getContext());
                    Bundle bundle2 = new Bundle();
                    bundle2.putLong("entryId", entryId);
                    fragment.setArguments(bundle2);
                    fragmentManager.beginTransaction()
                            .replace(android.R.id.content, fragment, null)
                            .addToBackStack(null).commit();
                    return true;
                case R.id.action_delete_entry:
                    // Warn user then delete entry in the db
                    new AlertDialog.Builder(context).setTitle(R.string.alertdialog_delete_entry)
                            .setMessage(R.string.alertdialog_delete_contact_body)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dbManager.deleteEntry(entryId);
                                    fragmentManager.popBackStackImmediate();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                    return true;
                default:
                    return false;
            }
        });

        ImageButton testButton = view.findViewById(R.id.new_pause_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                ViewGroup viewGroup = view.findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.custom_pause_dialog, viewGroup, false);
                builder.setView(dialogView);
                AlertDialog alertDialog = builder.create();

                EditText pause_beginning = dialogView.findViewById(R.id.edittext_beginning_pause);
                EditText pause_ending = dialogView.findViewById(R.id.edittext_finish_pause);

                Button fill_beginning = dialogView.findViewById(R.id.prefill_beginning_pause);
                fill_beginning.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pause_beginning.setText(Utils.getdateFormatted(new Date()));
                    }
                });

                Button fill_end = dialogView.findViewById(R.id.prefill_finish_pause);
                fill_end.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pause_ending.setText(Utils.getdateFormatted(new Date()));
                    }
                });

                Button save_entry = dialogView.findViewById(R.id.validate_pause);
                save_entry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int isRunning = 0;
                        if (pause_ending.getText().toString().isEmpty()) {
                            pause_ending.setText("NOT SET YET");
                            isRunning = 1;
                        }
                        Log.d(TAG, "pauseTablePut = " + pause_ending.getText());
                        dbManager.createNewPause(entryId, pause_beginning.getText().toString(), pause_ending.getText().toString(), isRunning);
                        alertDialog.dismiss();
                        updatePauseList();
                    }
                });
                alertDialog.show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                new AlertDialog.Builder(context).setTitle(R.string.alertdialog_delete_entry)
                        .setMessage(R.string.alertdialog_delete_contact_body)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                RingModel object = (RingModel) arg0.getItemAtPosition(pos);
                                Log.d(TAG, "delete pause with id: " + object.getId());
                                dbManager.deletePauseEntry(entryId);
                                updatePauseList();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
                return true;
            }
        });

    }

    /**
     * Update the listView by fetching all elements from the db
     */
    private void updatePauseList() {
       dataModels.clear();
       ArrayList<RingModel> pausesDatas = dbManager.getAllPausesForId(entryId, true);

       dataModels.addAll(pausesDatas);
       adapter = new CustomListPausesAdapter(dataModels, getContext());
       listView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (entryId > 0) {
            // Load datas from the db and put them at the right place
            ArrayList<String> contactDetails = dbManager.getEntryDetails(entryId);
            TextView put = view.findViewById(R.id.details_entry_put);
            TextView removed = view.findViewById(R.id.details_entry_removed);
            TextView timeWeared = view.findViewById(R.id.details_entry_time_weared);
            TextView isRunning = view.findViewById(R.id.details_entry_isRunning);
            TextView ableToGetItOff = view.findViewById(R.id.details_entry_able_to_get_it_off);

            // Choose color if the timeWeared is enough or not
            // Depending of the timeWeared set in the settings
            if (!contactDetails.get(2).equals("NOT SET YET") && Integer.parseInt(contactDetails.get(2)) / 60 >= weared_time)
                timeWeared.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            else if (!contactDetails.get(2).equals("NOT SET YET") && Integer.parseInt(contactDetails.get(2)) / 60 < weared_time)
                timeWeared.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            else
                timeWeared.setTextColor(getResources().getColor(R.color.yellow));

            put.setText(contactDetails.get(0));
            removed.setText(contactDetails.get(1));

            // Check if the session is finished and display the corresponding text
            // Either 'Not set yet', saying the session is not over
            // Or the endSession date
            if (contactDetails.get(2).equals("NOT SET YET")) {
                long timeBeforeRemove = Utils.getDateDiff(contactDetails.get(0), Utils.getdateFormatted(new Date()), TimeUnit.MINUTES);
                timeWeared.setText(String.format("%dh%02dm", timeBeforeRemove / 60, timeBeforeRemove % 60));
            } else {
                int time_spent_wearing = Integer.parseInt(contactDetails.get(2));
                if (time_spent_wearing < 60)
                    timeWeared.setText(contactDetails.get(2) + getString(R.string.minute_with_M_uppercase));
                else
                    timeWeared.setText(String.format("%dh%02dm", time_spent_wearing / 60, time_spent_wearing % 60));
            }

            // Display the datas relative to the session
            if (Integer.parseInt(contactDetails.get(3)) == 1) {
                isRunning.setTextColor(getResources().getColor(R.color.yellow));
                isRunning.setText(R.string.session_is_running);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Utils.getdateParsed(contactDetails.get(0)));
                calendar.add(Calendar.HOUR_OF_DAY, weared_time);

                long timeBeforeRemove = Utils.getDateDiff(Utils.getdateFormatted(new Date()), Utils.getdateFormatted(calendar.getTime()), TimeUnit.MINUTES);
                Log.d(TAG, "timeBeforeRemove = " + timeBeforeRemove);
                ableToGetItOff.setText(getString(R.string._message_able_to_get_it_off) + Utils.getdateFormatted(calendar.getTime())
                        + "\n" + String.format("(in about %dh%02dm)", timeBeforeRemove / 60, timeBeforeRemove % 60));
            } else {
                // If the session is finished, no need to show the ableToGetItOff textView.
                // This textview is only used to warn user when he will be able to get it off
                isRunning.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                isRunning.setText(R.string.session_finished);
                ableToGetItOff.setVisibility(View.INVISIBLE);
            }
            updatePauseList();
        } else {
            // Trigger an error if the entryId is wrong, then go back to main list
            Toast.makeText(context, context.getString(R.string.error_bad_id_entry_details) + entryId, Toast.LENGTH_SHORT);
            Log.e(TAG, "Error: Wrong Id: " + entryId);
            fragmentManager.popBackStackImmediate();
        }
    }
}
