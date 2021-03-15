package com.lubenard.oring_reminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EntryDetailsFragment extends Fragment {

    private int entryId = -1;
    private DbManager dbManager;
    private int weared_time;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.entry_details_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbManager = new DbManager(getContext());

        Bundle bundle = this.getArguments();
        entryId = bundle.getInt("entryId", -1);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));
        this.view = view;

        Toolbar toolbar = view.findViewById(R.id.entry_details_toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_edit_entry:
                    EditEntryFragment fragment = new EditEntryFragment();
                    Bundle bundle2 = new Bundle();
                    bundle2.putInt("entryId", entryId);
                    fragment.setArguments(bundle2);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, fragment, null)
                            .addToBackStack(null).commit();
                    return true;
                case R.id.action_delete_entry:
                    new AlertDialog.Builder(getContext()).setTitle(R.string.alertdialog_delete_entry)
                            .setMessage(R.string.alertdialog_delete_contact_body)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dbManager.deleteEntry(entryId);
                                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                    return true;
                default:
                    return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (entryId > 0) {
            ArrayList<String> contactDetails = dbManager.getEntryDetails(entryId);
            TextView put = view.findViewById(R.id.details_entry_put);
            TextView removed = view.findViewById(R.id.details_entry_removed);
            TextView timeWeared = view.findViewById(R.id.details_entry_time_weared);
            TextView isRunning = view.findViewById(R.id.details_entry_isRunning);
            TextView ableToGetItOff = view.findViewById(R.id.details_entry_able_to_get_it_off);

            if (!contactDetails.get(2).equals("NOT SET YET") && Integer.parseInt(contactDetails.get(2)) / 60 >= weared_time)
                timeWeared.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            else
                timeWeared.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            put.setText(contactDetails.get(0));
            removed.setText(contactDetails.get(1));

            if (contactDetails.get(2).equals("NOT SET YET"))
                timeWeared.setText(R.string.not_set_yet);
            else {
                int time_spent_wearing = Integer.parseInt(contactDetails.get(2));
                if (time_spent_wearing < 60) {
                    timeWeared.setText(contactDetails.get(2) + getString(R.string.minute_with_M_uppercase));
                } else {
                    timeWeared.setText(String.format("%dh%02dm", time_spent_wearing / 60, time_spent_wearing % 60));
                }
            }

            if (Integer.parseInt(contactDetails.get(3)) == 1) {
                isRunning.setTextColor(getResources().getColor(R.color.yellow));
                isRunning.setText(R.string.session_is_running);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(dateFormat.parse(contactDetails.get(0)));
                    calendar.add(Calendar.HOUR_OF_DAY, weared_time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                ableToGetItOff.setText(getString(R.string._message_able_to_get_it_off) + dateFormat.format(calendar.getTime()));
            } else {
                isRunning.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                isRunning.setText(R.string.session_finished);
                ableToGetItOff.setVisibility(View.INVISIBLE);
            }
        }
        else {
            Toast.makeText(getContext(), R.string.error_bad_id_entry_details, Toast.LENGTH_SHORT);
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        }
    }
}
