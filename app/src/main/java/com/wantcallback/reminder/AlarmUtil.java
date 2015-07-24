package com.wantcallback.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.wantcallback.notifications.NotificationActionBroadcastReceiver;

import java.util.Date;

public class AlarmUtil {
	private static AlarmManager alarmManager;

	public static void createNewReminderAlarm(Context ctx, long remId, String phoneNumber, Date date) {
		// TODO make reboot-proof
		getAlarmManager(ctx).set(AlarmManager.RTC_WAKEUP, date.getTime(), preparePendingIntent(ctx, remId));
	}
	
	public static void cancelAlarm(Context ctx, long reminderId) {
		getAlarmManager(ctx).cancel(preparePendingIntent(ctx, reminderId));
	}
	
	private static AlarmManager getAlarmManager(Context ctx) {
		if (alarmManager == null) {
			alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		}
		
		return alarmManager;
	}
	
	private static PendingIntent preparePendingIntent(Context ctx, long reminderId) {
		Intent intent = new Intent(NotificationActionBroadcastReceiver.ACTION_REMIND);
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_REMINDER_ID, reminderId);
		
		return PendingIntent.getBroadcast(ctx, (int) reminderId, intent, 0);
	}
}
