package com.wantcallback.helper;

import android.content.Context;
import android.content.Intent;

import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.startup.InitializerIntentService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AppHelper {
	public static DateFormat sdfTime = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault());
	public static DateFormat sdfDateTime = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.getDefault());

	private static final String FILE_PREF_LOCAL = "local.xml";
	private static final String PREF_APP_ENABLED = "app_enabled";

	public static ReminderInfo convertCallToReminder(CallInfo callInfo) {
		ReminderInfo reminderInfo = new ReminderInfo();
		reminderInfo.setDate(callInfo.getDate());
		reminderInfo.setPhone(callInfo.getPhone());
		reminderInfo.setCallInfo(callInfo);
		
		return reminderInfo;
	}

	public static boolean isApplicationEnabled(Context ctx) {
		return ctx.getSharedPreferences(FILE_PREF_LOCAL, Context.MODE_PRIVATE).getBoolean(PREF_APP_ENABLED, false);
	}

	public static void persistAppEnabledState(Context ctx, boolean enabled) {
		ctx.getSharedPreferences(FILE_PREF_LOCAL, Context.MODE_PRIVATE).edit().putBoolean(PREF_APP_ENABLED, enabled).commit();
	}

	public static Intent getInitServiceIntent(Context ctx, boolean startupExtraValue) {
		Intent intent = new Intent(ctx, InitializerIntentService.class);
		intent.putExtra(InitializerIntentService.EXTRA_START_SHUT, startupExtraValue);

		return intent;
	}
}
