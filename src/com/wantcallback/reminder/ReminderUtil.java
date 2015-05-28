package com.wantcallback.reminder;

import java.util.Date;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.dao.model.ReminderInfo;

import android.content.Context;

public class ReminderUtil {
	
	public static void createNewReminder(Context ctx, ReminderInfo info) {
		// TODO try exceptions ?
		AlarmUtil.createNewReminder(ctx, info.getId(), info.getPhone(), new Date(info.getDate()));
		
		ReminderDao reminderDao = new ReminderDao(ctx);
		reminderDao.save(info);
		
		// TODO add reboot awareness (alarms can be lost if device rebooted)
	}
}
