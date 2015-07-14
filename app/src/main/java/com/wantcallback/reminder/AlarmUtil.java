package com.wantcallback.reminder;

import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.wantcallback.notifications.NotificationActionBroadcastReciever;

public class AlarmUtil {
	private static AlarmManager alarmManager;

	public static void createNewReminderAlarm(Context ctx, int callId, String phoneNumber, Date date) {
		getAlarmManager(ctx).set(AlarmManager.RTC_WAKEUP, date.getTime(), preparePendingIntent(ctx, callId, phoneNumber));
	}
	
	public static void cancelAlarm(Context ctx, int callId, String phoneNumber) {
		getAlarmManager(ctx).cancel(preparePendingIntent(ctx, callId, phoneNumber));
	}
	
	private static AlarmManager getAlarmManager(Context ctx) {
		if (alarmManager == null) {
			alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		}
		
		return alarmManager;
	}
	
	private static PendingIntent preparePendingIntent(Context ctx, int callId, String phoneNumber) {
		Intent intent = new Intent(NotificationActionBroadcastReciever.ACTION_REMIND);
		
		intent.putExtra(NotificationActionBroadcastReciever.EXTRA_PHONE, phoneNumber);
		intent.putExtra(NotificationActionBroadcastReciever.EXTRA_CALL_ID, phoneNumber);
		
		return PendingIntent.getBroadcast(ctx, callId, intent, 0);
	}
}
