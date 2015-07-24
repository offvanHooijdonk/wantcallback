package com.wantcallback.reminder;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.ReminderInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderUtil {
	private static final String DEFAULT_TIME_ADD = "10";
	
	public static void createNewReminder(Context ctx, ReminderInfo reminder) {
		AlarmUtil.createNewReminderAlarm(ctx, reminder.getCallInfo().getLogId(), reminder.getPhone(), new Date(reminder.getDate()));
		
		ReminderDao reminderDao = new ReminderDao(ctx);
		reminderDao.save(reminder);
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
	
	public static long calcDefaultRemindDate(Context ctx, long timeMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMillis);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.MINUTE, getDefaultRemindMinutes(ctx));
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
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		List<ReminderInfo> reminders = dao.getAllSince(now.getTime());

		for (ReminderInfo r : reminders) {
			AlarmUtil.createNewReminderAlarm(ctx, r.getId(), r.getPhone(), new Date(r.getDate()));
		}

		// remove all, because they are not actual
		dao.deleteAllBeforeDate(now.getTime());
	}
	
	public static int getDefaultRemindMinutes(Context ctx) {
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString(ctx.getString(R.string.default_reminder_time_key), DEFAULT_TIME_ADD));
	}
}
