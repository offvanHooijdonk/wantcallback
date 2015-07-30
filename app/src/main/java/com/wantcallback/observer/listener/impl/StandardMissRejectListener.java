package com.wantcallback.observer.listener.impl;

import android.content.Context;
import android.content.Intent;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.model.CallInfo;
import com.wantcallback.reminder.ReminderUtil;
import com.wantcallback.ui.MainActivity;

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
		if (info.getPhone() != null && !"".equals(info.getPhone())) {
			ReminderInfo reminderInfo = reminderDao.findByPhone(info.getPhone());
			if (reminderInfo == null) { // no reminders yet
				// TODO make this logic configurable
				reminderInfo = AppHelper.convertCallToReminder(info);

				ReminderUtil.createNewDefaultReminder(ctx, reminderInfo);
				sendBroadCastToActivity(ctx);
				notifyUtil.showMissedCallNotification(reminderInfo);
			}
		} else {
			// TODO handle hidden number
		}
	}

	@Override
	public void onCallRejected(CallInfo info) {
		if (info.getPhone() != null && !"".equals(info.getPhone())) {
			ReminderInfo reminderInfo = reminderDao.findByPhone(info.getPhone());
			// TODO make this logic configurable
			if (reminderInfo == null) { // no reminders yet
				notifyUtil.showRejectedCallNotification(info);
			} else {
				notifyUtil.showRejectedCallNotification(reminderInfo);
			}
			sendBroadCastToActivity(ctx);
		} else {
			// TODO handle hidden number
		}
	}

	private void sendBroadCastToActivity(Context ctx) {
		ctx.sendBroadcast(new Intent(MainActivity.RemindersBroadcastReceiver.ACTION_ANY));
	}

}
