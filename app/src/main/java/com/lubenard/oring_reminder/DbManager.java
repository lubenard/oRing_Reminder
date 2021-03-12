package com.lubenard.oring_reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
    private static final String ringTablePut = "datetime_put";
    private static final String ringTableRemoved = "datetime_removed";

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
     * Get the contact list for a the main List
     * @return The datas fetched from the DB as a LinkedHashMap
     */
    public LinkedHashMap<Integer, RingModel> getAllContactsForMainList() {
        LinkedHashMap<Integer, RingModel> contactDatas = new LinkedHashMap<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved};
        Cursor cursor = readableDB.query(ringTable,  columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            contactDatas.put(cursor.getInt(cursor.getColumnIndex(ringTableId)), new RingModel(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved))));
            //Log.d(TAG, "getStatApp adding " + cursor.getString(cursor.getColumnIndex(contactsTableName)) + " and value " + cursor.getString(cursor.getColumnIndex(contactsTablePhoneNumber)));
        }
        cursor.close();
        return contactDatas;
    }

    /**
     * Create a new contact only if non existent:
     * Example: The contact named Toto does not exist, so let's create it
     * @param dateRemoved new contact name
     * @param datePut new Phone number
     */
    public void createNewDatesRing(String datePut, String dateRemoved) {
        ContentValues cv = new ContentValues();
        cv.put(ringTablePut, datePut);
        cv.put(ringTableRemoved, dateRemoved);

        //Log.d(TAG, String.format("update Contact: Create contact with new value (name = %d, PhoneNumber = %d, email = %d, address = %d, birtdate = %d",
        //        name, phoneNumber, email, address, birthday));
        writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateDatesRing(int id, String name, String phoneNumber, String email, String address, String birthday) {

    }

    /**
     * Close the db when finished using it.
     */
    public void closeDb() {
        if (writableDB != null) { writableDB.close();}
    }
}

