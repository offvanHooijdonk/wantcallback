package com.wantcallback.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.reminder.ReminderUtil;

public class NotificationActionBroadcastReceiver extends BroadcastReceiver {
	public static final String ACTION_FORGET = "action_forget";
	public static final String ACTION_CREATE_DEFAULT_REMINDER = "action_create_default_reminder";
	public static final String ACTION_REMIND = "action_reminde";
	
	public static final String EXTRA_PHONE = "extra_phone";
	public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
	public static final String EXTRA_NOTIF_ID = "extra_notif_id";
	public static final String EXTRA_CALL_ID = "extra_notif_id";
	public static final String EXTRA_CALL_DATE_LONG = "extra_call_date_long";

	public NotificationActionBroadcastReceiver() {
	}

	public static String[] getAllActions() {
		return new String[] {ACTION_CREATE_DEFAULT_REMINDER, ACTION_FORGET, ACTION_REMIND};
	}

	@Override
	public void onReceive(Context ctx, Intent intent) {
	
		String action = intent.getAction();
		
		if (ACTION_FORGET.equals(action)) {
			String phoneNumber = intent.getExtras().getString(EXTRA_PHONE);
			int notifId = intent.getExtras().getInt(EXTRA_NOTIF_ID);
			
			if (phoneNumber != null) {
				String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG);
				
				NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);
				notificationsUtil.dismissNotification(tag, notifId);
				
				ReminderUtil.cancelReminder(ctx, phoneNumber);
			} else {
				//TODO what to do if phoneNumber is null
			}
		} else if (ACTION_CREATE_DEFAULT_REMINDER.equals(action)) {
			// TODO do not create anything if application is disabled
			String phoneNumber = intent.getExtras().getString(EXTRA_PHONE);
			int notifId = intent.getExtras().getInt(EXTRA_NOTIF_ID);
			long callDateLong = intent.getExtras().getLong(EXTRA_CALL_DATE_LONG);
			int callId = intent.getExtras().getInt(EXTRA_CALL_ID);
			
			if (phoneNumber != null) {
				String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG);
				
				NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);
				notificationsUtil.dismissNotification(tag, notifId);
				
				ReminderInfo info = new ReminderInfo();
				info.setDate(callDateLong);
				info.setId(callId);
				info.setPhone(phoneNumber);
				
				ReminderUtil.createNewDefaultReminder(ctx, info);
			} else {
				//TODO what to do if phoneNumber is null
			}
		} else if (ACTION_REMIND.equals(action)) {
			String phone = intent.getExtras().getString(EXTRA_PHONE);
			int callId = intent.getExtras().getInt(EXTRA_CALL_ID);
			
			ReminderDao dao = new ReminderDao(ctx);
			ReminderInfo reminderInfo = dao.findByPhone(phone);
			
			if (reminderInfo != null) {
				if (AppHelper.isApplicationEnabled(ctx)) { // TODO remove this check as if app disable there must be no Receiver registered
					NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);

					CallInfo callInfo = reminderInfo.getCallInfo();
					notificationsUtil.showReminderNotification(callInfo, reminderInfo);
				}
				dao.deleteByPhone(phone);
			}
		}
		
	}

}
