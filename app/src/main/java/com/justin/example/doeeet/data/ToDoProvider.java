package com.justin.example.doeeet.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 *  Content provider for the list of lists and list of tasks
 */
public class ToDoProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ToDoDbHelper mOpenHelper;

    static final int TODO = 100;
    static final int TODO_WITH_LIST = 101;
    static final int LIST = 200;
    private static final SQLiteQueryBuilder sToDoQueryBuilder;
    private static final SQLiteQueryBuilder sToDoByListQueryBuilder;

    static {
        sToDoQueryBuilder = new SQLiteQueryBuilder();
        sToDoQueryBuilder.setTables(ToDoContract.ToDoEntry.TABLE_NAME);
    }

    //  Create a query builder
    static {
        sToDoByListQueryBuilder = new SQLiteQueryBuilder();
        sToDoByListQueryBuilder.setTables(
                ToDoContract.ToDoEntry.TABLE_NAME + " INNER JOIN " +
                        ToDoContract.ToDoList.TABLE_NAME +
                        " ON " + ToDoContract.ToDoEntry.TABLE_NAME +
                        "." + ToDoContract.ToDoEntry.COLUMN_LIST_ID +
                        " = " + ToDoContract.ToDoList.TABLE_NAME +
                        "." + ToDoContract.ToDoList._ID);
    }

    private static final String sToDoListSelection =
            ToDoContract.ToDoList.TABLE_NAME +
                    "." + ToDoContract.ToDoList.COLUMN_NAME + " = ? ";

    @Override
    public boolean onCreate() {
        Log.d("sql", "creating tododbhelper");
        mOpenHelper = new ToDoDbHelper(getContext());
        return true;
    }


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ToDoContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, ToDoContract.PATH_TODO, TODO);
        matcher.addURI(authority, ToDoContract.PATH_TODO + "/*", TODO_WITH_LIST);
        matcher.addURI(authority, ToDoContract.PATH_LIST, LIST);
        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case TODO:
                return ToDoContract.ToDoEntry.CONTENT_TYPE;
            case LIST:
                return ToDoContract.ToDoList.CONTENT_TYPE;
            case TODO_WITH_LIST:
                return ToDoContract.ToDoEntry.CONTENT_TYPE;
            default:
                return ToDoContract.ToDoEntry.CONTENT_TYPE;
        }
    }

    //  This is a custom function for getting an item by its description as well as the list ID
    private Cursor getToDoByList(Uri uri, String[] projection, String sortOrder) {
        String listName = ToDoContract.ToDoEntry.getListFromUri(uri);
        String[] selectionArgs = new String[]{listName};
        String selection = sToDoListSelection;
        return sToDoByListQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    //  Standard query
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case TODO_WITH_LIST:
                retCursor = getToDoByList(uri, projection, sortOrder);
                break;
            case TODO:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ToDoContract.ToDoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case LIST:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ToDoContract.ToDoList.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ToDoContract.ToDoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case TODO: {
                long _id = db.insert(ToDoContract.ToDoEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    returnUri = ToDoContract.ToDoEntry.buildTodoUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case LIST: {
                long _id = db.insert(ToDoContract.ToDoList.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ToDoContract.ToDoList.buildListUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch(match) {
            case TODO:
                rowsDeleted = db.delete(ToDoContract.ToDoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LIST:
                rowsDeleted = db.delete(ToDoContract.ToDoList.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            Log.d("Delete", "Rows deleted");
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case TODO:
                rowsUpdated = db.update(ToDoContract.ToDoEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LIST:
                rowsUpdated = db.update(ToDoContract.ToDoList.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);

        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    //  Pretty sure I never bulk insert any items, but it doesn't hurt I guess...
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case TODO:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ToDoContract.ToDoEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}