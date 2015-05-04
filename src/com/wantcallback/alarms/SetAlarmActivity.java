package com.wantcallback.alarms;

import com.wantcallback.R;
import com.wantcallback.data.ContactsUtil;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.model.ContactInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class SetAlarmActivity extends Activity {
	public static final String EXTRA_PHONE = "extra_phone";
	public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
	public static final String EXTRA_NOTIF_ID = "extra_notif_id";

	private EditText etPhoneNumber;
	private TextView textContactInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_alarm);
		
		etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
		textContactInfo = (TextView) findViewById(R.id.textContactInfo);
	}
	
	@Override
	protected void onResume() {
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
			etPhoneNumber.setText(phone);
			ContactsUtil contactsUtil = new ContactsUtil(this);
			ContactInfo contactInfo = contactsUtil.findContactByPhone(phone);
			if (contactInfo != null) {
				textContactInfo.setText("ID: " + contactInfo.getId() + ", Name: " + contactInfo.getDisplayName());
			}
		}
		
		super.onStart();
	}
}
