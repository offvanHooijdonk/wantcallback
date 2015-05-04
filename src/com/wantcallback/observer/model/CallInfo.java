package com.wantcallback.observer.model;

public class CallInfo {
	public static final long INVALID_DATE = -1L;

	public static enum TYPE {
		MISSED, REJECTED
	};

	private int id;
	private String phone;
	private long date;
	private TYPE type;

	public CallInfo(int id, String phone, long date, TYPE type) {
		this.id = id;
		this.phone = phone;
		this.date = date;
		this.type = type;
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

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

}
