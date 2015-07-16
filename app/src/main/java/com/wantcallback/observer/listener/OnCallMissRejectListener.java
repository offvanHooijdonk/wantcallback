package com.wantcallback.observer.listener;

import com.wantcallback.model.CallInfo;

public interface OnCallMissRejectListener {
	
	public void onCallMissed(CallInfo info);
	
	public void onCallRejected(CallInfo info);
}