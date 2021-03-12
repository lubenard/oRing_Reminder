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

    /**
     * Get the datas list for a the main List
     * @return The datas fetched from the DB as a LinkedHashMap
     */
    public LinkedHashMap<Integer, RingModel> getAllDatasForMainList() {
        LinkedHashMap<Integer, RingModel> contactDatas = new LinkedHashMap<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableIsRunning, ringTableTimeWeared};
        Cursor cursor = readableDB.query(ringTable,  columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            contactDatas.put(cursor.getInt(cursor.getColumnIndex(ringTableId)), new RingModel(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared))));
        }
        cursor.close();
        return contactDatas;
    }

    public static long getDateDiff(String sDate1, String sDate2, TimeUnit timeUnit)
    {
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate1);
            Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sDate2);

            long diffInMillies = date2.getTime() - date1.getTime();
            Log.d(TAG, "User weared protection during " + timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS));
            return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Create a new contact only if non existent:
     * Example: The contact named Toto does not exist, so let's create it
     * @param dateRemoved date at which user has removed the protection
     * @param datePut date at which user has put the protection
     */
    public void createNewDatesRing(String datePut, String dateRemoved) {
        ContentValues cv = new ContentValues();
        cv.put(ringTablePut, datePut);
        cv.put(ringTableRemoved, dateRemoved);
        cv.put(ringTableTimeWeared, getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES));

        writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateDatesRing(int id, String name, String phoneNumber, String email, String address, String birthday) {

    }

    /**
     * Close the db when finished using it.
     */
    public void closeDb() {
        if (writableDB != null) { writableDB.close();}
        if (readableDB != null) { readableDB.close();}
    }
}

