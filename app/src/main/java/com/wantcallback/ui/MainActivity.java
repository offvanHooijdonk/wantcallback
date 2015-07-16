package com.wantcallback.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.wantcallback.R;

public class MainActivity extends Activity {
	
	private MainActivity that;
	private Button btnAddAlarm;
	/*private CallLogObserver callLogObserver;
	private NotificationActionBroadcastReceiver broadcastReceiver;*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		that = this;
		
		btnAddAlarm = (Button) findViewById(R.id.btnAddAlarm);
		
		/*initCallObserver();
		initBroadcastReceiver();*/
		
		btnAddAlarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SetReminderActivity.class);
				intent.putExtra(SetReminderActivity.EXTRA_PHONE, "+375447897897");
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
