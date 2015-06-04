package com.wantcallback.notifications;

import com.wantcallback.dao.impl.ReminderDao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationActionBroadcastReciever extends BroadcastReceiver {
	public static final String ACTION_FORGET = "action_forget";
	public static final String ACTION_DEFAULT_REMINDER = "action_default_reminder";
	
	public static final String EXTRA_PHONE = "extra_phone";
	public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
	public static final String EXTRA_NOTIF_ID = "extra_notif_id";
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		
		String phoneNumber = null;
		String action = intent.getAction();
		
		if (ACTION_FORGET.equals(action)) {
			phoneNumber = intent.getExtras().getString(EXTRA_PHONE);
			int id = intent.getExtras().getInt(EXTRA_NOTIF_ID);
			
			if (phoneNumber != null) {
				String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG);
				
				NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);
				notificationsUtil.dismissNotification(tag, id);
				
				ReminderDao dao = new ReminderDao(ctx);
				int count = dao.deleteByPhone(phoneNumber);
				if (count > 0) {
					Toast.makeText(ctx, "Forgot " + phoneNumber, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(ctx, "Did not find reminder for " + phoneNumber + " !", Toast.LENGTH_LONG).show();
				}
			} else {
				//TODO what to do if phoneNumber is null
			}
		}
		
	}

}
