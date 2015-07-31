package com.wantcallback.observer.listener;

import com.wantcallback.model.CallInfo;

public interface OnCallMissRejectListener {
	int ACTION_REJECTED_CREATE = 0;
	int ACTION_REJECTED_DECIDE = 1;
	int ACTION_REJECTED_NONE = 2;

	int ACTION_MISSED_CREATE = 10;
	int ACTION_MISSED_DECIDE = 11;
	int ACTION_MISSED_NONE = 12;
	
	public void onCallMissed(CallInfo info);
	
	public void onCallRejected(CallInfo info);
}
