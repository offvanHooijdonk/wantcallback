package com.wantcallback.ui;

import com.wantcallback.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RemindActivity extends Activity {
	public static final String EXTRA_PHONE = "extra_phone";
	
	private TextView textPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reminder);
		
		textPhone = (TextView) findViewById(R.id.textPhone);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		String phoneNumber = getIntent().getExtras().getString(EXTRA_PHONE);
		
		if (phoneNumber != null) {
			textPhone.setText(phoneNumber);
		} else {
			textPhone.setText("-");
		}
	}
}
