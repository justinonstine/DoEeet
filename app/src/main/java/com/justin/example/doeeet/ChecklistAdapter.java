package com.justin.example.doeeet;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 *  Custom adapter for the checklist. It will read from the database whether items are marked
 *  complete or not and check them appropriately.
 */
public class ChecklistAdapter extends CursorAdapter {

    public ChecklistAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view.findViewById(R.id.checked_text_view);
        CheckBox cb = (CheckBox) view.findViewById(R.id.item_checkbox);

        int isComplete = cursor.getInt(ChecklistFragment.COL_TODO_COMPLETE);
        if (isComplete == 0) {
            cb.setChecked(false);
            tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        } else {
            cb.setChecked(true);
            tv.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
        tv.setText(cursor.getString(ChecklistFragment.COL_TODO_SUMMARY));
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_task, parent, false);
    }
}
