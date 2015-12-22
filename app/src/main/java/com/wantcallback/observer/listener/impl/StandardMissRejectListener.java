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

        if (action == ACTION_MISSED_CREATE) {
            if (!isHiddenNumber(call)) {
                ReminderInfo reminderInfo = reminderDao.findByPhone(call.getPhone());
                if (reminderInfo == null) {
                    reminderInfo = AppHelper.convertCallToReminder(call);

                    ReminderUtil.createNewDefaultReminder(ctx, reminderInfo);
                    sendBroadCastToActivity(ctx);
                    notifyUtil.showMissedCallCreatedNotification(reminderInfo);
                } else {
                    // if reminder exists - do nothing
                }
            } else {
                // hidden number - do nothing
            }
        } else if (action == ACTION_MISSED_DECIDE) {
            if (!isHiddenNumber(call)) {
                notifyUtil.showMissedCallDecideNotification(call);
            } else {
                // hidden number - do nothing
            }
        } else if (action == ACTION_MISSED_NONE) {
            // do nothing
        }

    }

    @Override
    public void onCallRejected(CallInfo call) {
        int action = AppHelper.Pref.getActionOnRejected(ctx);

        if (action == ACTION_REJECTED_CREATE) {
            if (!isHiddenNumber(call)) {
                ReminderInfo reminderInfo = reminderDao.findByPhone(call.getPhone());
                if (reminderInfo == null) {
                    reminderInfo = AppHelper.convertCallToReminder(call);

                    ReminderUtil.createNewDefaultReminder(ctx, reminderInfo);
                    sendBroadCastToActivity(ctx);
                    notifyUtil.showRejectedCallCreatedNotification(reminderInfo);
                } else {
                    // if reminder exists - do nothing
                }
            } else {
                // hidden number - do nothing
            }
        } else if (action == ACTION_REJECTED_DECIDE) {
            if (!isHiddenNumber(call)) {
                notifyUtil.showRejectedCallDecideNotification(call);
            } else {
                // hidden number - do nothing
            }
        } else if (action == ACTION_REJECTED_NONE) {
            // do nothing
        }

    }

    private void sendBroadCastToActivity(Context ctx) {
        ctx.sendBroadcast(new Intent(MainActivity.RemindersBroadcastReceiver.ACTION_ANY));
    }

    private boolean isHiddenNumber(CallInfo callInfo) {
        return callInfo.getPhone() != null && !"".equals(callInfo.getPhone());
    }

}
