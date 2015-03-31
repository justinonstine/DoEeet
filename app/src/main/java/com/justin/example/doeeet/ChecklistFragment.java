package com.justin.example.doeeet;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MotionEventCompat;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.justin.example.doeeet.data.ToDoContract;

/**
 * Checklist fragment. This represents a single to-do list...
 */
public class ChecklistFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView mListView;
    ChecklistAdapter mChecklistAdapter;
    Uri mListUri;

    public static final String CHECKLIST_URI = "uri";

    public static final int LOADER_ID = 100;

    //  Define our SQLite projection for a to-do list entry (details was never implemented)
    public static final String[] TODO_COLUMNS = {
            ToDoContract.ToDoEntry.TABLE_NAME + "." + ToDoContract.ToDoEntry._ID,
            ToDoContract.ToDoEntry.COLUMN_SUMMARY,
            ToDoContract.ToDoEntry.COLUMN_DETAILS,
            ToDoContract.ToDoEntry.COLUMN_COMPLETE
    };

    static final int COL_ID = 0;
    static final int COL_TODO_SUMMARY = 1;
    static final int COL_TODO_DETAILS = 2;
    static final int COL_TODO_COMPLETE = 3;

    public ChecklistFragment() {
        super();
        setHasOptionsMenu(true);
    }

    //  Loader stuff
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (mListUri != null) {
            CursorLoader cur = new CursorLoader(getActivity(), mListUri, TODO_COLUMNS,
                    null, null, null);
            return cur;
        }
        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mChecklistAdapter.swapCursor(cursor);
    }

    public void onLoaderReset(Loader<Cursor> loader) { mChecklistAdapter.swapCursor(null); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_checklist, container, false);
        mListView = (ListView) rootView.findViewById(R.id.checklist_listview);

        //  Add a swipe listener so that users can fling their tasks away when they're done...
        final GestureDetector gesture = new GestureDetector(getActivity(), new SwipeListener());
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean retval = gesture.onTouchEvent(event);

                // snap the view back to its original place if it wasn't actually swiped
                if (v != null) {
                    int action = MotionEventCompat.getActionMasked(event);
                    if (action == MotionEvent.ACTION_UP) {
                        float yPos = event.getY();
                        int adapterIndex = mListView.pointToPosition((int)event.getX(), (int)yPos);
                        int firstPos = mListView.getFirstVisiblePosition();
                        int viewIndex = adapterIndex - firstPos;
                        View movingView = mListView.getChildAt(viewIndex);
                        if (movingView == null) {
                            return retval;
                        }
                        movingView.setTranslationX(0);
                    }
                }
                return retval;
            }
        });
        Bundle arguments = getArguments();
        if (arguments != null) {
            mListUri = arguments.getParcelable(ChecklistFragment.CHECKLIST_URI);
        }

        mChecklistAdapter = new ChecklistAdapter(getActivity(), null, 0);
        mListView.setAdapter(mChecklistAdapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.checklistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_item) {
            createNewItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //  Create a dialog box to add a new task to the list. Once the user hits the Ok
    //  button, then insert the item into the database.
    public void createNewItem() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("What're you gonna do now, Napoleon?");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskText = input.getText().toString();
                float listId = -1;
                if (mListUri != null) {
                    String listName = ToDoContract.ToDoEntry.getListFromUri(mListUri);
                    listId = Utility.getListIdFromName(getActivity(), listName);
                }

                ContentValues values = new ContentValues();
                values.put(ToDoContract.ToDoEntry.COLUMN_COMPLETE, 0);
                values.put(ToDoContract.ToDoEntry.COLUMN_SUMMARY, taskText);
                values.put(ToDoContract.ToDoEntry.COLUMN_LIST_ID, listId);
                values.put(ToDoContract.ToDoEntry.COLUMN_DETAILS,
                        ToDoContract.ToDoEntry.TEMP_DETAIL_TEXT);
                getActivity().getContentResolver().insert(
                        ToDoContract.ToDoEntry.CONTENT_URI, values);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
    }

    //  Based on the swipe position, figure out which item in the listAdapter was swiped
    //  and delete it from the database...
