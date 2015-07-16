package com.wantcallback.notifications;

import android.content.Context;
import android.content.IntentFilter;

/**
 * Created by Yahor_Fralou on 7/15/2015.
 */
public class NotifBroadcastReceiverFactory {
    private static NotificationActionBroadcastReceiver receiver = null;
    private static IntentFilter filter;

    public static void registerReceiver(Context ctx) {
        if (receiver == null) {
            receiver = new NotificationActionBroadcastReceiver();
            filter = new IntentFilter();
            filter.addAction(NotificationActionBroadcastReceiver.ACTION_FORGET);
        }

        ctx.registerReceiver(receiver, filter);
    }

    public static void unregisterReceiver(Context ctx) {
        if (receiver != null) {
            ctx.unregisterReceiver(receiver);
        }
    }
}
