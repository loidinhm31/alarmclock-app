package com.example.alarmclock.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.example.alarmclock.model.AlarmModel;


import java.util.ArrayList;

public class AlarmDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AlarmTable.db";


    private static final String SQL_CREATE_ALARM_TABLE = "CREATE TABLE " +
            Schema.FeedEntry.TABLE_NAME + " (" +
            Schema.FeedEntry._ID + " INTEGER PRIMARY KEY," +
            Schema.FeedEntry.ALARM_HOUR + " INT," +
            Schema.FeedEntry.ALARM_MINUTE + " INT," +
            Schema.FeedEntry.ALARM_ENABLED + " BOOLEAN)";

    private static final String SQL_DELETE_ALARM_TABLE =
            "DROP TABLE IF EXISTS " +
            Schema.FeedEntry.TABLE_NAME;

    // List of all alarm
    private final ArrayList<AlarmModel> alarmList = new ArrayList<>();

    public AlarmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ALARM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ALARM_TABLE);

        // Create new one
        onCreate(db);
    }

    // Add an alarm to database
    public long addAlarm(AlarmModel alarmModel) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Schema.FeedEntry.ALARM_HOUR, alarmModel.getHour());
        values.put(Schema.FeedEntry.ALARM_MINUTE, alarmModel.getMinute());
        values.put(Schema.FeedEntry.ALARM_ENABLED, alarmModel.isEnable());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(Schema.FeedEntry.TABLE_NAME, null, values);

        return newRowId;
    }

    // Delete an alarm from database
    public int deleteAlarm(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Define "WHERE" part of query
        String selection = Schema.FeedEntry._ID + " = ? ";
        // Specify argument in placeholder order
        String[] selectionArgs = new String[] {String.valueOf(id)};
        // Issue SQL statement
        int deleteRow = db.delete(Schema.FeedEntry.TABLE_NAME, selection, selectionArgs);

        return deleteRow;
    }



    // Update an alarm in database
    public int updateAlarm(int itemID, int hour, int minute, int isEnable) {
        SQLiteDatabase db = this.getWritableDatabase();

        // New value for one column
        final ContentValues values = new ContentValues();
        values.put(Schema.FeedEntry._ID, itemID);
        values.put(Schema.FeedEntry.ALARM_HOUR, hour);
        values.put(Schema.FeedEntry.ALARM_MINUTE, minute);
        values.put(Schema.FeedEntry.ALARM_ENABLED, isEnable);

        // Which row to update, based on the title
        String selection = Schema.FeedEntry._ID + " = ?";
        String[] selectionArgs = new String[] {String.valueOf(itemID)};

        // The number of rows affected in the database
        int count = db.update(
                Schema.FeedEntry.TABLE_NAME,    // The table name in which the data will be updated
                values,                         // The ContentValues instance with the new data
                selection,                      // The selection which specifies which row is updated. ? symbols are parameters.
                selectionArgs);                 // The actual parameters for the selection as a String[].

        return count;
    }



    // Get all alarm in List
    public ArrayList<AlarmModel> getAllAlarms() {
        alarmList.clear();

        SQLiteDatabase db = this.getReadableDatabase();

        // SELECT * FROM Schema.FeedEntry.TABLE_NAME

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                Schema.FeedEntry._ID,
                Schema.FeedEntry.ALARM_HOUR,
                Schema.FeedEntry.ALARM_MINUTE,
                Schema.FeedEntry.ALARM_ENABLED
        };

        // The results sorted in the resulting Cursor
        String sortOrder =
                Schema.FeedEntry._ID+ " ASC";

        Cursor cursor = db.query(
                Schema.FeedEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );


        try {
            // If moveToFirst() returns false then cursor is empty
            if(!cursor.moveToFirst()) {
                return new ArrayList<>();
            }

            do {
                // Loop through cursor
                AlarmModel alarmModel = new AlarmModel();
                alarmModel.setItemID(cursor.getInt(cursor.getColumnIndex(Schema.FeedEntry._ID)));
                alarmModel.setHour(cursor.getInt(cursor.getColumnIndex(Schema.FeedEntry.ALARM_HOUR)));
                alarmModel.setMinute(cursor.getInt(cursor.getColumnIndex(Schema.FeedEntry.ALARM_MINUTE)));
                alarmModel.setEnable(cursor.getInt(cursor.getColumnIndex(Schema.FeedEntry.ALARM_ENABLED)));

                alarmList.add(alarmModel);

            } while (cursor.moveToNext());

            return alarmList;

        } finally {
            // Close the Cursor once you are done to avoid memory leaks
            cursor.close();

            // close the database
            db.close();
        }

    }




}
