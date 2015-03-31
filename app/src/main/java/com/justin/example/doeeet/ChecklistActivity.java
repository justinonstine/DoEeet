package com.justin.example.doeeet;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.justin.example.doeeet.data.ToDoContract;

/**
 *  Activity to handle a checklist. It will pass along pertinent info to the fragment
 */
public class ChecklistActivity extends ActionBarActivity {

    public static final String FRAGMENT_TAG = "checklist_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            setTitle(ToDoContract.ToDoEntry.getListFromUri(getIntent().getData()));
            arguments.putParcelable(ChecklistFragment.CHECKLIST_URI, getIntent().getData());
            ChecklistFragment fragment = new ChecklistFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.todo_container, fragment, FRAGMENT_TAG)
                    .commit();
        }
    }

    // This method is called by a button at the bottom
    public void deleteChecked(View view) {
        ChecklistFragment cf = (ChecklistFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG);
        String listName = cf.getListName();
        long listId = Utility.getListIdFromName(this, listName);

        getContentResolver().delete(ToDoContract.ToDoEntry.CONTENT_URI,
            ToDoContract.ToDoEntry.COLUMN_LIST_ID + " = ? AND " +
            ToDoContract.ToDoEntry.COLUMN_COMPLETE + " = ?",
                        new String[]{Long.toString(listId), Long.toString(1)});
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.checklistfragment, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            Intent settingsIntent = new Intent(this, SettingsActivity.class);
//            startActivity(settingsIntent);
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    //  If a checkbox is ticked, update that item as complete
    public void toggleItem(View view) {
        CheckBox cb = (CheckBox) view;
        // Since the checkbox and textview are different things, grab them through the parent
        // view which is the linear layout
        LinearLayout ll = (LinearLayout) view.getParent();
        TextView tv = (TextView) ll.findViewById(R.id.checked_text_view);
        String listItem = tv.getText().toString();

        ContentValues cv = new ContentValues();
        cv.put(ToDoContract.ToDoEntry.COLUMN_COMPLETE, cb.isChecked());
        cv.put(ToDoContract.ToDoEntry.COLUMN_SUMMARY, listItem);
        cv.put(ToDoContract.ToDoEntry.COLUMN_DETAILS,
                ToDoContract.ToDoEntry.TEMP_DETAIL_TEXT);
        getContentResolver().update(ToDoContract.ToDoEntry.CONTENT_URI,
                cv, ToDoContract.ToDoEntry.COLUMN_SUMMARY + " = ? ", new String[] {listItem});
    }
}
