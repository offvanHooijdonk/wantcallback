package com.wantcallback.reminder;

import android.content.Context;

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
	
	public static void cancelAndRemoveReminder(Context ctx, ReminderInfo reminder) {
		if (reminder != null) {
			AlarmUtil.cancelAlarm(ctx, reminder.getId());
			ReminderDao reminderDao = new ReminderDao(ctx);
			reminderDao.deleteByPhone(reminder.getPhone());
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
		calendar = AppHelper.cutToMinutes(calendar);
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

	/**
	 * Reads time for which back reminders considered still actual and creates alarms for these reminders. Removes all other reminders
	 * from DB
	 * @param ctx context
	 */
	public static void recreateActualReminders(Context ctx) {
		ReminderDao dao = new ReminderDao(ctx);

		Calendar since = getActualSinceTime(ctx);

		List<ReminderInfo> reminders = dao.getAllSince(since.getTime());

		for (ReminderInfo r : reminders) {
			long remindAt;
			Calendar now = Calendar.getInstance();
			Calendar reminderTime = Calendar.getInstance();
			reminderTime.setTimeInMillis(r.getDate());
			if (reminderTime.before(now)) {
				reminderTime.setTimeInMillis(now.getTimeInMillis());
				reminderTime = AppHelper.cutToMinutes(reminderTime);
				reminderTime.add(Calendar.MINUTE, 1); // so it is in the future
				remindAt = reminderTime.getTimeInMillis();
			} else {
				remindAt = r.getDate();
			}
			AlarmUtil.createNewReminderAlarm(ctx, r.getId(), new Date(remindAt));
		}

		// remove all, because they are not actual
		dao.deleteAllBeforeDate(since.getTime());
	}

	private static Calendar getActualSinceTime(Context ctx) {
		Calendar calendar = Calendar.getInstance();
		int minutes = AppHelper.Pref.getOutdatedActualMins(ctx);
		if (minutes == AppHelper.Pref.OUTDATED_ACTUAL_ALL) {
			calendar.setTimeInMillis(0);
		} else {
			if (minutes != 0) {
				calendar.add(Calendar.MINUTE, -minutes);
			}
		}

		return calendar;
	}

}
