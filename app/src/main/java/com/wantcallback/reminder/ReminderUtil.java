package com.wantcallback.reminder;

import android.content.Context;
import android.widget.Toast;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.ReminderInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderUtil {
	
	public static void createNewReminder(Context ctx, ReminderInfo reminder) {
		ReminderDao reminderDao = new ReminderDao(ctx);
		reminderDao.save(reminder);

		AlarmUtil.createNewReminderAlarm(ctx, reminder.getId(), new Date(reminder.getDate()));
	}
	
	public static void createNewDefaultReminder(Context ctx, ReminderInfo info) {
		info.setDate(ReminderUtil.calcDefaultRemindDate(ctx, info.getDate()));
		
		createNewReminder(ctx, info);
	}
	
	public static void cancelReminder(Context ctx, ReminderInfo reminder) {
		if (reminder != null) {
			AlarmUtil.cancelAlarm(ctx, reminder.getId());
			ReminderDao reminderDao = new ReminderDao(ctx);
			reminderDao.deleteByPhone(reminder.getPhone());
			Toast.makeText(ctx, "Forgot " + reminder.getPhone(), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(ctx, "Did not find reminder for " + reminder.getPhone() + " !", Toast.LENGTH_LONG).show();
		}

	}

	public static void postponeReminder(Context ctx, ReminderInfo reminder, int minutes) {
		Calendar remindTime = Calendar.getInstance();
		remindTime.set(Calendar.SECOND, 0);
		remindTime.set(Calendar.MILLISECOND, 0);
		remindTime.add(Calendar.MINUTE, minutes);

		AlarmUtil.cancelAlarm(ctx, reminder.getId());
		AlarmUtil.createNewReminderAlarm(ctx, reminder.getId(), remindTime.getTime());

		reminder.setDate(remindTime.getTimeInMillis());
		ReminderDao dao = new ReminderDao(ctx);
		dao.save(reminder);
	}
	
	public static long calcDefaultRemindDate(Context ctx, long timeMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.MINUTE, AppHelper.Pref.getDefaultReminderMins(ctx));
		return calendar.getTimeInMillis();
	}

	public static void muteAllReminders(Context ctx) {
		ReminderDao dao = new ReminderDao(ctx);

		List<ReminderInfo> reminders = dao.getAll();

		for (ReminderInfo r : reminders) {
			AlarmUtil.cancelAlarm(ctx, r.getId());
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

		List<ReminderInfo> reminders = dao.getAllSince(now.getTime());

		for (ReminderInfo r : reminders) {
			AlarmUtil.createNewReminderAlarm(ctx, r.getId(), new Date(r.getDate()));
		}

		// remove all, because they are not actual
		dao.deleteAllBeforeDate(now.getTime());
	}

}
