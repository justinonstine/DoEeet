package com.justin.example.doeeet;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.justin.example.doeeet.data.ToDoContract;

/*
 *  Fragment for handling a list of to-do tasks.
 */
public class ToDoListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    ListView mListView;
    ToDoListAdapter mAdapter;
    public static final int LOADER_ID = 200;

    // Declare the SQLite projection for a task list
    public static final String[] LIST_COLUMNS = {
        ToDoContract.ToDoList.TABLE_NAME + "." + ToDoContract.ToDoList._ID,
        ToDoContract.ToDoList.COLUMN_NAME
    };

    public static final int COL_LIST_ID = 0;
    public static final int COL_LIST_NAME = 1;

    public ToDoListFragment() {
        super();
        setHasOptionsMenu(true);
    }

    //  Implement the loader methods for the cursor adapter
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = ToDoContract.ToDoList._ID + " ASC";
        Uri toDoUri = ToDoContract.ToDoList.CONTENT_URI;
        return new CursorLoader(getActivity(), toDoUri, LIST_COLUMNS, null, null,
                sortOrder);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.swapCursor(null); }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.main_listview);

        //  Create a header for the list of lists for user clarity
        TextView headerText = new TextView(getActivity());
        headerText.setText("To-Do Lists");
        headerText.setTextSize(24);
        mListView.addHeaderView(headerText);

        //  Create a gesture detector to handle single taps (launch the checklist activity)
        //  and long presses (context menu to delete a checklist)
        final GestureDetector gesture = new GestureDetector(getActivity(), new TouchListener());
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gesture.onTouchEvent(event);
                return false;
            }
        });
        registerForContextMenu(mListView);

        mAdapter = new ToDoListAdapter(getActivity(), null, 0);
        mListView.setAdapter(mAdapter);

        return rootView;
    }

    //  Create the context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    //  Delete a checklist when the context menu item has been selected (there's only one atm)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)
                item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.context_item_delete:
                deleteList(info.targetView);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.checklistfragment, menu);
    }

    //  Create a new list when the user hits the '+' button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_item) {
            createNewItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //  Display an alert dialog to capture the title of the new to-do list
    public void createNewItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("What list do you want to make?");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        //  Once the user hits 'ok', add a new entry into our database
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String listText = input.getText().toString();
                ContentValues values = new ContentValues();
                values.put(ToDoContract.ToDoList.COLUMN_NAME, listText);
                getActivity().getContentResolver().insert(
                        ToDoContract.ToDoList.CONTENT_URI, values);
            }
        });

        //  Don't do anything when 'Cancel' is selected
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //  Pop up the dialog window and activate the soft keyboard
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(bundle);
    }

    //  Delete an entire to-do list from the database
    public void deleteList(View v) {
        TextView tv = (TextView) v.findViewById(R.id.list_item_textview);
        String listName = tv.getText().toString();
        Cursor cursor = getActivity().getContentResolver().query(
                ToDoContract.ToDoList.CONTENT_URI, ToDoListFragment.LIST_COLUMNS,
                ToDoContract.ToDoList.COLUMN_NAME + " = ? ",
                new String[]{listName},
                null
        );
        cursor.moveToFirst();
        long listId = cursor.getLong(ToDoListFragment.COL_LIST_ID);

        //  First delete all the entries in the list to prevent any orphans
        getActivity().getContentResolver().delete(ToDoContract.ToDoEntry.CONTENT_URI,
                ToDoContract.ToDoEntry.COLUMN_LIST_ID + " = ? ",
                new String[]{Long.toString(listId)});

        //  Now delete the list itself...
        getActivity().getContentResolver().delete(ToDoContract.ToDoList.CONTENT_URI,
                ToDoContract.ToDoList.COLUMN_NAME + " = ? ",
                new String[]{listName});

        getLoaderManager().restartLoader(LOADER_ID, null, this);
        cursor.close();

    }

    //  Create a listener class to handle touch gestures
    class TouchListener extends GestureDetector.SimpleOnGestureListener {

        //  This must return true or else the fling will never be considered
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        //  Single tap means the user would like to see the items in a list
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            float xPos = event.getX();
            float yPos = event.getY();
            int adapterIndex = mListView.pointToPosition((int) xPos, (int) yPos);
            int firstPos = mListView.getFirstVisiblePosition();
            int viewIndex = adapterIndex - firstPos;
            View v = mListView.getChildAt(viewIndex);
            TextView tv = (TextView) v.findViewById(R.id.list_item_textview);

            //  If someone pokes the listview header, don't do anything...
            if (tv == null) {
                return false;
            }

            Intent intent = new Intent(getActivity(), ChecklistActivity.class)
                    .setData(ToDoContract.ToDoEntry.buildToDoList(tv.getText().toString()));
            startActivity(intent);
            return true;
        }

        //  Long press activates the context menu
        @Override
        public void onLongPress(MotionEvent event) {
            super.onLongPress(event);
            getActivity().openContextMenu(mListView);
        }
    }
}