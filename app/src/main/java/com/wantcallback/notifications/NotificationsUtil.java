package com.wantcallback.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.wantcallback.R;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ContactInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.phone.ContactsUtil;
import com.wantcallback.ui.EditReminderActivity;
import com.wantcallback.ui.MainActivity;

import java.util.Date;

public class NotificationsUtil {
    private static final int NOTIFICATION_MISSED_CALL = 0;
    private static final int NOTIFICATION_REJECTED_CALL = 1;
    private static final int NOTIFICATION_REMINDER = 2;
    public static final int NOTIFICATION_FOREGROUND_SERVICE = 100;

    private static final int LED_ON_MS = 1200;
    private static final int LED_OFF_MS = 1200;

    private Context ctx;
    private static NotificationManager mNotificationManager;
    private ContactsUtil contactsUtil;

    public NotificationsUtil(Context context) {
        this.ctx = context;
        contactsUtil = new ContactsUtil(ctx);
    }

    public void showMissedCallCreatedNotification(ReminderInfo reminder) {
        String tag = reminder.getPhone();
        int id = NOTIFICATION_MISSED_CALL;
        String callerLabel = getCallerLabel(reminder.getCallInfo());
        String text = "Missed Call from " + callerLabel + ". A reminder has been created on " + AppHelper.getTimeFormat(ctx).format(new Date(reminder.getDate()));

        NotificationCompat.Builder builder = getCommonCallBuilder("Reminder created", text)
                .setTicker(text)
                .setSmallIcon(R.drawable.ic_alarm_on_white_24dp)
                .setContentIntent(createEditReminderIntent(reminder, tag, id)) // Open reminder settings
                .addAction(R.drawable.ic_call_black_24dp, "Call now", createDialerIntent(reminder.getCallInfo())) // Dial missed call
                .addAction(R.drawable.ic_forget, "Forget it", createForgetIntent(reminder, tag, id)); // remove reminder created

        getNotificationManager().notify(tag, id, builder.build());
    }

    public void showMissedCallDecideNotification(CallInfo call) {
        String tag = call.getPhone();
        int id = NOTIFICATION_MISSED_CALL;
        String callerLabel = getCallerLabel(call);
        int defaultMin = AppHelper.Pref.getDefaultReminderMins(ctx);
        String text = "Missed Call from " + callerLabel;

        NotificationCompat.Builder builder = getCommonCallBuilder("Want to remind?", text)
                .setTicker(text)
                .setSmallIcon(R.drawable.ic_alarm_add_white)
                .setContentIntent(createNewReminderIntent(call, tag, id)) // Open reminder settings
                .addAction(R.drawable.ic_call_black_24dp, "Call now", createDialerIntent(call)) // Dial missed call
                .addAction(R.drawable.ic_alarm_add, "Remind in " + defaultMin + "m", createDefaultReminderIntent(call, tag, id)); // remove reminder created

        getNotificationManager().notify(tag, id, builder.build());
    }

    public void showRejectedCallDecideNotification(CallInfo info) {
        String tag = info.getPhone();
        int id = NOTIFICATION_REJECTED_CALL;
        String callerLabel = getCallerLabel(info);
        int defaultMin = AppHelper.Pref.getDefaultReminderMins(ctx);
        String text = "Rejected Call from " + callerLabel;

        NotificationCompat.Builder builder = getCommonCallBuilder("Want to remind?", text)
                .setTicker(text)
                .setSmallIcon(R.drawable.ic_alarm_add_white)
                .setContentIntent(createNewReminderIntent(info, tag, id)) // open activity to set custom info
                .addAction(R.drawable.ic_alarm_add, "Remind in " + defaultMin + "m", createDefaultReminderIntent(info, tag, id)); // create default reminder silently

        getNotificationManager().notify(tag, id, builder.build());
    }

    public void showRejectedCallCreatedNotification(ReminderInfo reminder) {
        String tag = reminder.getPhone();
        int id = NOTIFICATION_REJECTED_CALL;
        String callerLabel = getCallerLabel(reminder.getCallInfo());
        String text = "Rejected " + callerLabel + ". A reminder has been created on " + AppHelper.getTimeFormat(ctx).format(new Date(reminder.getDate()));

        NotificationCompat.Builder builder = getCommonCallBuilder("Reminder created", text)
                .setTicker("Rejected Call from " + callerLabel)
                .setSmallIcon(R.drawable.ic_alarm_on_white_24dp)
                .setContentIntent(createEditReminderIntent(reminder, tag, id)) // open activity to set custom info
                .addAction(R.drawable.ic_call_black_24dp, "Call now", createDialerIntent(reminder.getCallInfo())) // Dial missed call
                .addAction(R.drawable.ic_forget, "Forget", createForgetIntent(reminder, tag, id)); // remove reminder created

        getNotificationManager().notify(tag, id, builder.build());
    }

