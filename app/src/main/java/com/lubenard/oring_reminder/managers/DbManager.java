package com.lubenard.oring_reminder.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.lubenard.oring_reminder.custom_components.BreakSession;
import com.lubenard.oring_reminder.custom_components.RingSession;
import com.lubenard.oring_reminder.custom_components.Session;
import com.lubenard.oring_reminder.custom_components.Spermograms;
import com.lubenard.oring_reminder.utils.DateUtils;
import com.lubenard.oring_reminder.utils.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Handle the DB used to save datas.
 * Theses datas might be sent to Musk in order to build Skynet.
 */
public class DbManager extends SQLiteOpenHelper {

    private static final String TAG = "DBManager";

    private static final String dbName = "dataDB";
    private static final int DATABASE_VERSION = 3;

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

    // Table registering break
    private static final String pausesTable = "pauseTable";
    private static final String pauseTableId = "id";
    // The link to break table
    private static final String pauseTableEntryId = "entryId";
    private static final String pauseTableIsRunning = "isRunning";
    private static final String pauseTablePut = "datetimePut";
    private static final String pauseTableRemoved = "datetimeRemoved";
    // Computed by doing dateTimeRemoved - dateTimePut
    private static final String pauseTableTimeRemoved = "timeRemoved";

    // Spermograms table table
    private static final String spermoTable = "spermoTable";
    private static final String spermoTableId = "id";
    private static final String spermoTableDateAdded = "dateAdded";
    private static final String spermoTableFileLocation = "fileLocation";

    private SQLiteDatabase writableDB;
    private SQLiteDatabase readableDB;

    public DbManager(Context context) {
        super(context, dbName , null, DATABASE_VERSION);
        this.writableDB = this.getWritableDatabase();
        this.readableDB = this.getReadableDatabase();
        Log.d(TAG, "New Dbmanager is created at " + this);
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

        db.execSQL("CREATE TABLE " + spermoTable + " (" + spermoTableId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                spermoTableDateAdded + " INTEGER, " + spermoTableFileLocation + " TEXT)");

        Log.d(TAG, "The db has been created, this message should only appear once.");
    }

    /**
     * Get the dbName
     * @return the dbName
     */
    public static String getDBName() {
        return dbName;
    }

    public int getVersion() {
        return readableDB.getVersion();
    }

    /**
     * If you plan to improve the database, you might want to use this function as a automated
     * upgrade tool for db.
     * @param db the Db
     * @param i Old DB version
     * @param i1 New DB version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.d(TAG, "Old db version is " + i + ", new db version is " + i1);
        if (i == 1 && i1 == 2) {
            Log.d(TAG, "Updating db from v1.0 to v1.1");
            db.execSQL("CREATE TABLE " + pausesTable + " (" + pauseTableId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    pauseTableEntryId + " INTEGER, " + pauseTableIsRunning + " INTEGER, "
                    + pauseTableTimeRemoved + " INTEGER, " + pauseTableRemoved + " DATETIME, "
                    + pauseTablePut + " DATETIME)");

        } else if (i == 2 && i1 == 3) {
            Log.d(TAG, "Updating db from v1.2.1 to v1.3");
            db.execSQL("CREATE TABLE " + spermoTable + " (" + spermoTableId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    spermoTableDateAdded + " INTEGER, " + spermoTableFileLocation + " TEXT)");
        }
    }

    /**
     * EditEntry
     * Get the datas list for a the main List
     * @return The datas fetched from the DB as a LinkedHashMap
     */
    public LinkedHashMap<Integer, RingSession> getAllDatasForMainList(boolean reversed) {
        LinkedHashMap<Integer, RingSession> entryDatas = new LinkedHashMap<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableIsRunning, ringTableTimeWeared};
        Cursor cursor = readableDB.query(ringTable,  columns, null, null, null, null, (reversed) ? ringTableId + " DESC" : null);

