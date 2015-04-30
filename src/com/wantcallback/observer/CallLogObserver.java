package com.wantcallback.observer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.Log;

import com.wantcallback.Constants;
import com.wantcallback.observer.CallInfo.TYPE;

public class CallLogObserver extends ContentObserver {

	private Context ctx;
	private Handler handler;
	
	private static int lastCallId = 0;
	private static long startDate = (new Date()).getTime(); // in order not to handle calls that were before the app start
	
	private List<OnCallMissRejectListener> listeners = new ArrayList<OnCallMissRejectListener>();

	private CallLogObserver(Handler handler) {
		super(handler);

		this.handler = handler;
	}
	
	public CallLogObserver(Handler handler, Context context) {
		this(handler);
		this.ctx = context;
	}
	
	public void addListener(OnCallMissRejectListener l) {
		listeners.add(l);
	}

	@Override
	public void onChange(boolean selfChange) {
		Log.i(Constants.LOG_TAG, "OnChanged");
		
		Cursor cursorMissed = ctx.getContentResolver().query(Calls.CONTENT_URI, null, Calls._ID + " > ? AND " + Calls.TYPE + " = ? AND " + Calls.NEW + " = ? AND " + Calls.DATE + " > ? ",
				new String[] {Integer.toString(lastCallId), Integer.toString(Calls.MISSED_TYPE), "1", String.valueOf(startDate)}, Calls.DATE + " DESC ");
		
		Cursor cursorRejected = ctx.getContentResolver().query(Calls.CONTENT_URI, null, Calls._ID + " > ? AND " + Calls.TYPE + " = ? AND " + Calls.DURATION + " = ? AND " + 
				Calls.NEW + " = ? AND " + Calls.DATE + " > ? ",
				new String[] {Integer.toString(lastCallId), Integer.toString(Calls.INCOMING_TYPE), "0", "1", String.valueOf(startDate)}, Calls.DATE + " DESC ");
		
		List<CallInfo> callsAll = new ArrayList<CallInfo>();
		
		List<CallInfo> callsMissed = getCallsInfo(cursorMissed, TYPE.MISSED);
		List<CallInfo> callsRejected = getCallsInfo(cursorRejected, TYPE.REJECTED);
		callsAll.addAll(callsMissed);
		callsAll.addAll(callsRejected);
		
		handleCalls(callsAll);
	}
	
	private void handleCalls(List<CallInfo> calls) {
		for (CallInfo info : calls) {
			onEvent(info);
		}
	}
	
	private List<CallInfo> getCallsInfo(Cursor cursor, TYPE callType) {
		int c = cursor.getCount();
		List<CallInfo> calls = new ArrayList<CallInfo>(c);
		
		Log.i(Constants.LOG_TAG, "Found " + callType + " calls: " + c);
		
		if (c > 0) {
			int i = 0;
			int indexID = cursor.getColumnIndex(CallLog.Calls._ID);
			int indexNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);
			int indexDate = cursor.getColumnIndex(CallLog.Calls.DATE);
			int indexType = cursor.getColumnIndex(CallLog.Calls.TYPE);
			while (cursor.moveToNext()) {
				int id = cursor.getInt(indexID);
				lastCallId = id > lastCallId ? id : lastCallId;
				String number = cursor.getString(indexNumber);
				String type = cursor.getString(indexType);
				String date = cursor.getString(indexDate);
				long callDate = CallInfo.INVALID_DATE;
				
				try {
					callDate = Long.parseLong(date);
				} catch (NumberFormatException e) {
					Log.e(Constants.LOG_TAG, "Error parcing call date from Calls Source!", e);
				}
				
				Log.i(Constants.LOG_TAG, String.format("%s: %s | %s | %s", ++i, number, type, date));
				
				CallInfo info = new CallInfo(number, callDate, callType);
				calls.add(info);
			}
		}
		
		return calls;
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

}
