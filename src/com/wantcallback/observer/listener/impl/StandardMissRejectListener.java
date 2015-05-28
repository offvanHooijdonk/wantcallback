package com.wantcallback.observer.listener.impl;

import android.content.Context;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.observer.model.CallInfo;
import com.wantcallback.observer.model.CallInfo.TYPE;

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
		// TODO create Reminder if none yet created for previous calls
		pushNotification(info);
	}

	@Override
	public void onCallRejected(CallInfo info) {
		// TODO check if has reminders already, if does - do not show notification
		pushNotification(info);
	}

	private void pushNotification(CallInfo info) {
		if (info.getType() == TYPE.MISSED) {
			notifyUtil.showMissedCallNotification(info);
		} else if (info.getType() == TYPE.REJECTED) {
			notifyUtil.showRejectedCallNotification(info);
		}
	}

}
