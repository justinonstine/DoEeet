package com.justin.example.doeeet;

import android.content.Context;
import android.database.Cursor;

import com.justin.example.doeeet.data.ToDoContract;

/**
 *  Just one utility function at the moment to handle getting the list ID from its name
 */
public class Utility {
    public static long getListIdFromName(Context context, String listName) {
        Cursor cursor = context.getContentResolver().query(
                ToDoContract.ToDoList.CONTENT_URI,
                ToDoListFragment.LIST_COLUMNS,
                ToDoContract.ToDoList.COLUMN_NAME + " = ? ",
                new String[]{listName},
                null
        );
        cursor.moveToFirst();
        long listId = cursor.getLong(ToDoListFragment.COL_LIST_ID);
        cursor.close();
        return(listId);
    }
}
