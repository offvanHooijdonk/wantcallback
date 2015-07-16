package com.wantcallback.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.wantcallback.Constants;
import com.wantcallback.observer.CallLogObserver;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.observer.listener.impl.StandardMissRejectListener;

@Deprecated
public class CallHandlerService extends Service {

	private CallLogObserver callLogObserver;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(Constants.LOG_TAG, "Service created.");

		// initPhoneListener();

		initCallObserver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(Constants.LOG_TAG, "onStartCommand called");

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.i(Constants.LOG_TAG, "Service is being destroyed!");
		super.onDestroy();
	}

	private void initCallObserver() {
		OnCallMissRejectListener listener = new StandardMissRejectListener(this);
		
		callLogObserver = new CallLogObserver(new Handler(), this);
		callLogObserver.addListener(listener);
		this.getApplicationContext().getContentResolver()
				.registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, callLogObserver);

		Log.i(Constants.LOG_TAG, "Call observer initialized.");
	}
}
