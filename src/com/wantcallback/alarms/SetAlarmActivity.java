package com.wantcallback.alarms;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wantcallback.R;
import com.wantcallback.data.ContactsUtil;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.observer.model.ContactInfo;

public class SetAlarmActivity extends Activity {
	public static final String EXTRA_PHONE = "extra_phone";
	public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
	public static final String EXTRA_NOTIF_ID = "extra_notif_id";

	private EditText etPhoneNumber;
	private TextView textContactName;
	private ImageView ivPhoto;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_alarm);
		
		etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
		textContactName = (TextView) findViewById(R.id.textContactName);
		ivPhoto = (ImageView) findViewById(R.id.photo);
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
				textContactName.setVisibility(View.VISIBLE);
				textContactName.setText(contactInfo.getDisplayName());
				
				Bitmap contactPhoto = null;
				try {
					contactPhoto = contactsUtil.getContactPhoto(contactInfo);
				} catch (IOException e) {
					Toast.makeText(SetAlarmActivity.this, "Error getting contact photo" + e.toString(), Toast.LENGTH_LONG).show();
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
		
		super.onStart();
	}
}
