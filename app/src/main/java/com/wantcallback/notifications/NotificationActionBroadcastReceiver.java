package com.wantcallback.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.reminder.ReminderUtil;
import com.wantcallback.ui.MainActivity;

public class NotificationActionBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_FORGET = "action_forget";
    public static final String ACTION_CREATE_DEFAULT_REMINDER = "action_create_default_reminder";
    public static final String ACTION_REMIND = "action_remind";

    public static final String EXTRA_CALL_INFO = "extra_call_info";
    public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
    public static final String EXTRA_NOTIF_ID = "extra_notif_id";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";

    public NotificationActionBroadcastReceiver() {
    }

    public static String[] getAllActions() {
        return new String[]{ACTION_CREATE_DEFAULT_REMINDER, ACTION_FORGET, ACTION_REMIND};
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {

        String action = intent.getAction();

        if (ACTION_FORGET.equals(action)) {
            long reminderId = intent.getExtras().getLong(EXTRA_REMINDER_ID);
            int notifId = intent.getExtras().getInt(EXTRA_NOTIF_ID);

            ReminderDao dao = new ReminderDao(ctx);
            ReminderInfo reminder = dao.getById(reminderId);

            String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG);

            NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);
            notificationsUtil.dismissNotification(tag, notifId);

            ReminderUtil.cancelReminder(ctx, reminder);

            sendBroadCastToActivity(ctx);

        } else if (ACTION_CREATE_DEFAULT_REMINDER.equals(action)) {
            CallInfo call = intent.getExtras().getParcelable(EXTRA_CALL_INFO);
            int notifId = intent.getExtras().getInt(EXTRA_NOTIF_ID);
            String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG);

            NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);
            notificationsUtil.dismissNotification(tag, notifId);

            ReminderInfo reminder = new ReminderInfo();
            reminder.setDate(call.getDate());
            reminder.setPhone(call.getPhone());
            reminder.setCallInfo(call);

            ReminderUtil.createNewDefaultReminder(ctx, reminder);

            sendBroadCastToActivity(ctx);

        } else if (ACTION_REMIND.equals(action)) {
            long reminderId = intent.getExtras().getLong(EXTRA_REMINDER_ID);

            ReminderDao dao = new ReminderDao(ctx);
            ReminderInfo reminderInfo = dao.getById(reminderId);

            if (reminderInfo != null) {
                NotificationsUtil notificationsUtil = new NotificationsUtil(ctx);

                notificationsUtil.showReminderNotification(reminderInfo);

                dao.deleteById(reminderId);
            }

            sendBroadCastToActivity(ctx);
        }

    }

    private void sendBroadCastToActivity(Context ctx) {
        ctx.sendBroadcast(new Intent(MainActivity.RemindersBroadcastReceiver.ACTION_ANY));
    }
}
