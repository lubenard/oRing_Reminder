package com.lubenard.oring_reminder.managers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.lubenard.oring_reminder.BuildConfig;
import com.lubenard.oring_reminder.MainActivity;
import com.lubenard.oring_reminder.R;
import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.utils.CsvWriter;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;
import com.lubenard.oring_reminder.utils.XmlWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Used for Backup & Restore of User Datas
 */
public class BackupRestoreManager extends Activity {

    public static final String TAG = "BackupAndRestore";
    private AlertDialog dialog;
    private boolean shouldBackupRestoreDatas;
    private int typeOfDatas;
    private boolean shouldBackupRestoreSettings;
    private String exportPath = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldBackupRestoreDatas = getIntent().getBooleanExtra("shouldBackupRestoreDatas", true);
        shouldBackupRestoreSettings = getIntent().getBooleanExtra("shouldBackupRestoreSettings", true);
        Log.d(TAG, "shouldBackupRestoreDatas = " + shouldBackupRestoreDatas + " shouldBackupRestoreSettings = " + shouldBackupRestoreSettings);
        typeOfDatas = getIntent().getIntExtra("mode", -1);
        if (typeOfDatas == 1)
            startBackupIntoXML();
        if (typeOfDatas == 2)
            startRestoreFromXML();
        if (typeOfDatas == 3)
            startBackupIntoCSV();
    }

    /**
     * Launch the backup for export in CSV
     */
    private void startBackupIntoCSV() {
        Intent dataToFileChooser = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        dataToFileChooser.setType("text/csv");
        dataToFileChooser.putExtra(Intent.EXTRA_TITLE, "myDatasCSV.csv");
        launchIntent(dataToFileChooser);
    }

    /**
     * Start the file chooser intent
     * @param dataToFileChooser launch given intent. Might be to create new file / select existing file
     */
    private void launchIntent(Intent dataToFileChooser) {
        try {
            startActivityForResult(dataToFileChooser, 1);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Failed to open a Intent. Save will be done at default location.");
            Toast.makeText(this, R.string.toast_error_custom_path_backup_restore_fail, Toast.LENGTH_LONG).show();
            getDefaultFolder("xml");
            createDefaultFileIfNeeded();
            launchBackupRestore(exportPath);
        }
    }

    /**
     * Start export in XML
     */
    private void startBackupIntoXML() {
        Intent dataToFileChooser = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        dataToFileChooser.setType("text/xml");
        dataToFileChooser.putExtra(Intent.EXTRA_TITLE, "myDatas.xml");
        launchIntent(dataToFileChooser);
    }

    /**
     * Create the default export file
     * Probably in
     * "Intern Memory"/android/datas/com.lubenard.oring_reminder/oRingReminder-Backup/
     */
    private void createDefaultFileIfNeeded() {
        if (typeOfDatas == 1) {
            File f1 = new File(Environment.getExternalStorageDirectory() + "/oRingReminder-Backup", "backup.xml");
            if (!f1.exists()) {
                try {
                    f1.createNewFile();
                } catch (IOException e) {
                    Log.e(TAG, "Impossible to create the backup.xml file: ", e);
                }
            }
        }
    }

    /**
     * Create the default folder 'oRingReminder-Backup' in
     * "Intern Memory"/android/datas/com.lubenard.oring_reminder
     * @param extension file extension ("xml" or "csv")
     */
    private void getDefaultFolder(String extension) {
        String folder_main = "oRingReminder-Backup";

        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }
        exportPath = f.getPath() + "/backup." + extension;
        Log.d(TAG, "Absolute path of backup file is " + exportPath);
    }

    /**
     * Start Import from XML
     */
    private void startRestoreFromXML() {
        Log.d(TAG, "startRestoreFromXML");
        Intent dataToFileChooser = new Intent(Intent.ACTION_GET_CONTENT);
        dataToFileChooser.setType("text/xml");
        launchIntent(dataToFileChooser);
    }

    /**
     * Create the "Please wait..." alertdialog"
     */
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

    /**
     * Export settings in XML
     * @param xmlWriter to write to
     */
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

            xmlWriter.writeEntity("ui_action_on_plus_button");
            xmlWriter.writeText(preferences.getString("ui_action_on_plus_button", "default"));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("myring_wearing_time");
            xmlWriter.writeText(preferences.getString("myring_wearing_time", "15"));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("myring_send_notif_when_session_over");
            xmlWriter.writeText(String.valueOf(preferences.getBoolean("myring_send_notif_when_session_over", true)));
            xmlWriter.endEntity();

            /*
            xmlWriter.writeEntity("myring_prevent_me_when_started_session");
            xmlWriter.writeText(String.valueOf(preferences.getBoolean("myring_prevent_me_when_started_session", true)));
            xmlWriter.endEntity();
            */

            xmlWriter.writeEntity("myring_prevent_me_when_no_session_started_for_today");
            xmlWriter.writeText(String.valueOf(preferences.getBoolean("myring_prevent_me_when_no_session_started_for_today", false)));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("myring_prevent_me_when_no_session_started_date");
            xmlWriter.writeText(preferences.getString("myring_prevent_me_when_no_session_started_date", "12:00"));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("myring_prevent_me_when_pause_too_long");
            xmlWriter.writeText(String.valueOf(preferences.getBoolean("myring_prevent_me_when_pause_too_long", false)));
            xmlWriter.endEntity();

            xmlWriter.writeEntity("myring_prevent_me_when_pause_too_long_date");
            xmlWriter.writeText(String.valueOf(preferences.getInt("myring_prevent_me_when_pause_too_long_date", 0)));
            xmlWriter.endEntity();

            xmlWriter.endEntity();
        } catch (IOException e) {
            Log.e(TAG, "Impossible to save the settings to XML: ", e);
        }
    }

    /**
     * Export datas in XML
     * @param xmlWriter to write to
     */
    private void saveDatasIntoXml(XmlWriter xmlWriter) {
        DbManager dbManager = MainActivity.getDbManager();
        // Datas containing all saved datas
        try {
            xmlWriter.writeEntity("datas");
            // Contain all entry's
            ArrayList<RingSession> datas = dbManager.getAllDatasForAllEntrys();
            for (int i = 0; i < datas.size(); i++) {
                xmlWriter.writeEntity("session");
                xmlWriter.writeAttribute("dateTimePut", datas.get(i).getStartDate());
                xmlWriter.writeAttribute("dateTimeRemoved", datas.get(i).getEndDate());
                xmlWriter.writeAttribute("isRunning", String.valueOf(datas.get(i).getStatus() == Session.SessionStatus.RUNNING ? 1 : 0));
                xmlWriter.writeAttribute("timeWeared", String.valueOf(datas.get(i).getSessionDuration()));

                ArrayList<BreakSession> pauses = dbManager.getAllBreaksForId(datas.get(i).getId(), true);
                if (!pauses.isEmpty()) {
                    Log.d(TAG, "Break exist for session " + datas.get(i).getId() + ". There is " + pauses.size() + " breaks");
                    for (int j = 0; j != pauses.size(); j++) {
                        Log.d(TAG, "Looping through the break of session " + datas.get(i).getId());
                        xmlWriter.writeEntity("pause");
                        xmlWriter.writeAttribute("dateTimeRemoved", pauses.get(j).getStartDate());
                        xmlWriter.writeAttribute("dateTimePut", pauses.get(j).getEndDate());
                        xmlWriter.writeAttribute("isRunning", String.valueOf(pauses.get(j).getStatus() == Session.SessionStatus.RUNNING ? 1 : 0));
                        xmlWriter.writeAttribute("timeRemoved", String.valueOf(pauses.get(j).getSessionDuration()));
                        xmlWriter.endEntity();
                    }
                }
                xmlWriter.endEntity();
            }
            xmlWriter.endEntity();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save datas into XML: ", e);
        }
    }

    /**
     * Restore datas from XML
     * @param inputStream to read from
     */
    private void restoreDatasFromXml(InputStream inputStream) {
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            DbManager dbManager = MainActivity.getDbManager();
            int isRunning;
            long lastEntryInsertedId = -1;

            myParser.setInput(inputStream, null);

            // Skip the first element
            int eventType = myParser.next();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && myParser.getName().equals("session")) {
                    // Check if we have the minimum infos about the session to recreate
                    if (myParser.getAttributeValue(null, "dateTimePut") != null && myParser.getAttributeValue(null, "dateTimeRemoved") != null
                    && DateUtils.Companion.isDateSane(myParser.getAttributeValue(null, "dateTimePut"))) {
                        isRunning = myParser.getAttributeValue(null, "dateTimeRemoved").equals("NOT SET YET") ? 1 : 0;
                        lastEntryInsertedId = dbManager.createNewEntry(myParser.getAttributeValue(null, "dateTimePut"), myParser.getAttributeValue(null, "dateTimeRemoved"), isRunning);
                        if (isRunning == 1) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(DateUtils.Companion.getdateParsed(myParser.getAttributeValue(null, "dateTimePut")));
                            calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(preferences.getString("myring_wearing_time", "15")));
                            SessionsAlarmsManager.setAlarm(this, calendar, lastEntryInsertedId, true);
                        }
                    } else {
                        Toast.makeText(this, R.string.bad_import_date_xml, Toast.LENGTH_SHORT).show();
                        lastEntryInsertedId = -1;
                    }
                }
                if (eventType == XmlPullParser.START_TAG && myParser.getName().equals("pause")) {
                    Log.d(TAG, "Restoring pause for entryId: " + lastEntryInsertedId);
                    if (myParser.getAttributeValue(null, "dateTimeRemoved") != null && myParser.getAttributeValue(null, "dateTimePut") != null && lastEntryInsertedId != -1) {
                        isRunning = myParser.getAttributeValue(null, "dateTimePut").equals("NOT SET YET") ? 1 : 0;
                        if (lastEntryInsertedId != 0) {
                            dbManager.createNewPause(lastEntryInsertedId, myParser.getAttributeValue(null, "dateTimeRemoved"), myParser.getAttributeValue(null, "dateTimePut"), isRunning);
                            if (isRunning == 1)
                                SessionsAlarmsManager.cancelAlarm(this, lastEntryInsertedId);
                        }
                    } else
                        Toast.makeText(this, R.string.bad_import_date_pause_xml ,Toast.LENGTH_SHORT).show();
                }
                eventType = myParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error while restoring datas form XML: ", e);
        }
    }

    /**
     * Restore settings from XML
     * @param inputStream to read from
     */
    private void restoreSettingsFromXml(InputStream inputStream) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();

            myParser.setInput(inputStream, null);

            int eventType = myParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    switch (myParser.getName()) {
                        case "ui_language":
                            myParser.next();
                            Log.d(TAG, "ui_language setting = " + myParser.getText());
                            preferences.edit().putString("ui_language", myParser.getText()).apply();
                            break;
                        case  "ui_theme":
                            myParser.next();
                            Log.d(TAG, "ui_theme setting = " + myParser.getText());
                            preferences.edit().putString("ui_theme", myParser.getText()).apply();
                            break;
                        case  "ui_action_on_plus_button":
                            myParser.next();
                            Log.d(TAG, "ui_action_on_plus_button = " + myParser.getText());
                            preferences.edit().putString("ui_action_on_plus_button", myParser.getText()).apply();
                            break;
                        case "myring_wearing_time":
                            myParser.next();
                            Log.d(TAG, "myring_wearing_time setting = " + myParser.getText());
                            preferences.edit().putString("myring_wearing_time", myParser.getText()).apply();
                            break;
                        case "myring_send_notif_when_session_over":
                            myParser.next();
                            Log.d(TAG, "myring_send_notif_when_session_over setting = " + myParser.getText());
                            preferences.edit().putBoolean("myring_send_notif_when_session_over", Boolean.parseBoolean(myParser.getText())).apply();
                            break;
                        case "myring_prevent_me_when_started_session":
                            myParser.next();
                            Log.d(TAG, "myring_prevent_me_when_started_session setting = " + myParser.getText());
                            Log.d(TAG, "Option has been deprecated, ignoring");
                            break;
                        case "myring_prevent_me_when_no_session_started_for_today":
                            myParser.next();
                            Log.d(TAG, "myring_prevent_me_when_no_session_started_for_today setting = " + myParser.getText());
                            preferences.edit().putBoolean("myring_prevent_me_when_no_session_started_for_today", Boolean.parseBoolean(myParser.getText())).apply();
                            break;
                        case "myring_prevent_me_when_no_session_started_date":
                            myParser.next();
                            Log.d(TAG, "myring_prevent_me_when_no_session_started_date setting = " + myParser.getText());
                            preferences.edit().putString("myring_prevent_me_when_no_session_started_date", myParser.getText()).apply();
                            break;
                        case "myring_prevent_me_when_pause_too_long":
                            myParser.next();
                            Log.d(TAG, "myring_prevent_me_when_pause_too_long setting = " + myParser.getText());
                            preferences.edit().putBoolean("myring_prevent_me_when_pause_too_long", Boolean.parseBoolean(myParser.getText())).apply();
                            break;
                        case "myring_prevent_me_when_pause_too_long_date":
                            myParser.next();
                            Log.d(TAG, "myring_prevent_me_when_pause_too_long_date setting = " + myParser.getText());
                            preferences.edit().putInt("myring_prevent_me_when_pause_too_long_date", Integer.parseInt(myParser.getText())).apply();
                            break;
                    }
                }
                eventType = myParser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error while restoring settings from XML", e);
        }
        // TODO: Recreate activity once all settings have been restored
    }

    private void checkAppVersion(InputStream inputStream) {
        try {
            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();

            myParser.setInput(inputStream, null);

            // Skip the first element
            int eventType = myParser.next();
            if (eventType == XmlPullParser.START_TAG && myParser.getName().equals("app_version")) {
                myParser.next();
                if (myParser.getText().equals(BuildConfig.VERSION_NAME))
                    Log.d(TAG, "Same app version !");
                else
                    Log.d(TAG, "Not same app version ! " + myParser.getText() + "/" + BuildConfig.VERSION_NAME);
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Error while checking app version: ", e);
        }
    }

    /**
     * Actually launch the backup system depending on what to do.
     * This function is executed when we now everything is ready for export
     * @param filePath file path to export to
     */
    private void launchBackupRestore(String filePath) {
        createAlertDialog();
        Uri uri;
        if (exportPath != null)
            uri = Uri.fromFile(new File(filePath));
        else
            uri = Uri.parse(filePath);
        if (typeOfDatas == 1) {
            Log.d(TAG, "Backup at path: " + filePath);
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                XmlWriter xmlWriter = new XmlWriter(outputStream);
                // Write app version in xml export, for warning user if
                // saves are imported from earlier version of the app
                xmlWriter.writeEntity("app_version");
                xmlWriter.writeText(BuildConfig.VERSION_NAME);
                xmlWriter.endEntity();
                if (shouldBackupRestoreDatas)
                    saveDatasIntoXml(xmlWriter);
                if (shouldBackupRestoreSettings)
                    saveSettingsIntoXml(xmlWriter);
                xmlWriter.close();
            } catch (IOException e) {
                Log.e(TAG, "Error: something failed during the save of the datas: ", e);
            }
            Toast.makeText(this, getString(R.string.toast_success_save_datas), Toast.LENGTH_LONG).show();
        } else if (typeOfDatas == 2) {
            Log.d(TAG, "ActivityResult restore from path: " + filePath);
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                //checkAppVersion(inputStream);
                if (shouldBackupRestoreDatas)
                    restoreDatasFromXml(inputStream);
                // TODO: HOTFIX ! I should not need to reopen a stream (at least i think so)
                // Find a way to fix this.
                // Before this fix, the settings could not be restored
                inputStream = getContentResolver().openInputStream(uri);
                if (shouldBackupRestoreSettings)
                    restoreSettingsFromXml(inputStream);
            } catch (IOException e) {
                Log.e(TAG, "Error: something failed during the restore of the datas", e);
            }
            Toast.makeText(this, getString(R.string.toast_success_restore_datas), Toast.LENGTH_LONG).show();
        } else if (typeOfDatas == 3) {
            Log.d(TAG, "Backup at path: " + filePath);
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                CsvWriter csvWriter = new CsvWriter(outputStream);
                saveDatasIntoCsv(csvWriter);
                csvWriter.close();
            } catch (IOException e) {
                Log.e(TAG, "Error: something failed during the save of the datas: ", e);
            }
            Toast.makeText(this, getString(R.string.toast_success_save_datas), Toast.LENGTH_LONG).show();
        }
        dialog.dismiss();
        finish();
    }

    /**
     * Export datas into CSV
     * @param csvWriter to write to
     */
    private void saveDatasIntoCsv(CsvWriter csvWriter) {
        DbManager dbManager = MainActivity.getDbManager();
        // Datas containing all saved datas
        try {

            csvWriter.writeColumnsName(new String[]{"All times are computed in minutes."});
            csvWriter.writeColumnsName(new String[]{"Date put", "Hour put", "Date removed", "Hour removed", "Time worn (without breaks)",
                                                    "Total break time", "Time worn (with breaks)"});

            ArrayList<String> formattedDatas = new ArrayList<>();
            // Contain all entry's
            ArrayList<RingSession> rawDatas = dbManager.getAllDatasForAllEntrys();
            for (int i = 0; i < rawDatas.size(); i++) {
                String[] datePut = rawDatas.get(i).getStartDate().split(" ");
                String[] dateRemoved = rawDatas.get(i).getEndDate().split(" ");
                int totalTimePauses = rawDatas.get(i).computeTotalTimePause();
                formattedDatas.add(datePut[0]);
                formattedDatas.add(datePut[1]);
                formattedDatas.add(dateRemoved[0]);
                formattedDatas.add(dateRemoved[1]);
                if (!(rawDatas.get(i).getStatus() == Session.SessionStatus.RUNNING))
                    formattedDatas.add(String.valueOf(rawDatas.get(i).getSessionDuration()));
                else
                    formattedDatas.add(String.valueOf(DateUtils.Companion.getDateDiff(rawDatas.get(i).getStartDate(), DateUtils.Companion.getdateFormatted(new Date()), TimeUnit.MINUTES)));
                formattedDatas.add(String.valueOf(totalTimePauses));
                if (!(rawDatas.get(i).getStatus() == Session.SessionStatus.RUNNING))
                    formattedDatas.add(String.valueOf(rawDatas.get(i).getSessionDuration() - totalTimePauses));
                else
                    formattedDatas.add(String.valueOf(DateUtils.Companion.getDateDiff(rawDatas.get(i).getStartDate(), DateUtils.Companion.getdateFormatted(new Date()), TimeUnit.MINUTES) - totalTimePauses));
                csvWriter.writeColumnsDatas(formattedDatas);
                formattedDatas.clear();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error while saving datas into CSV: ", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getDataString() != null) {
            launchBackupRestore(data.getDataString());
        } else
            finish();
    }

}
