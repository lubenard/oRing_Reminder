package com.lubenard.oring_reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lubenard.oring_reminder.custom_components.RingModel;
import com.lubenard.oring_reminder.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Handle the DB used to save datas.
 * Theses datas are sent to Musk in order to build Skynet.
 */
public class DbManager extends SQLiteOpenHelper {

    public static final String TAG = "DBManager";

    static final String dbName = "dataDB";

    // Ring table
    private static final String ringTable = "ringTable";
    private static final String ringTableId = "id";
    // If isRunning is 1, the session is running.
    // Otherwise, the session is finished
    private static final String ringTableIsRunning = "isRunning";
    private static final String ringTablePut = "datetimePut";
    private static final String ringTableRemoved = "datetimeRemoved";
    // Computed by doing dateTimeRemoved - dateTimePut
    private static final String ringTableTimeWeared = "timeWeared";

    // Table registering pauses
    private static final String pausesTable = "pauseTable";
    private static final String pauseTableId = "id";
    // The link to ringTable
    private static final String pauseTableEntryId = "entryId";
    private static final String pauseTableIsRunning = "isRunning";
    private static final String pauseTablePut = "datetimePut";
    private static final String pauseTableRemoved = "datetimeRemoved";
    // Computed by doing dateTimeRemoved - dateTimePut
    private static final String pauseTableTimeRemoved = "timeRemoved";

    private SQLiteDatabase writableDB;
    private SQLiteDatabase readableDB;

    public DbManager(Context context) {
        super(context, dbName , null,1);
        this.writableDB = this.getWritableDatabase();
        this.readableDB = this.getReadableDatabase();
    }

    /**
     * If the db does not exist, create it with thoses fields.
     * @param db The database Object
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create apps table
        db.execSQL("CREATE TABLE " + ringTable + " (" + ringTableId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ringTableIsRunning + " INTEGER, " + ringTableTimeWeared + " INTEGER, " +
                ringTableRemoved + " DATETIME, " + ringTablePut + " DATETIME)");

        db.execSQL("CREATE TABLE " + pausesTable + " (" + pauseTableId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                pauseTableEntryId + " INTEGER, " + pauseTableIsRunning + " INTEGER, "
                + pauseTableTimeRemoved + " INTEGER, " + pauseTableRemoved + " DATETIME, "
                + pauseTablePut + " DATETIME)");

        Log.d(TAG, "The db has been created, this message should only appear once.");
    }

    /**
     * Get the dbName
     * @return the dbName
     */
    public static String getDBName() {
        return dbName;
    }

    /**
     * If you plan to improve the database, you might want to use this function as a automated
     * upgrade tool for db.
     * @param sqLiteDatabase
     * @param i Old DB version
     * @param i1 New DB version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**EditEntry
     * Get the datas list for a the main List
     * @return The datas fetched from the DB as a LinkedHashMap
     */
    public LinkedHashMap<Integer, RingModel> getAllDatasForMainList(boolean isDesc) {
        LinkedHashMap<Integer, RingModel> entryDatas = new LinkedHashMap<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableIsRunning, ringTableTimeWeared};
        Cursor cursor = readableDB.query(ringTable,  columns, null, null, null, null, (isDesc) ? ringTableId + " DESC" : null);

