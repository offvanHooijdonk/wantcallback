package com.wantcallback.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wantcallback.dao.DBHelperUtil;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ReminderInfo;

import java.util.ArrayList;
import java.util.List;

public class ReminderDao {
    public static final String TABLE = ReminderInfo.TABLE;
    public static final int DATE_MULT = 60 * 1000;

    private Context ctx;
    private DBHelperUtil dbHelper;

    public ReminderDao(Context context) {
        this.ctx = context;
        dbHelper = new DBHelperUtil(ctx);
    }

    public void save(ReminderInfo info) {
        ReminderInfo found = findByPhone(info.getPhone());
        if (found == null) {
            insert(info);
        } else {
            update(info);
        }
    }

    protected void insert(ReminderInfo info) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = beanToCV(info);

        db.insert(TABLE, null, cv);
    }

    protected void update(ReminderInfo info) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = beanToCV(info);

        db.update(TABLE, cv, ReminderInfo.ID + " = ? ", new String[]{String.valueOf(info.getId())});
    }

    public int deleteByPhone(String phoneNumber) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count = db.delete(TABLE, ReminderInfo.PHONE + " = ? ", new String[]{phoneNumber});
        return count;
    }

    public ReminderInfo findByPhone(String phoneNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ReminderInfo info = null;

        Cursor cursor = db.query(TABLE, null, ReminderInfo.PHONE + " = ? ", new String[]{phoneNumber}, null, null, null);
        if (cursor.moveToFirst()) { // assume phone is a unique field
            info = cursorToBean(cursor);
        }

        return info;
    }

    public List<ReminderInfo> getAll() {
        List<ReminderInfo> reminders = new ArrayList<ReminderInfo>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE, null, null, null, null, null, ReminderInfo.DATE + " desc");
        if (cursor.moveToFirst()) { // assume phone is a unique field
            reminders.add(cursorToBean(cursor));
        }

        return reminders;
    }

    private ContentValues beanToCV(ReminderInfo info) {
        ContentValues cv = new ContentValues();
        cv.put(ReminderInfo.ID, info.getId());
        cv.put(ReminderInfo.PHONE, info.getPhone());
        cv.put(ReminderInfo.DATE, (int) (info.getDate() / DATE_MULT));
        cv.put(ReminderInfo.CALL_ID, info.getCallInfo().getLogId());
        cv.put(ReminderInfo.CALL_DATE, info.getPhone());
        cv.put(ReminderInfo.CALL_TYPE, info.getCallInfo().getType().toString());

        return cv;
    }

    private ReminderInfo cursorToBean(Cursor cursor) {
        ReminderInfo info = new ReminderInfo();
        info.setId(cursor.getInt(cursor.getColumnIndex(ReminderInfo.ID)));
        info.setDate(cursor.getInt(cursor.getColumnIndex(ReminderInfo.DATE)) * DATE_MULT);
        info.setPhone(cursor.getString(cursor.getColumnIndex(ReminderInfo.PHONE)));

        CallInfo callInfo = new CallInfo(cursor.getInt(cursor.getColumnIndex(ReminderInfo.CALL_ID)),
                cursor.getString(cursor.getColumnIndex(ReminderInfo.PHONE)),
                cursor.getInt(cursor.getColumnIndex(ReminderInfo.CALL_DATE)) * DATE_MULT,
                CallInfo.TYPE.valueOf(cursor.getString(cursor.getColumnIndex(ReminderInfo.CALL_TYPE))));

        info.setCallInfo(callInfo);

        return info;
    }
}
