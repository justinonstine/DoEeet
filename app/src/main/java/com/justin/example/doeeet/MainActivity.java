package com.justin.example.doeeet;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

/**
 *  It isn't much, but this is the main activity.
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToDoListFragment lf = (ToDoListFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragment_list);
        getSupportActionBar().setIcon(R.drawable.ic_logo);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    public void onItemSelected(Uri listUri) {
//        Bundle arguments = new Bundle();
//        arguments.putParcelable(ChecklistFragment.CHECKLIST_URI, listUri);
//        ChecklistFragment fragment = new ChecklistFragment();
//        fragment.setArguments(arguments);
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fragment_list, fragment)
//                .commit();
//    }


}
