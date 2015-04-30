package com.wantcallback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.wantcallback.observer.CallLogObserver;
import com.wantcallback.observer.OnCallMissRejectListener;
import com.wantcallback.observer.impl.StandardMissRejectListener;

public class MainActivity extends Activity {
	
	private MainActivity that;
	private Button btnStartService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		that = this;
		
		btnStartService = (Button) findViewById(R.id.btnStartService);
		
		initCallObserver();
		
		btnStartService.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO disabled to test without starting service
				/*Intent intent = new Intent(that, CallHandlerService.class);
				that.startService(intent);*/
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initCallObserver() {
		OnCallMissRejectListener listener = new StandardMissRejectListener(that);
		
		CallLogObserver callLogObserver = new CallLogObserver(new Handler(), that);
		callLogObserver.addListener(listener);
		
		that.getApplicationContext().getContentResolver()
				.registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, callLogObserver);

		Log.i(Constants.LOG_TAG, "Call observer initialized.");
	}
}
