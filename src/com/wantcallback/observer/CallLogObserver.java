package com.wantcallback.observer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

import com.wantcallback.Constants;
import com.wantcallback.data.CallLogUtil;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.observer.model.CallInfo;
import com.wantcallback.observer.model.CallInfo.TYPE;

public class CallLogObserver extends ContentObserver {

	private Context ctx;
	
	private static int lastCallId = 0;
	private static long startDate = (new Date()).getTime(); // in order not to handle calls that were before the app start
	
	private List<OnCallMissRejectListener> listeners = new ArrayList<OnCallMissRejectListener>();
	
	private CallLogUtil callLogUtil;

	private CallLogObserver(Handler handler) {
		super(handler);
	}
	
	public CallLogObserver(Handler handler, Context context) {
		this(handler);
		this.ctx = context;
		
		callLogUtil = new CallLogUtil(ctx);
	}
	
	public void addListener(OnCallMissRejectListener l) {
		listeners.add(l);
	}

	@Override
	public void onChange(boolean selfChange) {
		Log.i(Constants.LOG_TAG, "OnChanged");
		
		List<CallInfo> callsAll = new ArrayList<CallInfo>();
		
		List<CallInfo> callsMissed = callLogUtil.getMissedCalls(lastCallId, startDate);
		List<CallInfo> callsRejected = callLogUtil.getRejectedCalls(lastCallId, startDate);
		callsAll.addAll(callsMissed);
		callsAll.addAll(callsRejected);

		updateLastId(callsAll);
		
		handleCalls(callsAll);
	}
	
	private void handleCalls(List<CallInfo> calls) {
		for (CallInfo info : calls) {
			onEvent(info);
		}
	}

	@Override
	public boolean deliverSelfNotifications() {
		return true;
	}
	
	private void onEvent(CallInfo info) {
		for (OnCallMissRejectListener l : listeners) {
			if (l != null) {
				if (info.getType() == TYPE.MISSED) {
					l.onCallMissed(info);
				} else if (info.getType() == TYPE.REJECTED) {
					l.onCallRejected(info);
				}
			}
		}
	}

	private void updateLastId(List<CallInfo> calls) {
		for (CallInfo info : calls) {
			lastCallId = (info.getLogId() > lastCallId) ? info.getLogId() : lastCallId;
		}
	}
}
