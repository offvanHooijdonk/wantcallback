package com.wantcallback.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.wantcallback.Constants;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.notifications.NotificationActionBroadcastReceiver;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.CallLogObserver;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.observer.listener.impl.StandardMissRejectListener;

public class InitializerIntentService extends Service {
	public static final String EXTRA_START_SHUT = "extra_start_shut";

	private static final String DEBUG_SERVICE_NAME = "InitializerIntentService";

	private static CallLogObserver callLogObserver = null;
	private static NotificationActionBroadcastReceiver receiver = null;
	private static IntentFilter filter;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(Constants.LOG_TAG, "Service created.");

		if (AppHelper.isApplicationEnabled(this)) {
			registerAll();
		}

		NotificationsUtil notificationsUtil = new NotificationsUtil(this);
		startForeground(NotificationsUtil.NOTIFICATION_FOREGROUND_SERVICE, notificationsUtil.createForegroundServiceNotification());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean isStart = intent.getBooleanExtra(EXTRA_START_SHUT, true);

		if (isStart) {
			registerAll();
			AppHelper.persistAppEnabledState(this, true);
		} else {
			unregisterAll();
			AppHelper.persistAppEnabledState(this, false);

			this.stopSelf();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.i(Constants.LOG_TAG, "Service is being destroyed!");
		unregisterAll();
		super.onDestroy();
	}

	private void registerAll() {
		registerReceiver();
		registerCallLogObserver();
	}

	private void registerCallLogObserver() {
		if (callLogObserver == null) {
			OnCallMissRejectListener listener = new StandardMissRejectListener(this);

			callLogObserver = new CallLogObserver(new Handler(), this);
			callLogObserver.addListener(listener);

			Log.i(Constants.LOG_TAG, "Call observer initialized.");
		}

		this.getApplicationContext().getContentResolver()
				.registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, callLogObserver);
		Log.i(Constants.LOG_TAG, "Call observer registered.");
	}

	private void registerReceiver() {
		if (receiver == null) {
			receiver = new NotificationActionBroadcastReceiver();
			filter = new IntentFilter();
			filter.addAction(NotificationActionBroadcastReceiver.ACTION_FORGET);
		}

		this.registerReceiver(receiver, filter);
	}

	private void unregisterAll() {
		unregisterReceiver();
		unregisterCallLogObserver();
	}

	private void unregisterCallLogObserver() {
		if (callLogObserver != null) {
			this.getApplicationContext().getContentResolver()
					.unregisterContentObserver(callLogObserver);
			callLogObserver = null;
		}
	}

	private void unregisterReceiver() {
		if (receiver != null) {
			this.unregisterReceiver(receiver);
			receiver = null;
		}
	}
}