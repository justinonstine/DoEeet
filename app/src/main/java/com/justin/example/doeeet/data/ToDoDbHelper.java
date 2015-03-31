package com.justin.example.doeeet.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 *  Helper class for creating database tables and deleting contents if an upgrade has occurred
 */
public class ToDoDbHelper extends SQLiteOpenHelper {

    //  In theory if I increment the database version it will start anew. However, in practice this
    //  isn't the case. I needed to change the database name as well.  :/
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "tasklist.db";

    public ToDoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    //  Create the two tables...
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TODO_LIST_TABLE = "CREATE TABLE " + ToDoContract.ToDoList.TABLE_NAME + "(" +
            ToDoContract.ToDoList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ToDoContract.ToDoList.COLUMN_NAME + " TEXT NOT NULL);";
        Log.d("sql", SQL_CREATE_TODO_LIST_TABLE);

        final String SQL_CREATE_TODO_TABLE = "CREATE TABLE " + ToDoContract.ToDoEntry.TABLE_NAME + "(" +
            ToDoContract.ToDoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ToDoContract.ToDoEntry.COLUMN_LIST_ID + " INTEGER NOT NULL, " +
            ToDoContract.ToDoEntry.COLUMN_SUMMARY + " TEXT NOT NULL, " +
            ToDoContract.ToDoEntry.COLUMN_DETAILS + " TEXT NOT NULL, " +
            ToDoContract.ToDoEntry.COLUMN_COMPLETE + " INTEGER NOT NULL, " +
            " FOREIGN KEY (" + ToDoContract.ToDoEntry.COLUMN_LIST_ID + ") REFERENCES " +
            ToDoContract.ToDoList.TABLE_NAME + " (" + ToDoContract.ToDoList._ID + "));";
        Log.d("sql", SQL_CREATE_TODO_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TODO_LIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d("sql", "onupgrade about to drop");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ToDoContract.ToDoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ToDoContract.ToDoList.TABLE_NAME);
    }
}
