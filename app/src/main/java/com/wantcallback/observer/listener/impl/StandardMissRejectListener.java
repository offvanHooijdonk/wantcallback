package com.wantcallback.observer.listener.impl;

import android.content.Context;
import android.content.Intent;

import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.model.CallInfo;
import com.wantcallback.reminder.ReminderUtil;
import com.wantcallback.ui.MainActivity;

public class StandardMissRejectListener implements OnCallMissRejectListener {

    private Context ctx;
    private NotificationsUtil notifyUtil;
    private ReminderDao reminderDao;

    public StandardMissRejectListener(Context context) {
        this.ctx = context;
        notifyUtil = new NotificationsUtil(ctx);
        reminderDao = new ReminderDao(ctx);
    }

    @Override
    public void onCallMissed(CallInfo call) {
        int action = AppHelper.Pref.getActionOnMissed(ctx);

        if (call.getPhone() != null && !"".equals(call.getPhone())) {
            ReminderInfo reminderInfo = reminderDao.findByPhone(call.getPhone());
            // TODO make this logic configurable
            if (reminderInfo == null) { // no reminders yet
                if (action == ACTION_MISSED_CREATE) {
                    reminderInfo = AppHelper.convertCallToReminder(call);

                    ReminderUtil.createNewDefaultReminder(ctx, reminderInfo);
                    sendBroadCastToActivity(ctx);
                    notifyUtil.showMissedCallCreatedNotification(reminderInfo);
                } else if (action == ACTION_MISSED_DECIDE) {
                    notifyUtil.showMissedCallDecideNotification(call);
                } else if (action == ACTION_MISSED_NONE) {
                    // do nothing
                }
            } else {
                // TODO if reminder exists
            }
        } else {
            // TODO hidden number
        }

    }

    @Override
    public void onCallRejected(CallInfo call) {
        int action = AppHelper.Pref.getActionOnRejected(ctx);

        if (call.getPhone() != null && !"".equals(call.getPhone())) {
            if (call.getPhone() != null && !"".equals(call.getPhone())) {
                ReminderInfo reminderInfo = reminderDao.findByPhone(call.getPhone());
                if (reminderInfo == null) { // no reminders yet
                    if (action == ACTION_REJECTED_CREATE) {
                        reminderInfo = AppHelper.convertCallToReminder(call);

                        ReminderUtil.createNewDefaultReminder(ctx, reminderInfo);
                        sendBroadCastToActivity(ctx);
                        notifyUtil.showRejectedCallCreatedNotification(reminderInfo);
                    } else if (action == ACTION_REJECTED_DECIDE) {
                        notifyUtil.showRejectedCallDecideNotification(call);
                    } else if (action == ACTION_REJECTED_NONE) {
                        // do nothing
                    }
                }
            } else {
                // TODO if reminder exists
            }
        } else {
            // TODO hidden number
        }
    }

    private void sendBroadCastToActivity(Context ctx) {
        ctx.sendBroadcast(new Intent(MainActivity.RemindersBroadcastReceiver.ACTION_ANY));
    }

}
