package com.wantcallback.observer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.wantcallback.Constants;
import com.wantcallback.observer.listener.OnCallMissRejectListener;
import com.wantcallback.observer.listener.impl.StandardMissRejectListener;

/**
 * Created by Yahor_Fralou on 7/15/2015.
 */
public class CallObserverFactory {
    private static CallLogObserver callLogObserver = null;

    public static void registerCallLogObserver(Context ctx) {
        if (callLogObserver == null) {
            OnCallMissRejectListener listener = new StandardMissRejectListener(ctx);

            callLogObserver = new CallLogObserver(new Handler(), ctx);
            callLogObserver.addListener(listener);

            Log.i(Constants.LOG_TAG, "Call observer initialized.");
        }

        ctx.getApplicationContext().getContentResolver()
                .registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, callLogObserver);
        Log.i(Constants.LOG_TAG, "Call observer registered.");
    }

    public static  void unregisterCallLogObserver(Context ctx) {
        if (callLogObserver != null) {
            ctx.getApplicationContext().getContentResolver()
                    .unregisterContentObserver(callLogObserver);
        }
    }
}
