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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BackupRestore extends Activity{

    public static final String TAG = "BackupAndRestore";
    private AlertDialog dialog;
    private boolean shouldBackupRestoreDatas;
    private int typeOfDatas;
    private boolean shouldBackupRestoreSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldBackupRestoreDatas = getIntent().getBooleanExtra("shouldBackupRestoreDatas", false);
        shouldBackupRestoreSettings = getIntent().getBooleanExtra("shouldBackupRestoreSettings", false);
        typeOfDatas = getIntent().getIntExtra("mode", -1);
        if (typeOfDatas == 1)
            startBackupIntoXML();
        if (typeOfDatas == 2)
            startRestoreFromXML();

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

    private boolean startRestoreFromXML() {
        Log.d(TAG, "startRestoreFromXML");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent dataToFileChooser = new Intent(Intent.ACTION_GET_CONTENT);
            dataToFileChooser.setType("text/xml");
            launchIntent(dataToFileChooser);
        } else {
            Log.w(TAG, "Android version too old. Save will be at default location");
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

    private void restoreDatasFromXml(InputStream inputStream) {
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            DbManager dbManager = new DbManager(getApplicationContext());
            int isRunning = 0;

            myParser.setInput(inputStream, null);

            int eventType = myParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && myParser.getName().equals("entry")) {
                    Log.d("Backup restore", "This is a item, lol");
                    if (myParser.getAttributeValue(null, "dateTimeRemoved").equals("NOT SET YET"))
                        isRunning = 1;
                    else
                        isRunning = 0;
                    dbManager.createNewDatesRing(myParser.getAttributeValue(null, "dateTimePut"), myParser.getAttributeValue(null, "dateTimeRemoved"), isRunning);
                }
                eventType = myParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: OPTIMIZE THIS FUNCTION
    private void restoreSettingsFromXml(InputStream inputStream) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();

            myParser.setInput(inputStream, null);

            int eventType = myParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (myParser.getName().equals("ui_language")) {
                        myParser.next();
                        Log.d(TAG, "ui_language setting = " + myParser.getText());
                        preferences.edit().putString("ui_language", myParser.getText()).apply();
                    } else if (myParser.getName().equals("ui_theme")) {
                        myParser.next();
                        Log.d(TAG, "ui_theme setting = " + myParser.getText());
                        preferences.edit().putString("ui_theme", myParser.getText()).apply();
                    } else if (myParser.getName().equals("myring_wearing_time")) {
                        myParser.next();
                        Log.d(TAG, "myring_wearing_time setting = " + myParser.getText());
                        preferences.edit().putString("myring_wearing_time", myParser.getText()).apply();
                    } else if (myParser.getName().equals("myring_send_notif_when_session_over")) {
                        myParser.next();
                        Log.d(TAG, "myring_send_notif_when_session_over setting = " + myParser.getText());
                        preferences.edit().putBoolean("myring_send_notif_when_session_over", Boolean.parseBoolean(myParser.getText())).apply();
                    } else if (myParser.getName().equals("myring_prevent_me_when_started_session")) {
                        myParser.next();
                        Log.d(TAG, "myring_prevent_me_when_started_session setting = " + myParser.getText());
                        preferences.edit().putBoolean("myring_prevent_me_when_started_session", Boolean.parseBoolean(myParser.getText())).apply();
                    }
                }
                eventType = myParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        // Recreate activity once all settings have been restored
        SettingsFragment.restartActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getDataString() != null) {
            createAlertDialog();
            if (typeOfDatas == 1) {
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
                Toast.makeText(this, getString(R.string.toast_success_save_datas), Toast.LENGTH_LONG).show();
            } else if (typeOfDatas == 2) {
                Log.d(TAG, "ActivityResult restore from path: " + data.getDataString());
                try {
                    InputStream inputStream = getContentResolver().openInputStream(Uri.parse((data.getDataString())));
                    if (shouldBackupRestoreDatas)
                        restoreDatasFromXml(inputStream);
                    if (shouldBackupRestoreSettings)
                        restoreSettingsFromXml(inputStream);
                } catch (IOException e) {
                    Log.d(TAG, "something failed during the restore of the datas");
                    e.printStackTrace();
                }
                Toast.makeText(this, getString(R.string.toast_success_restore_datas), Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
            finish();
        }
    }

}
