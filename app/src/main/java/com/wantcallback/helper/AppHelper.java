package com.wantcallback.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.telephony.PhoneNumberUtils;

import com.wantcallback.R;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.startup.InitializerIntentService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AppHelper {

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

    @SuppressLint("SimpleDateFormat")
    public static DateFormat getTimeFormat(Context ctx) {
        return new SimpleDateFormat(android.text.format.DateFormat.is24HourFormat(ctx) ? "HH:mm" : "hh:mm a");
    }

    public static DateFormat getDateFormat(Context ctx) {
        return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, getDefaultLocale(ctx));
    }

    public static Locale getDefaultLocale(Context ctx) {
        return ctx.getResources().getConfiguration().locale;
    }

    public static boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
    }

    public static boolean isTomorrow(Calendar now, Calendar calendar) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTimeInMillis(now.getTimeInMillis());
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        return isSameDay(tomorrow, calendar);
    }

    public static String formatPhoneNumber(Context ctx, String phone) {
        String formatted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            formatted = PhoneNumberUtils.formatNumber(phone, ctx.getResources().getConfiguration()
                    .locale.getISO3Country());
        } else {
            formatted = PhoneNumberUtils.formatNumber(phone);
        }

        return formatted;
    }

    public static Integer getColorizeOnImage(Context ctx, Uri uri) {
        Integer color = null;
        if (uri != null) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);
            } catch (IOException e) {
                color = null;
            }
            if (bitmap != null) {
                Palette palette = new Palette.Builder(bitmap).generate();
                Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch != null) {
                    color = swatch.getRgb();
                }
            }
        }

        return color;
    }

    public static class Pref {
        private static final String DEFAULT_TIME_ADD = "10";

        public static int getDefaultReminderMins(Context ctx) {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString(ctx.getString(R.string.default_reminder_time_key), DEFAULT_TIME_ADD));
        }

        public static int getDefaultPostponeMins(Context ctx) {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString(ctx.getString(R.string
                            .default_postpone_time_key),
                    DEFAULT_TIME_ADD));
        }

        public static int getActionOnRejected(Context ctx) {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString(ctx.getString(R.string
                    .action_on_rejected_key), "0"));
        }

        public static int getActionOnMissed(Context ctx) {
            return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString(ctx.getString(R.string
                    .action_on_missed_key), "1"));
        }
    }

    public static class Intents {
        public static Intent createDialerIntent(String phoneNumber) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));

            return intent;
        }

        public static Intent createContactIntent(String contactId) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId));

            return intent;
        }
    }
}
