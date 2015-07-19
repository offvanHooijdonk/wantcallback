package com.wantcallback.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.reminder.ReminderUtil;
import com.wantcallback.ui.actionbar.AppEnableActionProvider;

import java.util.List;

public class MainActivity extends Activity implements AppEnableActionProvider.ToggleListener, ReminderMainAdapter
        .ReminderInteractionListener {

    private static final String TASK_MUTE_REMINDERS = "task_mute_reminders";
    private static final String TASK_RECREATE_ACTUAL_REMINDERS = "task_recreate_actual_reminders";

    private MainActivity that;
    private Button btnAddAlarm;
    private ListView listReminders;

    private ReminderDao reminderDao;
    private List<ReminderInfo> remindersList;
    private RemindersBroadcastReceiver remindersBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        that = this;

        btnAddAlarm = (Button) findViewById(R.id.btnAddAlarm);
        reminderDao = new ReminderDao(that);

		/*initCallObserver();
        initBroadcastReceiver();*/

        if (AppHelper.isApplicationEnabled(that)) {
            displayMainLayout(true);
        } else {
            displayMainLayout(false);
        }

        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetReminderActivity.class);
                intent.putExtra(SetReminderActivity.EXTRA_PHONE, "+375447897897");
                startActivity(intent);
            }
        });

        listReminders = (ListView) findViewById(R.id.listReminders);
        remindersList = reminderDao.getAll();
        listReminders.setAdapter(new ReminderMainAdapter(that, remindersList, that));
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

        AppEnableActionProvider provider = (AppEnableActionProvider) menu.findItem(R.id.action_app_enable).getActionProvider();
        provider.addToggleListener(that);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            reloadRemindersList();
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

    @Override
    public void onDeleteReminder(ReminderInfo info) {
        // TODO make this cancelable
        reminderDao.deleteByPhone(info.getPhone());
        reloadRemindersList();
    }

    private void reloadRemindersList() {
        RefreshRemindersListTask task = new RefreshRemindersListTask();

        task.execute();
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
            return remindersList = reminderDao.getAll();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(List<ReminderInfo> reminders) {
            if (reminders != null) {
                remindersList.clear();
                remindersList.addAll(reminders);

                ((ReminderMainAdapter) listReminders.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    public class BatchReminderOperationTask extends  AsyncTask<String, Void, Void> {

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
