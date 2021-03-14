package com.lubenard.oring_reminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EntryDetails extends AppCompatActivity {
    private int entryId = -1;
    private DbManager dbManager;
    private int weared_time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_details);

        dbManager = new DbManager(this);

        Intent intent = getIntent();
        entryId = intent.getIntExtra("entryId", -1);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        weared_time = Integer.parseInt(sharedPreferences.getString("myring_wearing_time", "15"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_entry_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_edit_entry:
                Intent intent = new Intent(this, EditEntry.class);
                intent.putExtra("entryId", entryId);
                startActivity(intent);
                break;
            case R.id.action_delete_entry:
                new AlertDialog.Builder(this).setTitle(R.string.alertdialog_delete_entry)
                        .setMessage(R.string.alertdialog_delete_contact_body)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dbManager.deleteEntry(entryId);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (entryId > 0) {
            ArrayList<String> contactDetails = dbManager.getEntryDetails(entryId);
            TextView put = findViewById(R.id.details_entry_put);
            TextView removed = findViewById(R.id.details_entry_removed);
            TextView timeWeared = findViewById(R.id.details_entry_time_weared);
            TextView isRunning = findViewById(R.id.details_entry_isRunning);
            TextView ableToGetItOff = findViewById(R.id.details_entry_able_to_get_it_off);

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
            Toast.makeText(this, R.string.error_bad_id_entry_details, Toast.LENGTH_SHORT);
            finish();
        }
    }
}