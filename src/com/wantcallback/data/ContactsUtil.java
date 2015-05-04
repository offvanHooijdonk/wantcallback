package com.wantcallback.data;

import com.wantcallback.observer.model.ContactInfo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

public class ContactsUtil {

	private Context ctx;
	
	public ContactsUtil(Context context) {
		this.ctx = context;
	}
	
	public ContactInfo findContactByPhone(String phone) {
		ContactInfo info = null;
		ContentResolver cr = ctx.getContentResolver();
		
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
		
		Cursor cur = cr.query(uri, null, null, null, null);
		
		if (cur.moveToFirst()) {
			String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            
            info = new ContactInfo();
            info.setId(id);
            info.setDisplayName(name);
		}
		
		return info;
	}
}
