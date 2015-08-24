package com.wantcallback.model;

import android.net.Uri;
import android.text.TextUtils;

public class ContactInfo {

	private String id;
	private String displayName;
	private String phoneNumber;
	private Uri photoUri;
	private Uri thumbUri;
	private String lookupKey;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Uri getPhotoUri() {
		return photoUri;
	}

	public void setPhotoUri(Uri photoUri) {
		this.photoUri = photoUri;
	}

	public Uri getThumbUri() {
		return thumbUri;
	}

	public void setThumbUri(Uri thumbUri) {
		this.thumbUri = thumbUri;
	}

	public String pickIdentifier() {
		return !TextUtils.isEmpty(lookupKey) ? lookupKey : !TextUtils.isEmpty(id) ? id : phoneNumber;
	}

	public String getLookupKey() {
		return lookupKey;
	}

	public void setLookupKey(String lookupKey) {
		this.lookupKey = lookupKey;
	}
}
