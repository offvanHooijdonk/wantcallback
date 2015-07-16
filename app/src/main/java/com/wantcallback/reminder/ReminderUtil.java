package com.wantcallback.reminder;

import java.util.Calendar;
import java.util.Date;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.ReminderInfo;

import android.content.Context;
import android.widget.Toast;

public class ReminderUtil {
	private static final int DEFAULT_TIME_ADD = 10; // FIXME move to options
	
	public static void createNewReminder(Context ctx, ReminderInfo info) {
		// TODO try exceptions ?
		AlarmUtil.createNewReminderAlarm(ctx, info.getId(), info.getPhone(), new Date(info.getDate()));
		
		ReminderDao reminderDao = new ReminderDao(ctx);
		reminderDao.save(info);
		
		// TODO add reboot awareness (alarms can be lost if device rebooted)
	}
	
	public static void createNewDefaultReminder(Context ctx, ReminderInfo info) {
		info.setDate(ReminderUtil.calcDeafaultRemindDate(info.getDate()));
		
		createNewReminder(ctx, info);
	}
	
	public static void cancelReminder(Context ctx, String phoneNumber) {
		ReminderDao dao = new ReminderDao(ctx);
		ReminderInfo info = dao.findByPhone(phoneNumber);
		if (info != null) {
			AlarmUtil.cancelAlarm(ctx, info.getId(), info.getPhone());
			ReminderDao reminderDao = new ReminderDao(ctx);
			reminderDao.deleteByPhone(info.getPhone());
			Toast.makeText(ctx, "Forgot " + phoneNumber, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(ctx, "Did not find reminder for " + phoneNumber + " !", Toast.LENGTH_LONG).show();
		}

	}
	
	public static long calcDeafaultRemindDate(long timeMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.MINUTE, DEFAULT_TIME_ADD);
		return calendar.getTimeInMillis();
	}
	
	public static int getDefaultRemindMinutes() {
		return DEFAULT_TIME_ADD;
	}
}
