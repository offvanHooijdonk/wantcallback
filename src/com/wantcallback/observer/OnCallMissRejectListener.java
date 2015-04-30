package com.wantcallback.observer;

public interface OnCallMissRejectListener {
	
	public void onCallMissed(CallInfo info);
	
	public void onCallRejected(CallInfo info);
}
