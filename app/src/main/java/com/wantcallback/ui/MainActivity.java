package com.wantcallback.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.reminder.ReminderUtil;
import com.wantcallback.ui.actionbar.AppEnableActionProvider;
import com.wantcallback.ui.anim.AnimationFade;
import com.wantcallback.ui.preferences.PreferenceActivity;
import com.wantcallback.ui.recycler.ItemTouchCallback;
import com.wantcallback.ui.recycler.ReminderRecycleAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AppEnableActionProvider.ToggleListener,
                    ReminderRecycleAdapter.OnItemActionListener {

    private static final String TASK_MUTE_REMINDERS = "task_mute_reminders";
    private static final String TASK_RECREATE_ACTUAL_REMINDERS = "task_recreate_actual_reminders";

    private MainActivity that;
    private FloatingActionButton btnAddAlarm;
    private RecyclerView recyclerList;
    private ReminderRecycleAdapter recycleAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private View viewAppDisabled;

    private ReminderDao reminderDao;
    private List<ReminderInfo> remindersList = new ArrayList<>();
    private RemindersBroadcastReceiver remindersBroadcastReceiver;
    private ItemTouchHelper mItemTouchHelper;

    /*private int i = 0;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        that = this;
        PreferenceManager.setDefaultValues(that, R.xml.pref, false);

        btnAddAlarm = (android.support.design.widget.FloatingActionButton) findViewById(R.id.btnAddAlarm);
        reminderDao = new ReminderDao(that);

        recyclerList = (RecyclerView) findViewById(R.id.listReminders);

        recyclerList.setHasFixedSize(true);

        recyclerList.setLayoutManager(new LinearLayoutManager(that));

        remindersList = reminderDao.getAll();
        recycleAdapter = new ReminderRecycleAdapter(that, remindersList, that);
        recyclerList.setAdapter(recycleAdapter);
        emptyView = findViewById(R.id.emptyReminderList);
        viewAppDisabled = findViewById(R.id.viewAppDisabledOverlay);

        ItemTouchHelper.Callback callback = new ItemTouchCallback(recycleAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerList);

        recyclerList.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                if (emptyView.getVisibility() == View.VISIBLE) {
                    animateFade(emptyView, false);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                if (remindersList.size() == 0 && AppHelper.isApplicationEnabled(that)) {
                    animateFade(emptyView, true);
                }
            }
        });

        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditReminderActivity.class);
                intent.putExtra(EditReminderActivity.EXTRA_MODE, EditReminderActivity.MODE.BLANK.toString());
                startActivity(intent);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_one, R.color.refresh_two, R.color.refresh_three);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadRemindersList(true);
            }
        });

        // place this after Reminders List view is created and filled
        remindersBroadcastReceiver = new RemindersBroadcastReceiver();
        registerReceiver(remindersBroadcastReceiver, new IntentFilter(RemindersBroadcastReceiver.ACTION_ANY));

        if (AppHelper.isApplicationEnabled(that)) {
            displayMainLayout(true, false);
        } else {
            displayMainLayout(false, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        reloadRemindersList(false);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(remindersBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        AppEnableActionProvider provider = (AppEnableActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_app_enable));
        provider.addToggleListener(that);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(that, PreferenceActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStateChanged(boolean isChecked) {
        boolean initApp;

        if (isChecked) {
            initApp = true;
            displayMainLayout(true, true);

            BatchReminderOperationTask task = new BatchReminderOperationTask();
            task.execute(TASK_RECREATE_ACTUAL_REMINDERS);
        } else {
            initApp = false;
            displayMainLayout(false, true);

            BatchReminderOperationTask task = new BatchReminderOperationTask();
            task.execute(TASK_MUTE_REMINDERS);
        }

        startService(AppHelper.getInitServiceIntent(that, initApp));
    }

    private void displayMainLayout(boolean display, boolean animateAll) {
        if (display) {
            btnAddAlarm.show();
            if (animateAll) {
                animateFade(viewAppDisabled, false);
            }
            if (remindersList.size() == 0) {
                animateFade(emptyView, true);
            }
        } else {
            btnAddAlarm.hide();
            animateFade(viewAppDisabled, true);
            if (animateAll && remindersList.size() == 0) {
                animateFade(emptyView, false);
            }
        }
    }

    private void animateFade(View v, boolean in) {
        AnimationFade anim = new AnimationFade(v, in, 1.0f);
        anim.runAnimation();
    }

    private void reloadRemindersList(boolean showRefresh) {
        RefreshRemindersListTask task = new RefreshRemindersListTask();

        if (showRefresh && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        task.execute();
    }

    @Override
    public void onListItemClicked(long reminderId) {
        Intent intent = new Intent(that, EditReminderActivity.class);
        intent.putExtra(EditReminderActivity.EXTRA_REMINDER_ID, reminderId);
        intent.putExtra(EditReminderActivity.EXTRA_MODE, EditReminderActivity.MODE.EDIT.toString());
        startActivity(intent);
    }

    @Override
    public void onListItemDismissed(long id) {
        final ReminderInfo info = reminderDao.getById(id);
        ReminderUtil.cancelAndRemoveReminder(that, info);

        int position = recycleAdapter.getPositionById(id);
        remindersList.remove(position);
        recycleAdapter.notifyItemRemoved(position);

        Snackbar.make(recyclerList, R.string.snack_title, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ReminderUtil.createNewReminder(that, info.copyToNew());
                        reloadRemindersList(false);
                    }
                })
                .show();
    }

    public class RemindersBroadcastReceiver extends BroadcastReceiver {
        public static final String ACTION_ANY = "action_any";

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();

            if (ACTION_ANY.equals(action)) {
                that.reloadRemindersList(false);
            }
        }
    }

    public class RefreshRemindersListTask extends AsyncTask<Void, Void, List<ReminderInfo>> {
        @Override
        protected List<ReminderInfo> doInBackground(Void... params) {
            return reminderDao.getAll();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(List<ReminderInfo> reminders) {
            if (reminders != null) {
                remindersList.clear();
                remindersList.addAll(reminders);
                recycleAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public class BatchReminderOperationTask extends AsyncTask<String, Void, Void> {

        private String task;

        @Override
        protected Void doInBackground(String... params) {
            task = params[0];
            if (TASK_MUTE_REMINDERS.equals(task)) {
                ReminderUtil.muteAllReminders(that);
            } else if (TASK_RECREATE_ACTUAL_REMINDERS.equals(task)) {
                ReminderUtil.recreateActualReminders(that);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (TASK_RECREATE_ACTUAL_REMINDERS.equals(task)) {
                that.reloadRemindersList(false);
            }
        }
    }
}
