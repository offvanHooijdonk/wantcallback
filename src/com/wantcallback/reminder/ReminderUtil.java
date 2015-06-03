package com.wantcallback.reminder;

import java.util.Calendar;
import java.util.Date;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.dao.model.ReminderInfo;

import android.content.Context;

public class ReminderUtil {
	private static final int DEFAULT_TIME_ADD = 10; // FIXME move to options
	
	public static void createNewReminder(Context ctx, ReminderInfo info) {
		// TODO try exceptions ?
		AlarmUtil.createNewReminder(ctx, info.getId(), info.getPhone(), new Date(info.getDate()));
		
		ReminderDao reminderDao = new ReminderDao(ctx);
		reminderDao.save(info);
		
		// TODO add reboot awareness (alarms can be lost if device rebooted)
	}
	
	public static long calcDeafaultRemindDate(long timeMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.MINUTE, DEFAULT_TIME_ADD);
		return calendar.getTimeInMillis();
	}
}
