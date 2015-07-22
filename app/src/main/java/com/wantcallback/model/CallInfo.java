package com.wantcallback.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CallInfo implements Parcelable {
	public static final long INVALID_DATE = -1L;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(logId);
		dest.writeString(phone);
		dest.writeLong(date);
		dest.writeString(type.toString());
	}

	public enum TYPE {
		MISSED, REJECTED, CREATED
	};

	public CallInfo() {}

	public CallInfo(Parcel in) {
		setLogId(in.readInt());
		setPhone(in.readString());
		setDate(in.readLong());
		setType(TYPE.valueOf(in.readString()));
	}

	private int logId;
	private String phone;
	private long date;
	private TYPE type;

	public CallInfo(int id, String phone, long date, TYPE type) {
		this.logId = id;
		this.phone = phone;
		this.date = date;
		this.type = type;
	}

	public int getLogId() {
		return logId;
	}

	public void setLogId(int id) {
		this.logId = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public static final Parcelable.Creator<CallInfo> CREATOR = new Parcelable.Creator<CallInfo>() {
		public CallInfo createFromParcel(Parcel in) {
			return new CallInfo(in);
		}

		public CallInfo[] newArray(int size) {
			return new CallInfo[size];
		}
	};
}
