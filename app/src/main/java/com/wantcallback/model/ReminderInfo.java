package com.wantcallback.model;

public class ReminderInfo {
	public static final String TABLE = "reminders";

	public static final String ID = "_id";
	public static final String PHONE = "phone";
	public static final String DATE = "remind_date";
	public static final String CALL_ID = "call_id";
	public static final String CALL_DATE = "call_date";
	public static final String CALL_TYPE = "call_type";

	private int id;
	private String phone;
	private long date;
	private CallInfo callInfo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public CallInfo getCallInfo() {
		return callInfo;
	}

	public void setCallInfo(CallInfo callInfo) {
		this.callInfo = callInfo;
	}
}
