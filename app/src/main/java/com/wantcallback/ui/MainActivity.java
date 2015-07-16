package com.wantcallback.ui;

import android.app.Activity;
import android.content.Intent;
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
import com.wantcallback.service.InitializerIntentService;
import com.wantcallback.ui.actionbar.AppEnableActionProvider;

import java.util.List;

public class MainActivity extends Activity implements AppEnableActionProvider.ToggleListener, ReminderMainAdapter.ReminderInteractionListener {
	
	private MainActivity that;
	private Button btnAddAlarm;
	private ListView listReminders;

	private ReminderDao reminderDao;
	private List<ReminderInfo> remindersList;
	/*private CallLogObserver callLogObserver;
	private NotificationActionBroadcastReceiver broadcastReceiver;*/

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
		/*int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}*/
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStateChanged(boolean isChecked) {
		boolean initApp;
		if (isChecked) {
			initApp = true;
			displayMainLayout(true);
		} else {
			initApp = false;
			displayMainLayout(false);
		}
		Intent intent = new Intent(that, InitializerIntentService.class);
		intent.putExtra(InitializerIntentService.EXTRA_START_SHUT, initApp);
		startService(intent);
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
		remindersList.clear();
		remindersList.addAll(reminderDao.getAll());

		((ReminderMainAdapter) listReminders.getAdapter()).notifyDataSetChanged();
	}
	/*private void initBroadcastReceiver() {
		broadcastReceiver = new NotificationActionBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(NotificationActionBroadcastReceiver.ACTION_FORGET);
		
		registerReceiver(broadcastReceiver, filter);
	}
	
	private void initCallObserver() {
		OnCallMissRejectListener listener = new StandardMissRejectListener(that);
		
		callLogObserver = new CallLogObserver(new Handler(), that);
		callLogObserver.addListener(listener);
		
		that.getApplicationContext().getContentResolver()
				.registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, callLogObserver);

		Log.i(Constants.LOG_TAG, "Call observer initialized.");
	}*/
	
	/*@Override
	protected void onDestroy() {
		super.onDestroy();
		if (callLogObserver != null) {
			that.getApplicationContext().getContentResolver()
					.unregisterContentObserver(callLogObserver);
		}
		
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
	}*/
}
