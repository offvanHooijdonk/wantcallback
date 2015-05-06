package com.wantcallback.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;
import com.wantcallback.Constants;
import com.wantcallback.R;
import com.wantcallback.data.ContactsUtil;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.model.ContactInfo;

public class SetAlarmActivity extends FragmentActivity implements TimePickerDialogFragment.TimePickerDialogHandler {
	public static final String EXTRA_PHONE = "extra_phone";
	public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
	public static final String EXTRA_NOTIF_ID = "extra_notif_id";
	
	private static final int REF_REMINDER_TIME = 1;

	// private EditText etPhoneNumber;
	private TextView textContactName;
	private ImageView ivPhoto;
	private AutoCompleteTextView inputPhone;
	private TextView textTime;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

	private Date remindDate = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_alarm);

		// etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
		textContactName = (TextView) findViewById(R.id.textContactName);
		ivPhoto = (ImageView) findViewById(R.id.photo);

		/* Time */
		// TODO init date with default +10m
		textTime = (TextView) findViewById(R.id.textTime);
		textTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerBuilder builder = new TimePickerBuilder().setFragmentManager(getSupportFragmentManager()).setReference(REF_REMINDER_TIME);
				builder.show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		String phone = null;

		Intent intent = getIntent();
		if (intent != null) {
			phone = intent.getExtras().getString(EXTRA_PHONE);
			String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG);
			int id = intent.getExtras().getInt(EXTRA_NOTIF_ID);

			NotificationsUtil notificationsUtil = new NotificationsUtil(this);
			notificationsUtil.dismissNotification(tag, id);
		}

		if (phone != null) {
			inputPhone.setText(phone);
			ContactsUtil contactsUtil = new ContactsUtil(SetAlarmActivity.this);
			ContactInfo contactInfo = contactsUtil.findContactByPhone(phone);
			if (contactInfo != null) {
				textContactName.setVisibility(View.VISIBLE);
				textContactName.setText(contactInfo.getDisplayName());

				Bitmap contactPhoto = null;
				try {
					contactPhoto = contactsUtil.getContactPhoto(contactInfo);
				} catch (IOException e) {
					Toast.makeText(SetAlarmActivity.this, "Error getting contact photo" + e.toString(), Toast.LENGTH_LONG).show();
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

		}

	}

	@Override
	public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
		if (reference == REF_REMINDER_TIME) {
			// TODO see if this is today or tomorrow
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
			
			remindDate = calendar.getTime();
			
			String timeString = sdf.format(remindDate);
			textTime.setText(timeString);
			
			// TODO create an alarm
		}
	}

}
