package com.justin.example.doeeet.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 *  Database contract for the check list and the list of lists
 */
public class ToDoContract {
    public static final String CONTENT_AUTHORITY = "com.justin.example.doeeet";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TODO = "todo";
    public static final String PATH_LIST = "todo_list";

    public static final class ToDoList implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIST).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LIST;
        public static final String TABLE_NAME = "todo_list";
        public static final String COLUMN_NAME = "name";

        public static Uri buildListUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class ToDoEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TODO).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TODO;
        public static final String TABLE_NAME = "todo";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_COMPLETE = "complete";
        public static final String COLUMN_LIST_ID = "list_id";

        public static final String TEMP_DETAIL_TEXT = "Not Yet Implemented";

        public static Uri buildTodoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildToDoList(String list) {
            return CONTENT_URI.buildUpon().appendPath(list).build();
        }

        public static String getListFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

}