    public void showReminderNotification(ReminderInfo reminder) {
        String tag = reminder.getPhone();
        int id = NOTIFICATION_REMINDER;
        String text = "Call to " + reminder.getPhone() + " that called you at " + AppHelper.getTimeFormat(ctx).format(new Date(reminder.getCallInfo().getDate()));

        NotificationCompat.Builder builder = getCommonCallBuilder("Remind you to call back", text)
                .setTicker("Call back to " + reminder.getPhone())
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(createEditReminderIntent(reminder, tag, id))
                .setDeleteIntent(createForgetIntent(reminder, tag, id))
                .addAction(R.drawable.ic_call_black_24dp, "Call now", createDialerIntent(reminder.getCallInfo()))
                .addAction(R.drawable.ic_alarm_black_24dp, "Postpone", createPostponeIntent(reminder, tag, id));

        if (AppHelper.Pref.getLEDEnabled(ctx)) {
            builder.setLights(AppHelper.Pref.getLEDColor(ctx), LED_ON_MS, LED_OFF_MS);
        }

        getNotificationManager().notify(tag, id, builder.build());
    }

    public Notification createForegroundServiceNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)//.setLargeIcon(getLargeIconBitmap(R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.ic_notify_call)
                .setContentTitle(ctx.getString(R.string.foreground_title))
                .setContentText(ctx.getString(R.string.foreground_message))
                .setContentIntent(createMainActivityPedingIntent())
                .setPriority(Notification.PRIORITY_MIN)
                .setShowWhen(false);

        return builder.build();
    }

    /**
     * Create notification.builder that has settings common for typical Missed & Rejected notifications in the app
     *
     * @return
     */
    private NotificationCompat.Builder getCommonCallBuilder(String title, String text) {
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(text);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)//.setLargeIcon(getLargeIconBitmap(R.drawable.ic_launcher))
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(bigText)
                .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory("call").setAutoCancel(true);

        return builder;
    }

    private PendingIntent createDialerIntent(CallInfo info) {
        Intent intentDialer = new Intent(Intent.ACTION_DIAL);
        intentDialer.setData(Uri.parse("tel:" + info.getPhone()));
        PendingIntent intent = PendingIntent.getActivity(ctx, info.getLogId(), intentDialer, 0);

        return intent;
    }

    private PendingIntent createEditReminderIntent(ReminderInfo reminder, String notifTag, int notifId) {
        Intent intent = new Intent(ctx, EditReminderActivity.class);
        intent.putExtra(EditReminderActivity.EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(EditReminderActivity.EXTRA_NOTIF_TAG, notifTag);
        intent.putExtra(EditReminderActivity.EXTRA_NOTIF_ID, notifId);
        intent.putExtra(EditReminderActivity.EXTRA_MODE, EditReminderActivity.MODE.EDIT.toString());

        return PendingIntent.getActivity(ctx, (int) reminder.getId(), intent, 0);
    }

    private PendingIntent createNewReminderIntent(CallInfo info, String notifTag, int notifId) {
        Intent intent = new Intent(ctx, EditReminderActivity.class);
        intent.putExtra(EditReminderActivity.EXTRA_CALL_INFO, info);
        intent.putExtra(EditReminderActivity.EXTRA_NOTIF_TAG, notifTag);
        intent.putExtra(EditReminderActivity.EXTRA_NOTIF_ID, notifId);
        intent.putExtra(EditReminderActivity.EXTRA_MODE, EditReminderActivity.MODE.CREATE.toString());

        return PendingIntent.getActivity(ctx, info.getLogId(), intent, 0);
    }

    private PendingIntent createMainActivityPedingIntent() {
        return PendingIntent.getActivity(ctx, 0, new Intent(ctx, MainActivity.class), 0);
    }

    private PendingIntent createForgetIntent(ReminderInfo reminder, String notifTag, int notifId) {
        Intent intent = new Intent(NotificationActionBroadcastReceiver.ACTION_FORGET);
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_TAG, notifTag);
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_ID, notifId);

        return PendingIntent.getBroadcast(ctx, (int) reminder.getId(), intent, 0);
    }

    private PendingIntent createPostponeIntent(ReminderInfo reminder, String notifTag, int notifId) {
        Intent intent = new Intent(NotificationActionBroadcastReceiver.ACTION_POSTPONE);
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_TAG, notifTag);
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_ID, notifId);

        return PendingIntent.getBroadcast(ctx, (int) reminder.getId(), intent, 0);
    }

    private PendingIntent createDefaultReminderIntent(CallInfo info, String notifTag, int notifId) {
        Intent intent = new Intent(NotificationActionBroadcastReceiver.ACTION_CREATE_DEFAULT_REMINDER);
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_CALL_INFO, info);
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_TAG, notifTag);
        intent.putExtra(NotificationActionBroadcastReceiver.EXTRA_NOTIF_ID, notifId);

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
