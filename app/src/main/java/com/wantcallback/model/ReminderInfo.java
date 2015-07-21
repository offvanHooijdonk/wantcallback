package com.wantcallback.model;

public class ReminderInfo {
	public static final String TABLE = "reminders";

	private static final int BLANK_ID = -1;

	public static final String ID = "_id";
	public static final String PHONE = "phone";
	public static final String DATE = "remind_date";
	public static final String CALL_ID = "call_id";
	public static final String CALL_DATE = "call_date";
	public static final String CALL_TYPE = "call_type";

	private int id = BLANK_ID;
	private String phone;
	private long date;
	private CallInfo callInfo;

	public boolean isNew() {
		return getId() == BLANK_ID;
	}

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
