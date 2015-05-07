package com.wantcallback.reminder;

import java.util.Date;

import com.wantcallback.ui.RemindActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmUtil {
	private static AlarmManager alarmManager;
	// TODO do it better way
	//private static int index = 0;

	public static void createNewReminder(Context ctx, int callId, String phoneNumber, Date date) {
		Intent intent = new Intent(ctx, RemindActivity.class);
		intent.putExtra(RemindActivity.EXTRA_PHONE, phoneNumber);
		getAlarmManager(ctx).set(AlarmManager.RTC_WAKEUP, date.getTime(), PendingIntent.getActivity(ctx, callId, intent, PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	private static AlarmManager getAlarmManager(Context ctx) {
		if (alarmManager == null) {
			alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		}
		
		return alarmManager;
	}
}
