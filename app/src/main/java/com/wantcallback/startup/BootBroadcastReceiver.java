package com.wantcallback.startup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wantcallback.helper.AppHelper;

/**
 * Created by Yahor_Fralou on 7/17/2015.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        boolean startService = AppHelper.isApplicationEnabled(ctx);

        if (startService) {
            ctx.startService(AppHelper.getInitServiceIntent(ctx, true));
        }
    }
}
