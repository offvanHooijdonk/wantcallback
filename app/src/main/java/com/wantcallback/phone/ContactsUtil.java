package com.wantcallback.phone;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

import com.wantcallback.model.ContactInfo;

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
				info = fromPhoneCursorToContact(cur);
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
			contact = fromContactCursorToContact(cursor);
		}

		cursor.close();

		return contact;
	}
	
	/*public Bitmap getContactPhoto(ContactInfo info) throws IOException {
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
	}*/

	private ContactInfo fromPhoneCursorToContact(Cursor cur) {
		ContactInfo info = new ContactInfo();
		String id = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup._ID));
		String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		String defaultPhone = cur.getString(cur.getColumnIndex(PhoneLookup.NUMBER));

		String photoString = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
		String thumbString = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
		if (photoString != null && !"".equals(photoString)) {
			Uri photoUri = Uri.parse(photoString);
			info.setPhotoUri(photoUri);
		}
		if (thumbString != null && !"".equals(thumbString)) {
			Uri thumbUri = Uri.parse(thumbString);
			info.setThumbUri(thumbUri);
		}

		info.setId(id);
		info.setDisplayName(name);
		info.setPhoneNumber(defaultPhone);

		return info;
	}

	private ContactInfo fromContactCursorToContact(Cursor cur) {
		ContactInfo info = new ContactInfo();
		String id = cur.getString(cur.getColumnIndex(ContactsContract.Data.CONTACT_ID));
		String name = cur.getString(cur.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
		String defaultPhone = cur.getString(cur.getColumnIndex(ContactsContract.Data.DATA1));

		String photoString = cur.getString(cur.getColumnIndex(ContactsContract.Data.PHOTO_URI));
		String thumbString = cur.getString(cur.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI));
		if (photoString != null && !"".equals(photoString)) {
			Uri photoUri = Uri.parse(photoString);
			info.setPhotoUri(photoUri);
		}
		if (thumbString != null && !"".equals(thumbString)) {
			Uri thumbUri = Uri.parse(thumbString);
			info.setThumbUri(thumbUri);
		}

		info.setId(id);
		info.setDisplayName(name);
		info.setPhoneNumber(defaultPhone);

		return info;
	}
}
