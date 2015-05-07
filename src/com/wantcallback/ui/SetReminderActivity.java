package com.wantcallback.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.wantcallback.Constants;
import com.wantcallback.R;
import com.wantcallback.data.ContactsUtil;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.model.ContactInfo;
import com.wantcallback.reminder.ReminderUtil;

public class SetReminderActivity extends FragmentActivity implements RadialTimePickerDialog.OnTimeSetListener {
	public static final String EXTRA_PHONE = "extra_phone";
	public static final String EXTRA_CALL_ID = "extra_call_id";
	public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
	public static final String EXTRA_NOTIF_ID = "extra_notif_id";
	
	private static final String TAG_TIME_DIALOG = "tag_time_dialog";
	private static final int DEFAULT_TIME_ADD = 10;

	// private EditText etPhoneNumber;
	private TextView textContactName;
	private ImageView ivPhoto;
	private AutoCompleteTextView inputPhone;
	private TextView textTime;
	private TextView textToday;
	private Button btnSave;
	
	private static DateFormat sdfTime = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
	private static DateFormat sdfDateTime = SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);

	private Date remindDate = null;
	private boolean isToday = true;
	private String phoneNumber;
	private int callId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_alarm);

		// etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
		inputPhone = (AutoCompleteTextView) findViewById(R.id.inputPhone);
		textContactName = (TextView) findViewById(R.id.textContactName);
		ivPhoto = (ImageView) findViewById(R.id.photo);
		textTime = (TextView) findViewById(R.id.textTime);
		textToday = (TextView) findViewById(R.id.textToday);
		btnSave = (Button) findViewById(R.id.btnSave);

		/* Time */
		initRemindTime();
		
		textTime.setText(sdfTime.format(remindDate));
		setTodayText(isToday);
		
		textTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(remindDate);
				RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                        .newInstance(SetReminderActivity.this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                        		android.text.format.DateFormat.is24HourFormat(SetReminderActivity.this));
				timePickerDialog.show(getSupportFragmentManager(), TAG_TIME_DIALOG);
			}
		});
		
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar now = Calendar.getInstance();
				Calendar picked = Calendar.getInstance();
				picked.setTime(remindDate);
				if (picked.before(now)) {
					// TODO Alert that time already expired and need new time
					Dialog dialog = new Dialog(SetReminderActivity.this);
					dialog.setContentView(R.layout.dialog_date_expired);
					dialog.setCancelable(true);
					dialog.show();
				} else {
					ReminderUtil.createNewReminder(SetReminderActivity.this, callId, phoneNumber, remindDate);
					
					Toast.makeText(SetReminderActivity.this, "Will remind at " + sdfDateTime.format(remindDate), Toast.LENGTH_LONG).show();
					finish();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

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

		} else {
			// TODO handle if phone is empty somehow
			btnSave.setEnabled(false);
		}

	}

	@Override
	public void onTimeSet(RadialTimePickerDialog dialog, int hourOfDay, int minute) {
		if (dialog.getTag().equals(TAG_TIME_DIALOG)) {
			Calendar calendarPicked = Calendar.getInstance();
			calendarPicked.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendarPicked.set(Calendar.MINUTE, minute);
			calendarPicked.set(Calendar.SECOND, 0);
			calendarPicked.set(Calendar.MILLISECOND, 0);
			
			if (isTodayTime(Calendar.getInstance(), calendarPicked)) {
				remindDate = calendarPicked.getTime();
				isToday = true;
			} else {
				calendarPicked.roll(Calendar.DAY_OF_MONTH, 1);
				remindDate = calendarPicked.getTime();
				isToday = false;
			}
			
			String timeString = sdfTime.format(remindDate);
			textTime.setText(timeString);
			setTodayText(isToday);
			
		}
	}
	
	private boolean isTodayTime(Calendar now, Calendar picked) {
		return picked.after(now);
	}

	private void initRemindTime() {
		Calendar calendar = Calendar.getInstance();
		int todayDayNum = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.roll(Calendar.MINUTE, DEFAULT_TIME_ADD);
		remindDate = calendar.getTime();
		// if day changed - we moved to the next day
		isToday = todayDayNum == calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	private void setTodayText(boolean today) {
		if (today) {
			textToday.setText(this.getResources().getString(R.string.today));
		} else {
			textToday.setText(this.getResources().getString(R.string.tomorrow));
		}
	}
}
