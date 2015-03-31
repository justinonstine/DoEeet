package com.justin.example.doeeet;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 *  To-Do list adapter to handle any custom stuff I might want to do
 */
public class ToDoListAdapter extends CursorAdapter {
    public ToDoListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view.findViewById(R.id.list_item_textview);
        tv.setText(cursor.getString(ToDoListFragment.COL_LIST_NAME));
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return  LayoutInflater.from(context).inflate(R.layout.list_item_list, parent, false);
    }
}
