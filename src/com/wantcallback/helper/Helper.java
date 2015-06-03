package com.wantcallback.helper;

import com.wantcallback.dao.model.ReminderInfo;
import com.wantcallback.observer.model.CallInfo;

public class Helper {

	public static ReminderInfo convertCallToReminder(CallInfo callInfo) {
		ReminderInfo reminderInfo = new ReminderInfo();
		reminderInfo.setDate(callInfo.getDate());
		reminderInfo.setId(callInfo.getLogId());
		reminderInfo.setPhone(callInfo.getPhone());
		
		return reminderInfo;
	}
}
