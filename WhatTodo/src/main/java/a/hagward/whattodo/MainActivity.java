package a.hagward.whattodo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    static int STATUS_DUE = 0, STATUS_COMPLETED = 1;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    ArrayAdapter<String>[] mArrayAdapters;
    DatabaseHandler dbHandler;

    ArrayList<Todo> dueList, completedList;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private long nextId;

    private void initAdapters() {
        mArrayAdapters = new ArrayAdapter[2];
        for (int i = 0; i < 2; i++)
            mArrayAdapters[i] = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1);

        for (Todo t : dueList)
            mArrayAdapters[STATUS_DUE].add(t.toString());
        for (Todo t : completedList)
            mArrayAdapters[STATUS_COMPLETED].add(t.toString());
    }

    private void showCreateTodoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create new todo");
        final EditText input = new EditText(this);
        builder.setView(input)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String title = input.getText().toString();
                        Todo newTodo = new Todo(nextId++, System.currentTimeMillis()/1000L,
                                0, title);
                        dbHandler.addTodo(newTodo);
                        dueList.add(newTodo);
                        mArrayAdapters[STATUS_DUE].add(title);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHandler = new DatabaseHandler(this);
        dueList = (ArrayList<Todo>) dbHandler.getAllTodos(STATUS_DUE);
        completedList = (ArrayList<Todo>) dbHandler.getAllTodos(STATUS_COMPLETED);
        nextId = dbHandler.getNextId();

        initAdapters();

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
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

    /**
     * A {@link ListFragment} that have different behaviours for clicking 'due' items or 'completed'
     * items.
     */
    private class TodoListFragment extends ListFragment {
        private int mStatusView = STATUS_DUE;

        public TodoListFragment(int statusView) {
            mStatusView = statusView;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            if (mStatusView == STATUS_DUE) {
                try {
                    Todo todo = dueList.remove(position);
                    todo.setCompleted(STATUS_COMPLETED);
                    dbHandler.updateTodo(todo);
                    completedList.add(todo);
                    mArrayAdapters[STATUS_DUE].remove(todo.getTitle());
                    mArrayAdapters[STATUS_COMPLETED].add(todo.getTitle());
                } catch (Exception e) {
                    Log.e("onListItemClick", "List index out of bounds");
                }
            }
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
            ListFragment fragment = new TodoListFragment(position);
            fragment.setListAdapter(mArrayAdapters[position]);
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
