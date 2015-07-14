package com.wantcallback.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.wantcallback.dao.model.ReminderInfo;
import com.wantcallback.observer.model.CallInfo;

public class Helper {
	public static DateFormat sdfTime = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
	public static DateFormat sdfDateTime = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);

	public static ReminderInfo convertCallToReminder(CallInfo callInfo) {
		ReminderInfo reminderInfo = new ReminderInfo();
		reminderInfo.setDate(callInfo.getDate());
		reminderInfo.setId(callInfo.getLogId());
		reminderInfo.setPhone(callInfo.getPhone());
		
		return reminderInfo;
	}
}
