package com.wantcallback.phone;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.Log;

import com.wantcallback.Constants;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.CallInfo.TYPE;

import java.util.ArrayList;
import java.util.List;

public class CallLogUtil {
	private Context ctx;
	
	public CallLogUtil(Context context) {
		this.ctx = context;
	}

	public List<CallInfo> getMissedCalls(int startCallId, long startDate) {
		Cursor cursorMissed = ctx.getContentResolver().query(Calls.CONTENT_URI, null, Calls._ID + " > ? AND " + Calls.TYPE + " = ? AND " + Calls.NEW + " = ? AND " + Calls.DATE + " > ? ",
				new String[] {Integer.toString(startCallId), Integer.toString(Calls.MISSED_TYPE), "1", String.valueOf(startDate)}, Calls.DATE + " DESC ");

		List<CallInfo> list = getCallsInfo(cursorMissed, TYPE.MISSED);

		cursorMissed.close();

		return list;
	}
	
	public List<CallInfo> getRejectedCalls(int startCallId, long startDate) {
		Cursor cursorRejected = ctx.getContentResolver().query(Calls.CONTENT_URI, null, Calls._ID + " > ? AND " + Calls.TYPE + " = ? AND " + Calls.DURATION + " = ? AND " + 
				Calls.NEW + " = ? AND " + Calls.DATE + " > ? ",
				new String[] {Integer.toString(startCallId), Integer.toString(Calls.INCOMING_TYPE), "0", "1", String.valueOf(startDate)}, Calls.DATE + " DESC ");

		List<CallInfo> list = getCallsInfo(cursorRejected, TYPE.REJECTED);

		cursorRejected.close();

		return list;
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
				String number = cursor.getString(indexNumber);
				String type = cursor.getString(indexType);
				String date = cursor.getString(indexDate);
				long callDate = CallInfo.INVALID_DATE;
				
				try {
					callDate = Long.parseLong(date);
				} catch (NumberFormatException e) {
					Log.e(Constants.LOG_TAG, "Error parsing call date from Calls Source!", e);
				}
				
				Log.i(Constants.LOG_TAG, String.format("%s: %s | %s | %s", ++i, number, type, date));
				
				CallInfo info = new CallInfo(id, number, callDate, callType);
				calls.add(info);
			}
		}
		
		return calls;
	}
}
