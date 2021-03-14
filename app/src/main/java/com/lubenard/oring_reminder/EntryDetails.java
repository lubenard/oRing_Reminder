package com.lubenard.oring_reminder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EntryDetails extends AppCompatActivity {
    private int entryId = -1;
    private DbManager dbManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_details);

        dbManager = new DbManager(this);

        Intent intent = getIntent();
        entryId = intent.getIntExtra("entryId", -1);
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

            if (Integer.valueOf(contactDetails.get(2)) / 60 > 15)
                timeWeared.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            else
                timeWeared.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            put.setText(contactDetails.get(0));
            removed.setText(contactDetails.get(1));

            if (Integer.valueOf(contactDetails.get(2)) < 60) {
                timeWeared.setText(contactDetails.get(2) + " Minutes");
            } else if (Integer.valueOf(contactDetails.get(2)) <= 1440) {
                timeWeared.setText(String.format("%dh%02dm", Integer.valueOf(contactDetails.get(2)) / 60, Integer.valueOf(contactDetails.get(2)) % 60));
            }


            isRunning.setText(contactDetails.get(3));
        }
        else {
            // trigger error, show toast and exit
            finish();
        }
    }
}
