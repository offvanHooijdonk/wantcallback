package com.wantcallback.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wantcallback.Constants;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.ReminderInfo;

public class DBHelperUtil extends SQLiteOpenHelper {
	private static final String DB_NAME = "wantcallback_db";
	
	public DBHelperUtil(Context context) {
		super(context, DB_NAME, null, 3);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String q = "CREATE TABLE " + ReminderDao.TABLE + " (" + ReminderInfo.ID + 
				" integer primary key AUTOINCREMENT, " + ReminderInfo.PHONE + " text not null unique, " + ReminderInfo.DATE + " integer, " +
				ReminderInfo.CALL_ID + " integer, " + ReminderInfo.CALL_DATE + " integer, " + ReminderInfo.CALL_TYPE + " text " + ");";
		try {
			db.execSQL(q);
		} catch (Exception e) {
			Log.e(Constants.LOG_TAG, "Error creating DB.", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String dropTables = "DROP TABLE reminders;";
		try {
			String[] dropQueries = dropTables.split(";");
			for (String q : dropQueries) {
				db.execSQL(q);
			}
			
		} catch(Exception e) {
			Log.w(Constants.LOG_TAG, e);
		}
		onCreate(db);
	}

}
