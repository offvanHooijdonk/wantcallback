package com.wantcallback.observer.listener.impl;

import android.content.Context;

import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.observer.model.CallInfo;
import com.wantcallback.observer.model.CallInfo.TYPE;

public class StandardMissRejectListener implements OnCallMissRejectListener {
	
	private Context ctx;
	private NotificationsUtil notifyUtil;
	
	public StandardMissRejectListener(Context context) {
		this.ctx = context;
		notifyUtil = new NotificationsUtil(ctx);
	}

	@Override
	public void onCallMissed(CallInfo info) {
		pushNotification(info);
	}

	@Override
	public void onCallRejected(CallInfo info) {
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