package com.wantcallback.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.reminder.ReminderUtil;
import com.wantcallback.ui.actionbar.AppEnableActionProvider;
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
    private RecyclerView listReminders;
    private ReminderRecycleAdapter recycleAdapter;

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

        btnAddAlarm = (FloatingActionButton) findViewById(R.id.btnAddAlarm);
        reminderDao = new ReminderDao(that);

        if (AppHelper.isApplicationEnabled(that)) {
            displayMainLayout(true);
        } else {
            displayMainLayout(false);
        }

        listReminders = (RecyclerView) findViewById(R.id.listReminders);

        //TODO add empty view

        listReminders.setHasFixedSize(true);

        listReminders.setLayoutManager(new LinearLayoutManager(that));

        recycleAdapter = new ReminderRecycleAdapter(that, remindersList, that);
        listReminders.setAdapter(recycleAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchCallback(recycleAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(listReminders);

        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditReminderActivity.class);
                intent.putExtra(EditReminderActivity.EXTRA_MODE, EditReminderActivity.MODE.BLANK.toString());
                startActivity(intent);
            }
        });
        btnAddAlarm.attachToRecyclerView(listReminders);

        // place this after Reminders List view is created and filled
        remindersBroadcastReceiver = new RemindersBroadcastReceiver();
        registerReceiver(remindersBroadcastReceiver, new IntentFilter(RemindersBroadcastReceiver.ACTION_ANY));
    }

    @Override
    protected void onResume() {
        super.onResume();

        reloadRemindersList();
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
        if (id == R.id.action_reload) {
            reloadRemindersList();
        } else if (id == R.id.action_settings) {
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
            displayMainLayout(true);

            BatchReminderOperationTask task = new BatchReminderOperationTask();
            task.execute(TASK_RECREATE_ACTUAL_REMINDERS);
        } else {
            initApp = false;
            displayMainLayout(false);

            BatchReminderOperationTask task = new BatchReminderOperationTask();
            task.execute(TASK_MUTE_REMINDERS);
        }

        startService(AppHelper.getInitServiceIntent(that, initApp));
    }

    private void displayMainLayout(boolean display) {
        if (display) {
            btnAddAlarm.setEnabled(true);
        } else {
            btnAddAlarm.setEnabled(false);
        }
    }

    /*@Override
    public void onDeleteReminder(final ReminderInfo info) {
        // TODO make this cancelable
        AlertDialog.Builder dialog = new AlertDialog.Builder(that).
                setTitle(R.string.confirm_delete_title).
                setMessage(MessageFormat.format(that.getString(R.string.confirm_delete_title), info.getPhone())).
                setCancelable(true).
                setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reminderDao.deleteByPhone(info.getPhone());
                        reloadRemindersList();
                        dialog.dismiss();
                    }
                }).
                setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog.show();
    }*/

    private void reloadRemindersList() {
        RefreshRemindersListTask task = new RefreshRemindersListTask();

        task.execute();
    }

    @Override
    public void onItemClicked(View v) {
        int position = listReminders.indexOfChild(v);
        ReminderInfo reminder = remindersList.get(position);

        Intent intent = new Intent(that, EditReminderActivity.class);
        intent.putExtra(EditReminderActivity.EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(EditReminderActivity.EXTRA_MODE, EditReminderActivity.MODE.EDIT.toString());
        startActivity(intent);
    }

    @Override
    public void onItemDismissed(int position) {
        ReminderInfo info = remindersList.get(position);
        reminderDao.deleteByPhone(info.getPhone());
        reloadRemindersList();
    }

    public class RemindersBroadcastReceiver extends BroadcastReceiver {
        public static final String ACTION_ANY = "action_any";

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();

            if (ACTION_ANY.equals(action)) {
                that.reloadRemindersList();
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

                // test
                /*if (i == 0) {
                    ReminderInfo info = new ReminderInfo();
                    info.setId(456);
                    info.setDate(new Date().getTime());
                    info.setPhone("+375447778899");
                    info.setCallInfo(new CallInfo(456, "+375447778899", new Date().getTime(), CallInfo.TYPE.MISSED));
                    remindersList.add(info);
                }
                i++;*/
                recycleAdapter.notifyDataSetChanged();
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
                that.reloadRemindersList();
            }
        }
    }
}
