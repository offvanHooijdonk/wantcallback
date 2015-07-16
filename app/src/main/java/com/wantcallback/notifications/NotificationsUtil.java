package com.wantcallback.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.wantcallback.R;
import com.wantcallback.data.ContactsUtil;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ContactInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.reminder.ReminderUtil;
import com.wantcallback.ui.MainActivity;
import com.wantcallback.ui.SetReminderActivity;

import java.util.Date;

public class NotificationsUtil {
	private static final int NOTIFICATION_MISSED_CALL = 0;
	private static final int NOTIFICATION_REJECTED_CALL = 1;
	private static final int NOTIFICATION_REMINDER = 2;
	public static final int NOTIFICATION_FOREGROUND_SERVICE = 2;
	
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
				.setTicker("Missed Call from " + callerLabel) // TODO Show message 'Reminder created on 23:15'
				.setContentIntent(createOpenRemindersIntent(info, tag, id)) // Open reminder settings
				.addAction(R.drawable.ic_edit, "Call now", createDialerIntent(info)) // Dial missed call
				.addAction(R.drawable.ic_forget, "Forget", createForgetIntent(info, tag, id)); // remove reminder created
		
		getNotificationManager().notify(tag, id, builder.build());
	}
	
	public void showRejectedCallNotification(CallInfo info) {
		String tag = info.getPhone();
		int id = NOTIFICATION_REJECTED_CALL;
		String callerLabel = getCallerLabel(info);
		int defaultMin = ReminderUtil.getDefaultRemindMinutes();
		NotificationCompat.Builder builder = getCommonCallNBuilder(info).setContentTitle("Rejected Call")
				.setTicker("Rejected Call from " + callerLabel)
				.setContentIntent(createOpenRemindersIntent(info, tag, id)) // open activity to set custom info
				.addAction(R.drawable.ic_alarm_add, "Remind in " + defaultMin + "m", createDefaultReminderIntent(info, tag, id)); // create default reminder silently

		getNotificationManager().notify(tag, id, builder.build());
	}
	
	public void showRejectedCallNotification(CallInfo info, ReminderInfo reminderInfo) {
		String tag = info.getPhone();
		int id = NOTIFICATION_REJECTED_CALL;
		String callerLabel = getCallerLabel(info);
		
		NotificationCompat.Builder builder = getCommonCallNBuilder(info).setContentTitle("Rejected Call")
				.setContentText("You already have a reminder at " + AppHelper.sdfTime.format(new Date(reminderInfo.getDate())))
				.setTicker("Rejected Call from " + callerLabel)
				.setContentIntent(createOpenRemindersIntent(info, tag, id)) // open activity to set custom info
				.addAction(R.drawable.ic_call, "Call now", createDialerIntent(info)) // Dial missed call
				.addAction(R.drawable.ic_forget, "Forget", createForgetIntent(info, tag, id)); // remove reminder created
		
		getNotificationManager().notify(tag, id, builder.build());
	}
	
	public void showReminderNotification(CallInfo callInfo, ReminderInfo reminderInfo) {
		String tag = reminderInfo.getPhone();
		int id = NOTIFICATION_REMINDER;
		
		NotificationCompat.Builder builder = getCommonCallNBuilder(callInfo).setContentTitle("Time to call back to" + reminderInfo.getPhone())
				.setContentText("Call to " + reminderInfo.getPhone() + " that called you at " + AppHelper.sdfTime.format(new Date(callInfo.getDate())))
				.setTicker("Call back to " + reminderInfo.getPhone())
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setLights(ctx.getResources().getColor(R.color.led_blue), 500, 500) // TODO make configurable
				.setVibrate(new long[]{200, 300, 200, 300, 200, 300})
				//.setDefaults(Notification.DEFAULT_ALL)
				.setContentIntent(createDialerIntent(callInfo))
				.addAction(R.drawable.ic_edit, "Change", createOpenRemindersIntent(callInfo, tag, id))
				.addAction(R.drawable.ic_forget, "Forget", createForgetIntent(callInfo, tag, id));
		// TODO add Postpone action?
		
		getNotificationManager().notify(tag, id, builder.build());
	}

	public Notification createForegroundServiceNotification() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx).setLargeIcon(getLargeIconBitmap(R.drawable.ic_launcher))
				.setSmallIcon(R.drawable.ic_notify_call)
				.setContentTitle(ctx.getString(R.string.foreground_title))
				.setContentText(ctx.getString(R.string.foreground_message))
				.setContentIntent(createMainActivityPedingIntent());

		return builder.build();
	}
	
	/**
	 * Create notification.builder that has settings common for typical Missed & Rejected notifications in the app
	 * @param info
	 * @return
	 */
	private NotificationCompat.Builder getCommonCallNBuilder(CallInfo info) {
		// TODO remove one of icons
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.ic_launcher)
				.setContentText(info.getPhone()).setPriority(NotificationCompat.PRIORITY_HIGH)
				.setCategory("call").setAutoCancel(true);
		
		return builder;
	}
	
	private PendingIntent createDialerIntent(CallInfo info) {
		Intent intentDialer = new Intent(Intent.ACTION_DIAL);
		intentDialer.setData(Uri.parse("tel:" + info.getPhone()));
		PendingIntent intent = PendingIntent.getActivity(ctx, info.getLogId(), intentDialer, 0);
		
		return intent;
	}
	
	private PendingIntent createOpenRemindersIntent(CallInfo info, String notifTag, int notifId) {
		Intent intent = new Intent(ctx, SetReminderActivity.class);
		intent.putExtra(SetReminderActivity.EXTRA_PHONE, info.getPhone());
		intent.putExtra(SetReminderActivity.EXTRA_CALL_ID, info.getLogId());
		intent.putExtra(SetReminderActivity.EXTRA_NOTIF_TAG, notifTag);
		intent.putExtra(SetReminderActivity.EXTRA_NOTIF_ID, notifId);
		
		return PendingIntent.getActivity(ctx, info.getLogId(), intent, 0);
	}

	private PendingIntent createMainActivityPedingIntent() {
		return PendingIntent.getActivity(ctx, 0, new Intent(ctx, MainActivity.class), 0);
	}
	
	private PendingIntent createForgetIntent(CallInfo info, String notifTag, int notifId) {
		Intent intent = new Intent(NotificationActionBroadcastReceiver.ACTION_FORGET);
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_PHONE, info.getPhone());
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_TAG, notifTag);
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_ID, notifId);
		
		return PendingIntent.getBroadcast(ctx, info.getLogId(), intent, 0);
	}
	
	private PendingIntent createDefaultReminderIntent(CallInfo info, String notifTag, int notifId) {
		Intent intent = new Intent(NotificationActionBroadcastReceiver.ACTION_CREATE_DEFAULT_REMINDER);
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_PHONE, info.getPhone());
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_TAG, notifTag);
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_ID, notifId);
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_CALL_DATE_LONG, info.getDate());
		intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_CALL_ID, info.getLogId());
		
		return PendingIntent.getBroadcast(ctx, info.getLogId(), intent, 0);
	}
	
	private String getCallerLabel(CallInfo callInfo) {
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

	private Bitmap getLargeIconBitmap(int resourceId) {
		return BitmapFactory.decodeResource(ctx.getResources(), resourceId);
	}
	
	private NotificationManager getNotificationManager() {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		
		return mNotificationManager;
	}

}
