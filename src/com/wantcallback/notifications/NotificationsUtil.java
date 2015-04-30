package com.wantcallback.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.CallLog;
import android.support.v4.app.NotificationCompat;

import com.wantcallback.R;
import com.wantcallback.alarms.SetAlarmActivity;
import com.wantcallback.observer.CallInfo;

public class NotificationsUtil {
	private static final int NOTIFICATION_MISSED_CALL = 0;
	private static final int NOTIFICATION_REJECTED_CALL = 1;
	
	private Context ctx;
	private static NotificationManager mNotificationManager;
	
	public NotificationsUtil(Context context) {
		this.ctx = context;
	}

	public void showMissedCallNotification(CallInfo info) {
		String tag = info.getPhone();
		int id = NOTIFICATION_MISSED_CALL;
		NotificationCompat.Builder builder = getCommonCallNBuilder(info.getPhone()).setContentTitle("Missed Call")
				.addAction(R.drawable.ic_edit, "Change", createReminderIntent(info.getPhone(), tag, id))
				.addAction(R.drawable.ic_forget, "Forget", null);

		getNotificationManager().notify(tag, id, builder.build());
	}
	
	public void showRejectedCallNotification(CallInfo info) {
		String tag = info.getPhone();
		int id = NOTIFICATION_REJECTED_CALL;
		NotificationCompat.Builder builder = getCommonCallNBuilder(info.getPhone()).setContentTitle("Rejected Call")
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
		PendingIntent intent = PendingIntent.getActivity(ctx, 0, intentCallLog, 0);
		
		return intent;
	}
	
	private PendingIntent createReminderIntent(String phoneNumber, String notifTag, int notifId) {
		Intent intent = new Intent(ctx, SetAlarmActivity.class);
		intent.putExtra(SetAlarmActivity.EXTRA_PHONE, phoneNumber);
		intent.putExtra(SetAlarmActivity.EXTRA_NOTIF_TAG, notifTag);
		intent.putExtra(SetAlarmActivity.EXTRA_NOTIF_ID, notifId);
		
		return PendingIntent.getActivity(ctx, 0, intent, 0);
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
