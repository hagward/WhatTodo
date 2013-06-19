package a.hagward.whattodo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * WhatTodo?!: a (very) simple todo list application.
 * @author Anders Hagward
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    static int STATUS_DUE = 0, STATUS_COMPLETED = 1;

    SectionsPagerAdapter mSectionsPagerAdapter;
    SwipelessViewPager mViewPager;
    DatabaseHandler dbHandler;
    TodoListAdapter mListAdapters[];

    // Holds the next unused id for creating new tasks.
    private long nextId;

    /**
     * Shows a dialog for creating a new task. It consists of an {@link EditText} and a positive and
     * a negative button.
     */
    private void showCreateTodoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new todo");
        final EditText input = new EditText(this);
        builder.setView(input)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Add a new item with the specified title.
                        String title = input.getText().toString();
                        Todo newTodo = new Todo(nextId++, System.currentTimeMillis()/1000L,
                                0, title);
                        dbHandler.addTodo(newTodo);
                        mListAdapters[STATUS_DUE].add(newTodo);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    /**
     * Inverts the 'completed' status, i.e. sets completed tasks as 'due' again and vice versa, of
     * an item in the current tab, and updates the list adapters and database accordingly.
     * @param position the position of the item in the list adapter
     */
    private void updateCompleted(int position) {
        try {
            // Move the item from 'due' to 'completed', or vice versa, and update the database
            // respectively.
            int statusView = mViewPager.getCurrentItem();
            int invStatus = (statusView + 1) % 2;

            Todo todo = mListAdapters[statusView].getItem(position);
            mListAdapters[statusView].remove(todo);
            todo.setCompleted(invStatus);
            dbHandler.updateTodo(todo);
            mListAdapters[invStatus].add(todo);
        } catch (Exception e) {
            Log.e("onListItemClick", "List index out of bounds: " + String.valueOf(position));
        }
    }

    /**
     * Deletes all completed tasks from the list adapter and the database.
     */
    private void clearCompleted() {
        for (int i = mListAdapters[STATUS_COMPLETED].getCount() - 1; i >= 0; i--) {
            Todo todo = mListAdapters[STATUS_COMPLETED].getItem(i);
            mListAdapters[STATUS_COMPLETED].remove(todo);
            dbHandler.deleteTodo(todo);
        }
    }

    /**
     * Sets the style of the {@link TextView} in v to be either R.style.TextDue or
     * R.style.TextCompleted, and makes the text 'strike through' if an item in the 'due' list was
     * set to 'completed'.
     * @param v a View object that is a row in the {@link ListFragment} and has a tag that is a
     *          TodoListAdapter.TodoHolder
     * @param completed {@code true} if the View object was marked as completed, {@code false}
     *                  otherwise
     */
    private void setListItemStyleCompleted(View v, boolean completed) {
        int statusView = mViewPager.getCurrentItem();
        TodoListAdapter.TodoHolder holder = (TodoListAdapter.TodoHolder) v.getTag();
        holder.checkBox.setChecked(statusView == STATUS_DUE);
        holder.txtTitle.setTextAppearance(v.getContext(),
                (completed && statusView == STATUS_DUE) ? R.style.TextCompleted : R.style.TextDue);
        if (completed && statusView == STATUS_DUE)
            holder.txtTitle.setPaintFlags(holder.txtTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            holder.txtTitle.setPaintFlags(holder.txtTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (SwipelessViewPager) findViewById(R.id.pager);
        mViewPager.setSwipeable(true);  // swipeable for now
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        // Load todos and the next id to be used (max(id) + 1).
        dbHandler = new DatabaseHandler(this);
        ArrayList<Todo> due = (ArrayList<Todo>) dbHandler.getAllTodos(STATUS_DUE);
        ArrayList<Todo> completed = (ArrayList<Todo>) dbHandler.getAllTodos(STATUS_COMPLETED);
        nextId = dbHandler.getNextId();

        mListAdapters = new TodoListAdapter[2];
        mListAdapters[STATUS_DUE] = new TodoListAdapter(this, R.layout.row, due, STATUS_DUE);
        mListAdapters[STATUS_COMPLETED] = new TodoListAdapter(this, R.layout.row, completed, STATUS_COMPLETED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newtodo:
                showCreateTodoDialog();
                break;
            case R.id.action_clear:
                // Switch the view to show the completed items before deleting.
                if (mViewPager.getCurrentItem() == STATUS_DUE)
                    mViewPager.setCurrentItem(STATUS_COMPLETED);
                clearCompleted();
                break;
        }

        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private static class TodoListAdapter extends ArrayAdapter<Todo> {
        private int mLayoutResourceId;
        private int mStatusView;

        public TodoListAdapter(Context context, int layoutResourceId, List<Todo> data, int statusView) {
            super(context, layoutResourceId, data);
            mLayoutResourceId = layoutResourceId;
            mStatusView = statusView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            TodoHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                row = inflater.inflate(mLayoutResourceId, parent, false);

                holder = new TodoHolder();
                holder.checkBox = (CheckBox) row.findViewById(R.id.checkBox);
                holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);

                row.setTag(holder);
            } else {
                holder = (TodoHolder) row.getTag();
            }

            Todo todo = getItem(position);
            holder.checkBox.setChecked(todo.getCompleted() == STATUS_COMPLETED);
            holder.txtTitle.setText(todo.getTitle());
            holder.txtTitle.setTextAppearance(getContext(),
                    mStatusView == STATUS_DUE ? R.style.TextDue : R.style.TextCompleted);

            return row;
        }

        static class TodoHolder {
            CheckBox checkBox;
            TextView txtTitle;
        }
    }

    /**
     * A {@link ListFragment} that have different behaviours for clicking 'due' items or 'completed'
     * items.
     */
    private class TodoListFragment extends ListFragment {
        private int mClickDelay = 1000;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onListItemClick(ListView l, final View v, final int position, long id) {
            setListItemStyleCompleted(v, true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateCompleted(position);
                    setListItemStyleCompleted(v, false);
                }
            }, mClickDelay);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            /*ListFragment fragment = new ListFragment();*/
            ListFragment fragment = new TodoListFragment();
            fragment.setListAdapter(mListAdapters[position]);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }
}
