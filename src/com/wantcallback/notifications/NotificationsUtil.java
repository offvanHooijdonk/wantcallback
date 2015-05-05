package com.wantcallback.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.CallLog;
import android.support.v4.app.NotificationCompat;

import com.wantcallback.R;
import com.wantcallback.alarms.SetAlarmActivity;
import com.wantcallback.data.ContactsUtil;
import com.wantcallback.observer.model.CallInfo;
import com.wantcallback.observer.model.ContactInfo;

public class NotificationsUtil {
	private static final int NOTIFICATION_MISSED_CALL = 0;
	private static final int NOTIFICATION_REJECTED_CALL = 1;
	
	private Context ctx;
	private static NotificationManager mNotificationManager;
	private ContactsUtil contactsUtil;
	
	public NotificationsUtil(Context context) {
		this.ctx = context;
		contactsUtil = new ContactsUtil(ctx);
	}

	public void showMissedCallNotification(CallInfo info) {
		String tag = info.getPhone();
		int id = NOTIFICATION_MISSED_CALL;
		String callerLabel = getCallerLabel(info);
		NotificationCompat.Builder builder = getCommonCallNBuilder(info).setContentTitle("Missed Call")
				.setTicker("Missed Call from " + callerLabel)
				.addAction(R.drawable.ic_edit, "Change", createReminderIntent(info, tag, id))
				.addAction(R.drawable.ic_forget, "Forget", createForgetIntent(info, tag, id));

		getNotificationManager().notify(tag, id, builder.build());
	}
	
	public void showRejectedCallNotification(CallInfo info) {
		String tag = info.getPhone();
		int id = NOTIFICATION_REJECTED_CALL;
		String callerLabel = getCallerLabel(info);
		NotificationCompat.Builder builder = getCommonCallNBuilder(info).setContentTitle("Rejected Call")
				.setTicker("Rejected Call from " + callerLabel)
				.addAction(R.drawable.ic_alarm_add, "Remind in 10m", createReminderIntent(info, tag, id));

		getNotificationManager().notify(tag, id, builder.build());
	}
	
	/**
	 * Create notification.builder that has settings common for typical Missed & Rejected notifications in the app
	 * @param phoneNumber
	 * @return
	 */
	private NotificationCompat.Builder getCommonCallNBuilder(CallInfo info) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.ic_launcher)
				.setContentText(info.getPhone()).setContentIntent(createDialerIntent(info)).setPriority(NotificationCompat.PRIORITY_HIGH)
				.setCategory("call").setSmallIcon(R.drawable.ic_notify_call).setAutoCancel(true);
		
		return builder;
	}
	
	private PendingIntent createDialerIntent(CallInfo info) {
		Intent intentCallLog = new Intent(Intent.ACTION_VIEW);
		intentCallLog.setType(CallLog.Calls.CONTENT_TYPE);
		PendingIntent intent = PendingIntent.getActivity(ctx, info.getId(), intentCallLog, 0);
		
		return intent;
	}
	
	private PendingIntent createReminderIntent(CallInfo info, String notifTag, int notifId) {
		Intent intent = new Intent(ctx, SetAlarmActivity.class);
		intent.putExtra(SetAlarmActivity.EXTRA_PHONE, info.getPhone());
		intent.putExtra(SetAlarmActivity.EXTRA_NOTIF_TAG, notifTag);
		intent.putExtra(SetAlarmActivity.EXTRA_NOTIF_ID, notifId);
		
		return PendingIntent.getActivity(ctx, info.getId(), intent, 0);
	}
	
	private PendingIntent createForgetIntent(CallInfo info, String notifTag, int notifId) {
		Intent intent = new Intent(NotificationActionBroadcastReciever.ACTION_FORGET);
		intent.putExtra(NotificationActionBroadcastReciever.EXTRA_PHONE, info.getPhone());
		intent.putExtra(NotificationActionBroadcastReciever.EXTRA_NOTIF_TAG, notifTag);
		intent.putExtra(NotificationActionBroadcastReciever.EXTRA_NOTIF_ID, notifId);
		
		return PendingIntent.getBroadcast(ctx, info.getId(), intent, 0);
	}
	
	public String getCallerLabel(CallInfo callInfo) {
		String label;
		ContactInfo contactInfo = contactsUtil.findContactByPhone(callInfo.getPhone());
		if (contactInfo != null) {
			label = contactInfo.getDisplayName();
		} else {
			label = callInfo.getPhone();
		}
		return label;
	}
	
	public void dismissNotification(String notifTag, int notifId) {
		getNotificationManager().cancel(notifTag, notifId);
	}
	
	private NotificationManager getNotificationManager() {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		
		return mNotificationManager;
	}
}
