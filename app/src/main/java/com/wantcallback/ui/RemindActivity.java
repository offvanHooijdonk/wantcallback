package com.wantcallback.ui;

import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.ReminderInfo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

@Deprecated
public class RemindActivity extends Activity {
	public static final String EXTRA_PHONE = "extra_phone";
	ReminderDao reminderDao;
	
	private TextView textPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reminder);
		
		reminderDao = new ReminderDao(this);
		textPhone = (TextView) findViewById(R.id.textPhone);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		String phoneNumber = getIntent().getExtras().getString(EXTRA_PHONE);
		
		if (phoneNumber != null) {
			ReminderInfo reminder = reminderDao.findByPhone(phoneNumber);
			if (reminder != null) {
				reminderDao.deleteByPhone(phoneNumber);
				textPhone.setText(reminder.getPhone());
			}
		} else {
			textPhone.setText("-");
		}
	}
}
