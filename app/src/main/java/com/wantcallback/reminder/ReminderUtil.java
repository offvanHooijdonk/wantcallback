package com.wantcallback.reminder;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.ReminderInfo;

import android.content.Context;
import android.widget.Toast;

public class ReminderUtil {
	private static final int DEFAULT_TIME_ADD = 10; // FIXME move to options
	
	public static void createNewReminder(Context ctx, ReminderInfo info) {
		AlarmUtil.createNewReminderAlarm(ctx, info.getId(), info.getPhone(), new Date(info.getDate()));
		
		ReminderDao reminderDao = new ReminderDao(ctx);
		reminderDao.save(info);
	}
	
	public static void createNewDefaultReminder(Context ctx, ReminderInfo info) {
		info.setDate(ReminderUtil.calcDefaultRemindDate(info.getDate()));
		
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
	
	public static long calcDefaultRemindDate(long timeMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.MINUTE, DEFAULT_TIME_ADD);
		return calendar.getTimeInMillis();
	}

	public static void muteAllReminders(Context ctx) {
		ReminderDao dao = new ReminderDao(ctx);

		List<ReminderInfo> reminders = dao.getAll();

		for (ReminderInfo r : reminders) {
			AlarmUtil.cancelAlarm(ctx, r.getId(), r.getPhone());
		}
	}

	// TODO make preferences that say what time before now reminders considered actual

	/**
	 * Reads time for which back reminders considered still actual and creates alarms for these reminders. Removes all other reminders
	 * from DB
	 * @param ctx context
	 */
	public static void recreateActualReminders(Context ctx) {
		ReminderDao dao = new ReminderDao(ctx);

		Calendar now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		List<ReminderInfo> reminders = dao.getAllSince(now.getTime());

		for (ReminderInfo r : reminders) {
			AlarmUtil.createNewReminderAlarm(ctx, r.getId(), r.getPhone(), new Date(r.getDate()));
		}

		// remove all, because they are not actual
		dao.deleteAllBeforeDate(now.getTime());
	}
	
	public static int getDefaultRemindMinutes() {
		return DEFAULT_TIME_ADD;
	}
}
