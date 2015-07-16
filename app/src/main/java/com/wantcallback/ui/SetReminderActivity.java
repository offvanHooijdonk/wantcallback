package com.wantcallback.ui;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.wantcallback.Constants;
import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.data.ContactsUtil;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.model.ContactInfo;
import com.wantcallback.reminder.ReminderUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class SetReminderActivity extends FragmentActivity implements TimePickerDialog.OnTimeSetListener {
	public static final String EXTRA_PHONE = "extra_phone";
	public static final String EXTRA_CALL_ID = "extra_call_id";
	public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
	public static final String EXTRA_NOTIF_ID = "extra_notif_id";

	private static final String TAG_TIME_DIALOG = "tag_time_dialog";

	private SetReminderActivity that;

	private TextView textContactName;
	private ImageView ivPhoto;
	private AutoCompleteTextView inputPhone;
	private TextView textTime;
	private TextView textToday;
	private TextView textHasReminder;
	private Button btnSave;

	private Date remindDate = null;
	private boolean isToday = true;
	private String phoneNumber;
	private int callId;
	private ReminderDao reminderDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_alarm);

		this.that = this;

		reminderDao = new ReminderDao(this);

		inputPhone = (AutoCompleteTextView) findViewById(R.id.inputPhone);
		textContactName = (TextView) findViewById(R.id.textContactName);
		ivPhoto = (ImageView) findViewById(R.id.photo);
		textTime = (TextView) findViewById(R.id.textTime);
		textToday = (TextView) findViewById(R.id.textToday);
		textHasReminder = (TextView) findViewById(R.id.textHasReminder);
		btnSave = (Button) findViewById(R.id.btnSave);
		
		textTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(remindDate);
				TimePickerDialog timePickerDialog = new TimePickerDialog(that, that, calendar.get(Calendar.HOUR_OF_DAY), calendar.get
						(Calendar.MINUTE), DateFormat.is24HourFormat(that));

				timePickerDialog.show();
			}
		});
		
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar now = Calendar.getInstance();
				Calendar picked = Calendar.getInstance();
				picked.setTime(remindDate);
				if (picked.before(now)) {
					// Alert that time already expired and need new time
					AlertDialog.Builder builder = new AlertDialog.Builder(SetReminderActivity.this);
					builder.setTitle(R.string.date_expired_title).setMessage(R.string.date_expired)
							.setPositiveButton(R.string.date_experid_button, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
					builder.show();
				} else {
					ReminderInfo info = new ReminderInfo();
					info.setId(callId);
					info.setPhone(phoneNumber);
					info.setDate(remindDate.getTime());
					
					ReminderUtil.createNewReminder(SetReminderActivity.this, info);
					
					Toast.makeText(SetReminderActivity.this, "Will remind at " + AppHelper.sdfDateTime.format(remindDate), Toast.LENGTH_LONG).show();
					finish();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		// TODO check if application enabled, if not - disable controls and show message

		Intent intent = getIntent();
		if (intent != null) {
			callId = intent.getExtras().getInt(EXTRA_CALL_ID);
			phoneNumber = intent.getExtras().getString(EXTRA_PHONE);
			String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG);
			int id = intent.getExtras().getInt(EXTRA_NOTIF_ID);

			if (tag != null) {
				NotificationsUtil notificationsUtil = new NotificationsUtil(this);
				notificationsUtil.dismissNotification(tag, id);
			}
		}

		if (phoneNumber != null) {
			inputPhone.setText(phoneNumber);
			ContactsUtil contactsUtil = new ContactsUtil(SetReminderActivity.this);
			ContactInfo contactInfo = contactsUtil.findContactByPhone(phoneNumber);
			if (contactInfo != null) {
				textContactName.setVisibility(View.VISIBLE);
				textContactName.setText(contactInfo.getDisplayName());

				Bitmap contactPhoto = null;
				try {
					contactPhoto = contactsUtil.getContactPhoto(contactInfo);
				} catch (IOException e) {
					Toast.makeText(SetReminderActivity.this, "Error getting contact photo" + e.toString(), Toast.LENGTH_LONG).show();
					Log.e(Constants.LOG_TAG, "Error getting contact photo" + e.toString());
				}
				if (contactPhoto != null) {
					ivPhoto.setImageBitmap(contactPhoto);
				} else {
					ivPhoto.setImageResource(R.drawable.ic_contact_picture);
				}
				ivPhoto.setVisibility(View.VISIBLE);

			} else {
				textContactName.setVisibility(View.GONE);
				ivPhoto.setVisibility(View.GONE);
			}
			
			// Check if there is already a reminder for the number and reflect it on the activity screen
			ReminderInfo reminderInfo = reminderDao.findByPhone(phoneNumber);
			if (reminderInfo != null) {
				displayReminderTime(reminderInfo.getDate());
				textHasReminder.setVisibility(View.VISIBLE);
			} else { // if not reminders yet - set default time to the picker
				displayReminderTime(ReminderUtil.calcDeafaultRemindDate(Calendar.getInstance().getTimeInMillis()));
				textHasReminder.setVisibility(View.GONE);
			}

		} else {
			// TODO handle if phone is empty somehow
			btnSave.setEnabled(false);
		}

	}
	
	private void displayReminderTime(long time) {
		Calendar calendarRem = Calendar.getInstance();
		calendarRem.setTimeInMillis(time);
		
		if (isTodayTime(Calendar.getInstance(), calendarRem)) {
			isToday = true;
		} else {
			calendarRem.add(Calendar.DAY_OF_MONTH, 1);
			isToday = false;
		}
		remindDate = calendarRem.getTime();
		
		String timeString = AppHelper.sdfTime.format(remindDate);
		textTime.setText(timeString);
		setTodayText(isToday);
	}

	private boolean isTodayTime(Calendar now, Calendar picked) {
		return picked.after(now);
	}

	private void setTodayText(boolean today) {
		if (today) {
			textToday.setText(this.getResources().getString(R.string.today));
		} else {
			textToday.setText(this.getResources().getString(R.string.tomorrow));
		}
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // assume we have only one time picker dialog
		Calendar calendarPicked = Calendar.getInstance();
		calendarPicked.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendarPicked.set(Calendar.MINUTE, minute);
		calendarPicked.set(Calendar.SECOND, 0);
		calendarPicked.set(Calendar.MILLISECOND, 0);

		displayReminderTime(calendarPicked.getTimeInMillis());
	}
}
