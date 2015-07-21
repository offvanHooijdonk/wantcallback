package com.wantcallback.phone;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

import com.wantcallback.model.ContactInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContactsUtil {

	private Context ctx;
	
	public ContactsUtil(Context context) {
		this.ctx = context;
	}
	
	public ContactInfo findContactByPhone(String phone) {
		ContactInfo info = null;
		if (phone != null && !"".equals(phone)) {
			ContentResolver cr = ctx.getContentResolver();

			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));

			Cursor cur = cr.query(uri, null, null, null, null);

			if (cur.moveToFirst()) {
				info = toSingleContact(cur);
			}
			cur.close();
		}
		return info;
	}

	public ContactInfo getContactFromUri(Uri uri) {
		ContactInfo contact = null;

		Cursor cursor = ctx.getContentResolver()
				.query(uri, null, null, null, null);

		if (cursor.moveToFirst()) {
			contact = toSingleContact(cursor);
		}

		cursor.close();

		return contact;
	}
	
	public Bitmap getContactPhoto(ContactInfo info) throws IOException {
		Bitmap photo;
		ContentResolver cr = ctx.getContentResolver();
		
		InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(cr,
				ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(info.getId())));
		
		if (inputStream != null) {
            photo = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } else {
        	photo = null;
        }

        return photo;
	}

	private ContactInfo toSingleContact(Cursor cur) {
		ContactInfo info = new ContactInfo();
		String id = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup._ID));
		String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		String defaultPhone = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

		String photoString = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
		if (photoString != null && !"".equals(photoString)) {
			Uri photoUri = Uri.parse(photoString);
			info.setPhotoUri(photoUri);
		}

		info.setId(id);
		info.setDisplayName(name);
		info.setPhoneNumber(defaultPhone);

		return info;
	}
}