//    public void deleteItem(MotionEvent event) {
//        float xPos = event.getX();
//        float yPos = event.getY();
//        int adapterIndex = mListView.pointToPosition((int)xPos, (int)yPos);
//        int firstPos = mListView.getFirstVisiblePosition();
//        int viewIndex = adapterIndex - firstPos;
//        View v = mListView.getChildAt(viewIndex);
//        TextView tv = (TextView) v.findViewById(R.id.checked_text_view);
//        String listItem = tv.getText().toString();
//        String listName = ToDoContract.ToDoEntry.getListFromUri(mListUri);
//
//        long listId = Utility.getListIdFromName(getActivity(), listName);
//
//        getActivity().getContentResolver().delete(ToDoContract.ToDoEntry.CONTENT_URI,
//                ToDoContract.ToDoEntry.COLUMN_SUMMARY + " = ? AND " +
//                ToDoContract.ToDoEntry.COLUMN_LIST_ID + " = ?",
//                new String[]{listItem, Long.toString(listId)});
//        getLoaderManager().restartLoader(LOADER_ID, null, this);
//    }

    //  I found an easier way to implement the swipe delete
    public void deleteItem(View v) {
        TextView tv = (TextView) v.findViewById(R.id.checked_text_view);
        String listItem = tv.getText().toString();
        String listName = ToDoContract.ToDoEntry.getListFromUri(mListUri);

        long listId = Utility.getListIdFromName(getActivity(), listName);

        getActivity().getContentResolver().delete(ToDoContract.ToDoEntry.CONTENT_URI,
                ToDoContract.ToDoEntry.COLUMN_SUMMARY + " = ? AND " +
                ToDoContract.ToDoEntry.COLUMN_LIST_ID + " = ?",
                new String[]{listItem, Long.toString(listId)});
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        getLoaderManager().initLoader(ChecklistFragment.LOADER_ID, null, this);
        super.onActivityCreated(bundle);
    }

    public String getListName() {
        if (mListUri == null) {
            return null;
        } else {
            return ToDoContract.ToDoEntry.getListFromUri(mListUri);
        }
    }


    //  This is my home rolled fling listener
    class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";
        View mMovingView;
        float mPositionX = 0;
        float mFlingSpeed = 0;
        boolean firstScrollValue = true;
        private static final long SCROLL_THRESHOLD = 35;
        private static final long FLING_DURATION = 500;
        private static final long REDRAW_DELAY = FLING_DURATION / 60;

        //  This must return true or else the fling will never be considered
        @Override
        public boolean onDown(MotionEvent event) {
            mPositionX = 0;
            float yPos = event.getY();
            int adapterIndex = mListView.pointToPosition((int)mPositionX, (int)yPos);
            int firstPos = mListView.getFirstVisiblePosition();
            int viewIndex = adapterIndex - firstPos;
            mMovingView = mListView.getChildAt(viewIndex);
            firstScrollValue = true;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX, float distY) {
            Log.d("scroll", "scroll?");
            mPositionX -= distX;
            if (mMovingView != null) {
                mMovingView.setTranslationX(mPositionX);
                mFlingSpeed = distX;
                if (Math.abs(distX) > SCROLL_THRESHOLD && !firstScrollValue) {
                    flingAway();
                }
                firstScrollValue = false;
                return true;
            }
            return true;
        }

        //  Throw away the current item if it is flinged fast enough
        private void flingAway() {
            long flingCounter = 0;
            while(flingCounter < FLING_DURATION) {
                mMovingView.setTranslationX(mPositionX);
                mPositionX -= mFlingSpeed / 4;
                mMovingView.setAlpha(1-(flingCounter/FLING_DURATION));
                flingCounter += REDRAW_DELAY;
                try {
                    wait(REDRAW_DELAY);
                } catch (Exception e) {
                    continue;
                }
            }
            deleteItem(mMovingView);
            mMovingView = null;
        }

        //  I saved this as an example for myself later on...
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

//            final int SWIPE_MIN_DISTANCE = 150;
//            final int SWIPE_MAX_OFF_PATH = 250;
//            final int SWIPE_THRESHOLD_VELOCITY = 200;
//            try {
//                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
//                    return false;
//                }
//                if (Math.abs(event1.getX() - event2.getX()) > SWIPE_MIN_DISTANCE &&
//                        Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    deleteItem(event1);
//                    return true;
//                }
//            } catch (Exception e) {
//
//            }

            return super.onFling(event1, event2, velocityX, velocityY);
        }
    }
}
