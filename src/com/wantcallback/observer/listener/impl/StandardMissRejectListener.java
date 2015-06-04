package com.wantcallback.observer.listener.impl;

import android.content.Context;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.dao.model.ReminderInfo;
import com.wantcallback.helper.Helper;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.observer.model.CallInfo;
import com.wantcallback.observer.model.CallInfo.TYPE;
import com.wantcallback.reminder.ReminderUtil;

public class StandardMissRejectListener implements OnCallMissRejectListener {
	
	private Context ctx;
	private NotificationsUtil notifyUtil;
	private ReminderDao reminderDao;
	
	public StandardMissRejectListener(Context context) {
		this.ctx = context;
		notifyUtil = new NotificationsUtil(ctx);
	}

	@Override
	public void onCallMissed(CallInfo info) {
		// create Reminder and notify if none yet created for previous calls
		ReminderInfo reminderInfo = reminderDao.findByPhone(info.getPhone());
		if (reminderInfo == null) { // no reminders yet
			reminderInfo = Helper.convertCallToReminder(info);
			reminderInfo.setDate(ReminderUtil.calcDeafaultRemindDate(reminderInfo.getDate()));
			ReminderUtil.createNewReminder(ctx, reminderInfo);
			pushNotification(info);
		} else {
			// no action
		}
	}

	@Override
	public void onCallRejected(CallInfo info) {
		// TODO create Reminder and notify if none yet created for previous calls
		ReminderInfo reminderInfo = reminderDao.findByPhone(info.getPhone());
		if (reminderInfo == null) { // no reminders yet
			reminderInfo = Helper.convertCallToReminder(info);
			reminderInfo.setDate(ReminderUtil.calcDeafaultRemindDate(reminderInfo.getDate()));
			ReminderUtil.createNewReminder(ctx, reminderInfo);
			pushNotification(info);
		} else {
			// TODO no action or notification "you already have a reminder"? 
		}
	}

	private void pushNotification(CallInfo info) {
		if (info.getType() == TYPE.MISSED) {
			notifyUtil.showMissedCallNotification(info);
		} else if (info.getType() == TYPE.REJECTED) {
			notifyUtil.showRejectedCallNotification(info);
		}
	}

}
