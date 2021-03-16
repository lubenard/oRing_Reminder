package com.lubenard.oring_reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

    // Contact table
    private static final String ringTable = "ringTable";
    private static final String ringTableId = "id";
    private static final String ringTableIsRunning = "isRunning";
    private static final String ringTablePut = "datetimePut";
    private static final String ringTableRemoved = "datetimeRemoved";
    private static final String ringTableTimeWeared = "timeWeared";

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
    public LinkedHashMap<Integer, RingModel> getAllDatasForMainList() {
        LinkedHashMap<Integer, RingModel> entryDatas = new LinkedHashMap<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableIsRunning, ringTableTimeWeared};
        Cursor cursor = readableDB.query(ringTable,  columns, null, null, null, null, ringTableId + " DESC");

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
    public void createNewDatesRing(String datePut, String dateRemoved, int isRunning) {
        ContentValues cv = new ContentValues();
        cv.put(ringTablePut, datePut);
        cv.put(ringTableRemoved, dateRemoved);
        if (dateRemoved.equals("NOT SET YET"))
            cv.put(ringTableTimeWeared, dateRemoved);
        else
            cv.put(ringTableTimeWeared, Utils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES));
        cv.put(ringTableIsRunning, isRunning);

        writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Check if there is running sessions.
     * Return the one running
     * @return
     */
    public HashMap<Integer, String> getRunningSessions() {
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
    public void updateDatesRing(int id, String datePut, String dateRemoved, int isRunning) {
        ContentValues cv = new ContentValues();
        cv.put(ringTablePut, datePut);
        cv.put(ringTableRemoved, dateRemoved);
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
    public ArrayList<String> getEntryDetails(int entryId) {

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
    public void deleteEntry(int entryId)
    {
        if (entryId > 0)
            writableDB.delete(ringTable,ringTableId + "=?", new String[]{String.valueOf(entryId)});
    }

    /**
     * Close the db when finished using it.
     */
    public void closeDb() {
        if (writableDB != null) { writableDB.close();}
        if (readableDB != null) { readableDB.close();}
    }
}

