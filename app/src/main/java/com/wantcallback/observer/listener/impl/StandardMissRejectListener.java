package com.wantcallback.observer.listener.impl;

import android.content.Context;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.dao.model.ReminderInfo;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.observer.model.CallInfo;
import com.wantcallback.reminder.ReminderUtil;

public class StandardMissRejectListener implements OnCallMissRejectListener {
	
	private Context ctx;
	private NotificationsUtil notifyUtil;
	private ReminderDao reminderDao;
	
	public StandardMissRejectListener(Context context) {
		this.ctx = context;
		notifyUtil = new NotificationsUtil(ctx);
		reminderDao = new ReminderDao(ctx);
	}

	@Override
	public void onCallMissed(CallInfo info) {
		// create Reminder and notify if none yet created for previous calls
		ReminderInfo reminderInfo = reminderDao.findByPhone(info.getPhone());
		if (reminderInfo == null) { // no reminders yet
			reminderInfo = AppHelper.convertCallToReminder(info);

			ReminderUtil.createNewDefaultReminder(ctx, reminderInfo);
			notifyUtil.showMissedCallNotification(info);
		} else {
			// no action
		}
	}

	@Override
	public void onCallRejected(CallInfo info) {
		// TODO check if reminder exist and notify about call rejected showing if reminder exist and what its time
		ReminderInfo reminderInfo = reminderDao.findByPhone(info.getPhone());
		if (reminderInfo == null) { // no reminders yet
			notifyUtil.showRejectedCallNotification(info);
		} else {
			// TODO "you already have a reminder"
			notifyUtil.showRejectedCallNotification(info, reminderInfo);
		}
	}

}
