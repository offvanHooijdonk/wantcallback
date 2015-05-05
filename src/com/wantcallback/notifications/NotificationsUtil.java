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
	
	private static int requestIndex = 0;
	
	public NotificationsUtil(Context context) {
		this.ctx = context;
		contactsUtil = new ContactsUtil(ctx);
	}

	public void showMissedCallNotification(CallInfo info) {
		String tag = info.getPhone();
		int id = NOTIFICATION_MISSED_CALL;
		String callerLabel = getCallerLabel(info);
		NotificationCompat.Builder builder = getCommonCallNBuilder(info.getPhone()).setContentTitle("Missed Call")
				.setTicker("Missed Call from " + callerLabel)
				.addAction(R.drawable.ic_edit, "Change", createReminderIntent(info.getPhone(), tag, id))
				.addAction(R.drawable.ic_forget, "Forget", createForgetIntent(info.getPhone(), tag, id));

		getNotificationManager().notify(tag, id, builder.build());
	}
	
	public void showRejectedCallNotification(CallInfo info) {
		String tag = info.getPhone();
		int id = NOTIFICATION_REJECTED_CALL;
		String callerLabel = getCallerLabel(info);
		NotificationCompat.Builder builder = getCommonCallNBuilder(info.getPhone()).setContentTitle("Rejected Call")
				.setTicker("Rejected Call from " + callerLabel)
				.addAction(R.drawable.ic_alarm_add, "Remind in 10m", createReminderIntent(info.getPhone(), tag, id));

		getNotificationManager().notify(tag, id, builder.build());
	}
	
	/**
	 * Create notification.builder that has settings common for typical Missed & Rejected notifications in the app
	 * @param phoneNumber
	 * @return
	 */
	private NotificationCompat.Builder getCommonCallNBuilder(String phoneNumber) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.ic_launcher)
				.setContentText(phoneNumber).setContentIntent(createDialerIntent()).setPriority(NotificationCompat.PRIORITY_HIGH)
				.setCategory("call").setSmallIcon(R.drawable.ic_notify_call).setAutoCancel(true);
		
		return builder;
	}
	
	private PendingIntent createDialerIntent() {
		Intent intentCallLog = new Intent(Intent.ACTION_VIEW);
		intentCallLog.setType(CallLog.Calls.CONTENT_TYPE);
		PendingIntent intent = PendingIntent.getActivity(ctx, requestIndex++, intentCallLog, PendingIntent.FLAG_CANCEL_CURRENT);
		
		return intent;
	}
	
	private PendingIntent createReminderIntent(String phoneNumber, String notifTag, int notifId) {
		Intent intent = new Intent(ctx, SetAlarmActivity.class);
		intent.putExtra(SetAlarmActivity.EXTRA_PHONE, phoneNumber);
		intent.putExtra(SetAlarmActivity.EXTRA_NOTIF_TAG, notifTag);
		intent.putExtra(SetAlarmActivity.EXTRA_NOTIF_ID, notifId);
		// TODO implement better way to apply index!
		return PendingIntent.getActivity(ctx, requestIndex++, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	private PendingIntent createForgetIntent(String phoneNumber, String notifTag, int notifId) {
		Intent intent = new Intent(NotificationActionBroadcastReciever.ACTION_FORGET);
		intent.putExtra(NotificationActionBroadcastReciever.EXTRA_PHONE, phoneNumber);
		intent.putExtra(NotificationActionBroadcastReciever.EXTRA_NOTIF_TAG, notifTag);
		intent.putExtra(NotificationActionBroadcastReciever.EXTRA_NOTIF_ID, notifId);
		
		return PendingIntent.getBroadcast(ctx, requestIndex++, intent, 0);
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