        while (cursor.moveToNext()) {
            entryDatas.put(cursor.getInt(cursor.getColumnIndex(ringTableId)), new RingModel(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared))));
        }
        cursor.close();
        return entryDatas;
    }

    /**
     * Create a new contact only if non existent:
     * Example: The contact named Toto does not exist, so let's create it
     * @param dateRemoved date at which user has removed the protection
     * @param datePut date at which user has put the protection
     * @param isRunning if the current session is running
     */
    public long createNewDatesRing(String datePut, String dateRemoved, int isRunning) {
        ContentValues cv = new ContentValues();
        cv.put(ringTablePut, datePut);
        cv.put(ringTableRemoved, dateRemoved);
        if (dateRemoved.equals("NOT SET YET"))
            cv.put(ringTableTimeWeared, dateRemoved);
        else
            cv.put(ringTableTimeWeared, Utils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES));
        cv.put(ringTableIsRunning, isRunning);

        return writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Check if there is running sessions.
     * Return the one running
     * @return
     */
    public HashMap<Integer, String> getAllRunningSessions() {
        HashMap <Integer, String> entryDatas = new HashMap<>();

        String[] columns = new String[]{ringTableId, ringTablePut};
        Cursor cursor = readableDB.query(ringTable,  columns, ringTableIsRunning + "=?",
                new String[]{"1"}, null, null, null);

        while (cursor.moveToNext())
            entryDatas.put(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                            cursor.getString(cursor.getColumnIndex(ringTablePut)));
        cursor.close();
        return entryDatas;
    }

    /**
     * Updated the datas contained in a entry
     * @param id the entry we want to update
     * @param datePut the new datePut
     * @param dateRemoved the new dateRemoved
     * @param isRunning the new isRunning
     */
    public void updateDatesRing(long id, String datePut, String dateRemoved, int isRunning) {
        if (id <= 0)
            return;
        ContentValues cv = new ContentValues();
        cv.put(ringTablePut, datePut);
        cv.put(ringTableRemoved, dateRemoved);
        if (dateRemoved.equals("NOT SET YET"))
            cv.put(ringTableTimeWeared, dateRemoved);
        else
            cv.put(ringTableTimeWeared, Utils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES));
        cv.put(ringTableIsRunning, isRunning);

        int u = writableDB.update(ringTable, cv, ringTableId + "=?", new String[]{String.valueOf(id)});
        if (u == 0) {
            Log.d(TAG, "ringUpdate: update does not seems to work, insert data: (for id = " + id);
            cv.put(ringTablePut, datePut);
            cv.put(ringTableRemoved, dateRemoved);
            cv.put(ringTableTimeWeared, Utils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES));
            cv.put(ringTableIsRunning, isRunning);
            writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /**
     * Get the details for a entry
     * @param entryId the id we want to have details for
     * @return the following fields -> ringTablePut, ringTableRemoved, ringTableTimeWeared, ringTableIsRunning
     * in the form of a ArrayList
     */
    public ArrayList<String> getEntryDetails(long entryId) {
        if (entryId <= 0)
            return null;
        ArrayList<String> entryDatas = new ArrayList<>();

        String[] columns = new String[]{ringTablePut, ringTableRemoved, ringTableTimeWeared, ringTableIsRunning};
        Cursor cursor = readableDB.query(ringTable, columns,ringTableId + "=?",
                new String[]{String.valueOf(entryId)}, null, null, null);

        cursor.moveToFirst();
        entryDatas.add(cursor.getString(cursor.getColumnIndex(ringTablePut)));
        entryDatas.add(cursor.getString(cursor.getColumnIndex(ringTableRemoved)));
        entryDatas.add(cursor.getString(cursor.getColumnIndex(ringTableTimeWeared)));
        entryDatas.add(cursor.getString(cursor.getColumnIndex(ringTableIsRunning)));
        cursor.close();
        return entryDatas;
    }

    /**
     * Delete a entry
     * @param entryId the id of the contact we want to delete
     */
    public void deleteEntry(long entryId)
    {
        if (entryId > 0)
            writableDB.delete(ringTable,ringTableId + "=?", new String[]{String.valueOf(entryId)});
    }

    /**
     * Set a session as finished for given entryId
     * @param entryId set the session as finished for given id
     */
    public void endSession(long entryId) {
        if (entryId < 0)
            return;

        // First we catch the dateTablePut date
        String[] columns = new String[]{ringTablePut};
        Cursor cursor = readableDB.query(ringTable, columns,ringTableId + "=?",
                new String[]{String.valueOf(entryId)}, null, null, null);
        cursor.moveToFirst();

        // Then we set our values:
        // We need to recompute the date
        // And set the isRunning to 0
        String dateRemoved = Utils.getdateFormatted(new Date());
        ContentValues cv = new ContentValues();
        cv.put(ringTableRemoved, dateRemoved);
        cv.put(ringTableTimeWeared, Utils.getDateDiff(cursor.getString(cursor.getColumnIndex(ringTablePut)), dateRemoved, TimeUnit.MINUTES));
        cv.put(ringTableIsRunning, 0);

        int u = writableDB.update(ringTable, cv, ringTableId + "=?", new String[]{String.valueOf(entryId)});
        if (u == 0) {
            Log.d(TAG, "ringUpdate: update does not seems to work, insert data: (for id = " + entryId);
            cv.put(ringTableRemoved, dateRemoved);
            cv.put(ringTableTimeWeared, Utils.getDateDiff(cursor.getString(cursor.getColumnIndex(ringTablePut)), dateRemoved, TimeUnit.MINUTES));
            writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /**
     * Create a new Pause
     * @param entryId the id to link to the pause
     * @param dateRemoved the date the user stopped wearing protection
     * @param datePut the date the user put the new protection again
     * @param isRunning if the pause is running
     * @return the id of the pause entry
     */
    public long createNewPause(long entryId, String dateRemoved, String datePut, int isRunning) {
        ContentValues cv = new ContentValues();
        cv.put(pauseTableRemoved, dateRemoved);
        cv.put(pauseTablePut, datePut);
        cv.put(pauseTableEntryId, entryId);
        Log.d(TAG, "pauseTablePut = " + datePut);
        if (datePut.equals("NOT SET YET"))
            cv.put(pauseTableTimeRemoved, datePut);
        else
            cv.put(pauseTableTimeRemoved, Utils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES));
        cv.put(pauseTableIsRunning, isRunning);

        return writableDB.insertWithOnConflict(pausesTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * This function is used to backup into a file
     * @return All the datas for all the entrys.
     */
    public ArrayList<RingModel> getAllDatasForAllEntrys() {
        ArrayList<RingModel> datas = new ArrayList<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableIsRunning, ringTableTimeWeared};
        Cursor cursor = readableDB.query(ringTable,  columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            datas.add(new RingModel(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared))));
        }
        cursor.close();
        return datas;
    }

    public ArrayList<RingModel> getAllPausesForId(long entryId, boolean isDesc) {
        ArrayList<RingModel> datas = new ArrayList<>();

        String[] columns = new String[]{pauseTableId, pauseTableRemoved, pauseTablePut, pauseTableIsRunning, pauseTableTimeRemoved};
        Cursor cursor = readableDB.query(pausesTable,  columns, pauseTableEntryId + "=?", new String[]{String.valueOf(entryId)}, null, null,
                (isDesc) ? pauseTableId + " DESC": null);

        while (cursor.moveToNext()) {
            datas.add(new RingModel(cursor.getInt(cursor.getColumnIndex(pauseTableId)),
                    cursor.getString(cursor.getColumnIndex(pauseTablePut)),
                    cursor.getString(cursor.getColumnIndex(pauseTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableTimeRemoved))));
        }
        cursor.close();
        return datas;
    }

    public void deletePauseEntry(long entryId) {
        if (entryId > 0)
            writableDB.delete(pausesTable,pauseTableId + "=?", new String[]{String.valueOf(entryId)});
    }

    /**
     * Close the db when finished using it.
     */
    public void closeDb() {
        if (writableDB != null) { writableDB.close();}
        if (readableDB != null) { readableDB.close();}
    }
}

