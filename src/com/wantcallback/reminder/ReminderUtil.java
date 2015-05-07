package com.wantcallback.reminder;

import java.util.Date;

import android.content.Context;

public class ReminderUtil {
	
	public static void createNewReminder(Context ctx, int callId, String phoneNumber, Date date) {
		// TODO save to DB
		
		AlarmUtil.createNewReminder(ctx, callId, phoneNumber, date);
		
		// TODO add reboot awarness
	}
}
