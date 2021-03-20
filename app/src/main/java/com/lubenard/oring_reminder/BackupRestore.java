package com.lubenard.oring_reminder;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BackupRestore extends Activity{

    public static final String TAG = "BackupAndRestore";
    private AlertDialog dialog;
    private boolean shouldBackupRestoreDatas;
    private boolean shouldBackupRestoreSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldBackupRestoreDatas = getIntent().getBooleanExtra("shouldBackupRestoreDatas", false);
        shouldBackupRestoreSettings = getIntent().getBooleanExtra("shouldBackupRestoreSettings", false);
        startBackupIntoXML();
    }

    private void launchIntent(Intent dataToFileChooser) {
        try {
            startActivityForResult(dataToFileChooser, 1);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Failed to open a Intent. Save will be done at default location.");
            Toast.makeText(this, R.string.toast_error_custom_path_backup_restore_fail, Toast.LENGTH_LONG).show();
        }
    }

    private boolean startBackupIntoXML() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent dataToFileChooser = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            dataToFileChooser.setType("text/xml");
            dataToFileChooser.putExtra(Intent.EXTRA_TITLE, "myDatas.xml");
            launchIntent(dataToFileChooser);
        } else {
            Log.w(TAG, "Your android version is pretty old. Save will be done at default location.");
            Toast.makeText(this, R.string.toast_error_custom_path_backup_restore_android_too_old, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private void createAlertDialog() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Saving datas");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.loading_layout, null);
        builder.setView(customLayout);

        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void saveSettingsIntoXml(XmlWriter xmlWriter) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            xmlWriter.writeEntity("settings");

            xmlWriter.writeEntity("ui_language");
            xmlWriter.writeText(preferences.getString("ui_language", "system"));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("ui_theme");
            xmlWriter.writeText(preferences.getString("ui_theme", "dark"));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("myring_wearing_time");
            xmlWriter.writeText(preferences.getString("myring_wearing_time", "15"));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("myring_send_notif_when_session_over");
            xmlWriter.writeText(String.valueOf(preferences.getBoolean("myring_send_notif_when_session_over", true)));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("myring_prevent_me_when_started_session");
            xmlWriter.writeText(String.valueOf(preferences.getBoolean("myring_prevent_me_when_started_session", true)));
            xmlWriter.endEntity();

            xmlWriter.endEntity();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDatasIntoXml(XmlWriter xmlWriter) {
        DbManager dbManager = new DbManager(this);
        // Datas containing all saved datas
        try {
            xmlWriter.writeEntity("datas");
            // Contain number of unlocks
            ArrayList<EntryClass> datas = dbManager.getAllDatasForAllEntrys();

            for (int i = 0; i < datas.size(); i++) {
                xmlWriter.writeEntity("entry");
                xmlWriter.writeAttribute("id", String.valueOf(datas.get(i).getId()));
                xmlWriter.writeAttribute("isRunning", String.valueOf(datas.get(i).getIsRunning()));
                xmlWriter.writeAttribute("dateTimePut", datas.get(i).getDateTimePut());
                xmlWriter.writeAttribute("dateTimeRemoved", datas.get(i).getDateTimeRemoved());
                xmlWriter.writeAttribute("timeWeared", String.valueOf(datas.get(i).getTimeWeared()));
                xmlWriter.endEntity();
            }
            xmlWriter.endEntity();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getDataString() != null) {
            createAlertDialog();
            Log.d(TAG, "ActivityResult backup at path: " + data.getDataString());
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(Uri.parse(data.getDataString()));
                XmlWriter xmlWriter = new XmlWriter(outputStream);
                if (shouldBackupRestoreDatas)
                    saveDatasIntoXml(xmlWriter);
                if (shouldBackupRestoreSettings)
                    saveSettingsIntoXml(xmlWriter);
                xmlWriter.close();
            } catch (IOException e) {
                Log.d(TAG, "something failed during the save of the datas");
                e.printStackTrace();
            }
            Toast.makeText(this, "Success to save the datas", Toast.LENGTH_LONG).show();
            dialog.dismiss();
            finish();
        }
    }

}
