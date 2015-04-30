package com.wantcallback.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationActionBroadcastReciever extends BroadcastReceiver {
	public static final String ACTION_FORGET = "action_forget";
	
	public static final String EXTRA_PHONE = "extra_phone";
	public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
	public static final String EXTRA_NOTIF_ID = "extra_notif_id";
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		
		String phone = null;
		String action = intent.getAction();
		
		if (ACTION_FORGET.equals(action)) {
			phone = intent.getExtras().getString(EXTRA_PHONE);
			String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG);
			int id = intent.getExtras().getInt(EXTRA_NOTIF_ID);
			
			NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);
			notificationsUtil.dismissNotification(tag, id);
			
			if (phone != null) {
				Toast.makeText(ctx, "Forgot " + phone, Toast.LENGTH_LONG).show();
			}
		}
		
	}

}