        while (cursor.moveToNext()) {
            entryDatas.put(cursor.getInt(cursor.getColumnIndex(ringTableId)), new RingSession(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared))));
        }
        cursor.close();
        return entryDatas;
    }

    /**
     *
     * @param dateRemoved date at which user has removed the protection
     * @param datePut date at which user has put the protection
     * @param isRunning if the current session is running
     */
    public long createNewEntry(String datePut, String dateRemoved, int isRunning) {
        ContentValues cv = new ContentValues();
        cv.put(ringTablePut, datePut);
        cv.put(ringTableRemoved, dateRemoved);
        if (dateRemoved.equals("NOT SET YET"))
            cv.put(ringTableTimeWeared, dateRemoved);
        else
            cv.put(ringTableTimeWeared, DateUtils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES));
        cv.put(ringTableIsRunning, isRunning);

        return writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Check if there is running sessions.
     * Return the one running
     * @return Hashmap containing ringTableId and DatePut of all Sessions considered running
     */
    public ArrayList<RingSession> getAllRunningSessions() {
        ArrayList<RingSession> entryDatas = new ArrayList<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableTimeWeared, ringTableIsRunning};
        Cursor cursor = readableDB.query(ringTable,  columns, ringTableIsRunning + "=?",
                new String[]{"1"}, null, null, null);

        while (cursor.moveToNext())
            entryDatas.add(new RingSession(
                    cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared))
            ));
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
        if (datePut != null)
            cv.put(ringTablePut, datePut);

        if (dateRemoved != null)
            cv.put(ringTableRemoved, dateRemoved);

        if (dateRemoved != null && dateRemoved.equals("NOT SET YET"))
            cv.put(ringTableTimeWeared, dateRemoved);
        else if (dateRemoved != null)
            cv.put(ringTableTimeWeared, DateUtils.getDateDiff(datePut, dateRemoved, TimeUnit.MINUTES));

        if (isRunning != -1)
            cv.put(ringTableIsRunning, isRunning);

        int u = writableDB.update(ringTable, cv, ringTableId + "=?", new String[]{String.valueOf(id)});
        if (u == 0) {
            Log.d(TAG, "ringUpdate: update does not seems to work, insert data: (for id = " + id);
            writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    /**
     * Get the details for a entry
     * @param entryId the id we want to have details for
     * @return the following fields -> ringTablePut, ringTableRemoved, ringTableTimeWeared, ringTableIsRunning
     * in the form of a RingModel object
     */
    public RingSession getEntryDetails(long entryId) {
        if (entryId <= 0)
            return null;

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableTimeWeared, ringTableIsRunning};
        Cursor cursor = readableDB.query(ringTable, columns,ringTableId + "=?",
                new String[]{String.valueOf(entryId)}, null, null, null);

        cursor.moveToFirst();
        RingSession data = new RingSession(cursor.getInt(cursor.getColumnIndex(ringTableId)), cursor.getString(cursor.getColumnIndex(ringTablePut)), cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)), cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared)));

        cursor.close();
        return data;
    }

    /**
     * Delete a entry
     * @param entryId the id of the contact we want to delete
     */
    public void deleteEntry(long entryId) {
        if (entryId > 0) {
            // Also delete pauses linked to this entry
            writableDB.delete(pausesTable, pauseTableEntryId + "=?", new String[]{String.valueOf(entryId)});
            writableDB.delete(ringTable, ringTableId + "=?", new String[]{String.valueOf(entryId)});
        }
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
        String dateRemoved = DateUtils.getdateFormatted(new Date());
        ContentValues cv = new ContentValues();
        cv.put(ringTableRemoved, dateRemoved);
        cv.put(ringTableTimeWeared, DateUtils.getDateDiff(cursor.getString(cursor.getColumnIndex(ringTablePut)), dateRemoved, TimeUnit.MINUTES));
        cv.put(ringTableIsRunning, 0);

        int u = writableDB.update(ringTable, cv, ringTableId + "=?", new String[]{String.valueOf(entryId)});
        if (u == 0) {
            Log.d(TAG, "endSession: update does not seems to work, insert data: (for id = " + entryId);
            writableDB.insertWithOnConflict(ringTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
        // End pause is session is set to finish
        endPause(entryId);
    }

    /**
     * End pause for given sessionId
     * @param entryId sessionId
     */
    public void endPause(long entryId) {
        // First we catch the dateTablePut date
        String[] columns = new String[]{pauseTableRemoved};
        Cursor cursor = readableDB.query(pausesTable, columns,pauseTableEntryId + "=?"
                        + " AND " + pauseTableIsRunning + "=?",
                new String[]{String.valueOf(entryId), "1"}, null, null, null);

        if (cursor.moveToFirst()) {
            Log.d(TAG, "Found entry for endPause");
            // Then we set our values:
            // We need to recompute the date
            // And set the isRunning to 0
            String datePut = DateUtils.getdateFormatted(new Date());
            ContentValues cv = new ContentValues();
            cv.put(pauseTablePut, datePut);
            cv.put(pauseTableTimeRemoved, DateUtils.getDateDiff(cursor.getString(cursor.getColumnIndex(pauseTableRemoved)), datePut, TimeUnit.MINUTES));
            cv.put(pauseTableIsRunning, 0);

            int u = writableDB.update(pausesTable, cv, pauseTableEntryId + "=? AND " + pauseTableIsRunning + "=?",
                    new String[]{String.valueOf(entryId), "1"});
            if (u == 0) {
                Log.d(TAG, "endPause: update does not seems to work, insert data: (for id = " + entryId);
                writableDB.insertWithOnConflict(pausesTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
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
        Log.d(TAG, "pauseTableRemoved = " + cv.get(pauseTableRemoved));
        Log.d(TAG, "pauseTablePut = " + cv.get(pauseTablePut));
        if (datePut.equals("NOT SET YET"))
            cv.put(pauseTableTimeRemoved, datePut);
        else
            cv.put(pauseTableTimeRemoved, DateUtils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES));
        cv.put(pauseTableIsRunning, isRunning);

        return writableDB.insertWithOnConflict(pausesTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public long createNewPause(BreakSession breakSession) {
        return createNewPause(breakSession.getSessionId(), breakSession.getStartDate(), breakSession.getEndDate(), (breakSession.getStatus() == Session.SessionStatus.RUNNING) ? 1 : 0);
    }

    /**
     * Updated the datas contained in a entry
     * @param pauseId the entry we want to update
     * @param datePut the new datePut
     * @param dateRemoved the new dateRemoved
     * @param isRunning the new isRunning
     */
    public long updatePause(long pauseId, String dateRemoved, String datePut, int isRunning) {
        if (pauseId <= 0)
            return -1;
        ContentValues cv = new ContentValues();
        cv.put(pauseTableRemoved, dateRemoved);
        cv.put(pauseTablePut, datePut);
        if (datePut.equals("NOT SET YET"))
            cv.put(pauseTableTimeRemoved, datePut);
        else
            cv.put(pauseTableTimeRemoved, DateUtils.getDateDiff(dateRemoved, datePut, TimeUnit.MINUTES));
        cv.put(pauseTableIsRunning, isRunning);

        int u = writableDB.update(pausesTable, cv, pauseTableId + "=?", new String[]{String.valueOf(pauseId)});
        if (u == 0) {
            Log.d(TAG, "pauseUpdate: update does not seems to work, insert data: (for id = " + pauseId);
            return writableDB.insertWithOnConflict(pausesTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
        return -1;
    }

    public long updatePause(BreakSession breakSession) {
        return updatePause(breakSession.getId(), breakSession.getStartDate(), breakSession.getEndDate(), (breakSession.getStatus() == Session.SessionStatus.RUNNING) ? 1 : 0);
    }

    public ArrayList<RingSession> searchEntryInDb(String date) {
        ArrayList<RingSession> datas = new ArrayList<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableTimeWeared, ringTableIsRunning};

        Cursor cursor = readableDB.query(ringTable, columns,ringTablePut + " LIKE ?" + " OR " + ringTableRemoved + " LIKE ?",
                new String[]{date + "%", date + "%"}, null, null, null, "10");

        while (cursor.moveToNext()) {
            datas.add(new RingSession(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared))));
        }
        cursor.close();
        return datas;
    }

    /**
     * Get the last running entry
     * @return A ringModel containing last Running entry.
     * Primarily used for widget
     */
    public RingSession getLastRunningEntry() {
        Log.d(TAG, "DbManager is at " + this);

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableTimeWeared, ringTableIsRunning};
        Cursor cursor = readableDB.query(ringTable, columns,ringTableIsRunning + "!= ?",
                new String[]{"0"}, null, null, pauseTableId + " DESC");

        RingSession data = null;
        if (cursor.moveToFirst()) {
            data = new RingSession(cursor.getInt(cursor.getColumnIndex(ringTableId)), cursor.getString(cursor.getColumnIndex(ringTablePut)), cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)), cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared)));
            data.setBreakList(getAllBreaksForId(cursor.getInt(cursor.getColumnIndex(ringTableId)), true));
        }
        cursor.close();
        return data;
    }

    /**
     * Get the last running break for specified id
     * @return A ringModel containing last Running entry.
     * Primarily used for widget
     */
    public BreakSession getLastRunningPauseForId(long entryId) {
        String[] columns = new String[]{pauseTableId, pauseTableRemoved, pauseTablePut, pauseTableIsRunning, pauseTableTimeRemoved, pauseTableEntryId};
        Cursor cursor = readableDB.query(pausesTable,  columns, pauseTableEntryId + "=? AND " + pauseTableIsRunning + "=?", new String[]{String.valueOf(entryId), "1"},
                null, null, pauseTableId + " DESC");

        BreakSession datas = null;

        if (cursor.moveToFirst()) {
            datas = new BreakSession(
                    cursor.getInt(cursor.getColumnIndex(pauseTableId)),
                    cursor.getString(cursor.getColumnIndex(pauseTableRemoved)),
                    cursor.getString(cursor.getColumnIndex(pauseTablePut)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableTimeRemoved)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableEntryId)));
        }
        cursor.close();
        return datas;
    }

    /**
     * This function is used to backup into a file
     * @return All the datas for all the entrys.
     */
    public ArrayList<RingSession> getAllDatasForAllEntrys() {
        ArrayList<RingSession> datas = new ArrayList<>();

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableIsRunning, ringTableTimeWeared};
        Cursor cursor = readableDB.query(ringTable,  columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            RingSession session = new RingSession(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared)));
            session.setBreakList(getAllBreaksForId(cursor.getInt(cursor.getColumnIndex(ringTableId)), true));

            datas.add(session);
        }
        cursor.close();
        return datas;
    }

    /**
     * This function is used to backup into a file
     * @return All the datas for all the entrys.
     */
    public ArrayList<RingSession> getEntriesForMonth(Calendar date) {
        ArrayList<RingSession> datas = new ArrayList<>();

        String month = String.valueOf(date.get(Calendar.MONTH) + 1);

         if (month.length() == 1)
             month = "0" + month;

        String year = String.valueOf(date.get(Calendar.YEAR));

        String[] columns = new String[]{ringTableId, ringTablePut, ringTableRemoved, ringTableIsRunning, ringTableTimeWeared};

        Log.d(TAG, "getEntriesForMonth query say " + year + "-" + month + "%" + " as request");

        Cursor cursor = readableDB.query(ringTable,  columns, ringTablePut + " like ?", new String[]{year + "-" + month + "%"},null, null, null);

        while (cursor.moveToNext()) {
            datas.add(new RingSession(cursor.getInt(cursor.getColumnIndex(ringTableId)),
                    cursor.getString(cursor.getColumnIndex(ringTablePut)),
                    cursor.getString(cursor.getColumnIndex(ringTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(ringTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(ringTableTimeWeared))));
        }
        cursor.close();
        Log.d(TAG, "getEntriesForMonth return " + datas.size() + " results");
        return datas;
    }

    /**
     * Return a array list of all pauses for given id
     * @param entryId id to look for
     * @param isDesc set if the pauses should be desc or not
     * @return a Arraylist containing RingModel objects of all pauses
     */
    public ArrayList<BreakSession> getAllBreaksForId(long entryId, boolean isDesc) {
        ArrayList<BreakSession> datas = new ArrayList<>();

        String[] columns = new String[]{pauseTableId, pauseTableRemoved, pauseTablePut, pauseTableIsRunning, pauseTableTimeRemoved, pauseTableEntryId};
        Cursor cursor = readableDB.query(pausesTable,  columns, pauseTableEntryId + "=?", new String[]{String.valueOf(entryId)}, null, null,
                (isDesc) ? pauseTableId + " DESC": null);

        while (cursor.moveToNext()) {
            datas.add(new BreakSession(cursor.getInt(cursor.getColumnIndex(pauseTableId)),
                    cursor.getString(cursor.getColumnIndex(pauseTableRemoved)),
                    cursor.getString(cursor.getColumnIndex(pauseTablePut)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableTimeRemoved)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableEntryId))));
        }
        cursor.close();
        return datas;
    }

    /**
     * Return a array list of all pauses for given id
     * @param entryId id to look for
     * @return a Break Session in the form of a RingSession
     */
    public BreakSession getBreakForId(long entryId) {
        BreakSession breakSession;

        String[] columns = new String[]{pauseTableId, pauseTableRemoved, pauseTableEntryId, pauseTablePut, pauseTableIsRunning, pauseTableTimeRemoved};
        Cursor cursor = readableDB.query(pausesTable,  columns, pauseTableId + "=?", new String[]{String.valueOf(entryId)}, null, null, null);

        cursor.moveToFirst();
        breakSession = new BreakSession(cursor.getInt(cursor.getColumnIndex(pauseTableId)),
                    cursor.getString(cursor.getColumnIndex(pauseTableRemoved)),
                    cursor.getString(cursor.getColumnIndex(pauseTablePut)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableTimeRemoved)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableEntryId)));
        cursor.close();
        return breakSession;
    }

    /**
     * Delete pause in db
     * @param pauseId the pauseId to delete
     */
    public void deletePauseEntry(long pauseId) {
        if (pauseId > 0)
            writableDB.delete(pausesTable,pauseTableId + "=?", new String[]{String.valueOf(pauseId)});
    }

    /**
     * This function is used to backup into a file
     * @return All the datas for all the entrys.
     */
    public ArrayList<RingSession> getAllDatasForAllPauses() {
        ArrayList<RingSession> datas = new ArrayList<>();

        String[] columns = new String[]{pauseTableEntryId, pauseTablePut, pauseTableRemoved, pauseTableIsRunning, pauseTableTimeRemoved};
        Cursor cursor = readableDB.query(pausesTable,  columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            datas.add(new RingSession(cursor.getInt(cursor.getColumnIndex(pauseTableEntryId)),
                    cursor.getString(cursor.getColumnIndex(pauseTablePut)),
                    cursor.getString(cursor.getColumnIndex(pauseTableRemoved)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableIsRunning)),
                    cursor.getInt(cursor.getColumnIndex(pauseTableTimeRemoved))));
        }
        cursor.close();
        return datas;
    }

    public long importNewSpermo(String uri) {
        ContentValues cv = new ContentValues();
        cv.put(spermoTableDateAdded, DateUtils.getdateFormatted(new Date()).split(" ")[0]);
        cv.put(spermoTableFileLocation, uri);

        return writableDB.insertWithOnConflict(spermoTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Get the details for spermogram
     * @param entryId the id we want to have details for
     */
    public Spermograms getSpermoEntryForId(long entryId) {
        if (entryId <= 0)
            return null;

        String[] columns = new String[]{spermoTableDateAdded, spermoTableFileLocation};
        Cursor cursor = readableDB.query(spermoTable, columns,spermoTableId + "=?",
                new String[]{String.valueOf(entryId)}, null, null, null);

        cursor.moveToFirst();
        Spermograms data = new Spermograms(0, cursor.getString(cursor.getColumnIndex(spermoTableDateAdded)),
                Uri.parse(cursor.getString(cursor.getColumnIndex(spermoTableFileLocation))));

        cursor.close();
        return data;
    }

    public void updateSpermogram(long entryId, String new_spermogram_date) {
        ContentValues cv = new ContentValues();
        cv.put(spermoTableDateAdded, new_spermogram_date);

        int u = writableDB.update(spermoTable, cv, pauseTableId + "=?", new String[]{String.valueOf(entryId)});
        if (u == 0) {
            Log.d(TAG, "pauseUpdate: update does not seems to work, insert data: (for id = " + entryId);
            writableDB.insertWithOnConflict(spermoTable, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public LinkedHashMap<Integer, Spermograms> getAllSpermograms() {
        LinkedHashMap<Integer, Spermograms> entryDatas = new LinkedHashMap<>();

        String[] columns = new String[]{spermoTableId, spermoTableDateAdded, spermoTableFileLocation};
        Cursor cursor = readableDB.query(spermoTable,  columns, null, null, null, null, null);

        while (cursor.moveToNext()) {
            entryDatas.put(cursor.getInt(cursor.getColumnIndex(spermoTableId)), new Spermograms(cursor.getInt(cursor.getColumnIndex(spermoTableId)),
                    cursor.getString(cursor.getColumnIndex(spermoTableDateAdded)),
                    Uri.parse(cursor.getString(cursor.getColumnIndex(spermoTableFileLocation)))));
        }
        cursor.close();
        return entryDatas;
    }

    public void deleteSpermoEntry(long entryId) {
        if (entryId > 0) {
            String pdfUrl = getSpermoEntryForId(entryId).getFileAddr().toString().substring(7);
            File pdf = new File(pdfUrl);
            File pdfIcon = new File(pdfUrl + ".jpg");
            if (pdf.exists())
                pdf.delete();
            if (pdfIcon.exists())
                pdfIcon.delete();
            writableDB.delete(spermoTable, ringTableId + "=?", new String[]{String.valueOf(entryId)});
        }
    }

    /**
     * Close the db when finished using it.
     */
    public void closeDb() {
        Log.d(TAG, "Close db");
        if (writableDB != null) { writableDB.close();}
        if (readableDB != null) { readableDB.close();}
    }
}

