package com.wantcallback.dao.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wantcallback.dao.DBHelperUtil;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ReminderInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReminderDao {
    public static final String TABLE = ReminderInfo.TABLE;
    private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    //public static final long DATE_MULT = 60 * 1000l;

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
            info.setId(found.getId());
            update(info);
        }
    }

    protected void insert(ReminderInfo info) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = beanToCV(info);

        long newId = db.insert(TABLE, null, cv);
        info.setId(newId);
    }

    protected void update(ReminderInfo info) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = beanToCV(info);

        db.update(TABLE, cv, ReminderInfo.ID + " = ? ", new String[]{String.valueOf(info.getId())});
    }

    public int deleteById(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count = db.delete(TABLE, ReminderInfo.ID + " = ? ", new String[]{String.valueOf(id)});
        return count;
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
        cursor.close();
        return info;
    }

    public ReminderInfo getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ReminderInfo info = null;

        Cursor cursor = db.query(TABLE, null, ReminderInfo.ID + " = ? ", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst()) { // assume phone is a unique field
            info = cursorToBean(cursor);
        }
        cursor.close();
        return info;
    }

    public List<ReminderInfo> getAll() {
        List<ReminderInfo> reminders = new ArrayList<ReminderInfo>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE, null, null, null, null, null, ReminderInfo.DATE + " asc");
        while (cursor.moveToNext()) { // assume phone is a unique field
            reminders.add(cursorToBean(cursor));
        }
        cursor.close();
        return reminders;
    }

    public List<ReminderInfo> getAllSince(Date sinceDate) {
        List<ReminderInfo> reminders = new ArrayList<ReminderInfo>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE, null, ReminderInfo.DATE + " >= ?", new String[]{sdfDate.format(new Date(sinceDate.getTime()))},
                null, null, ReminderInfo.DATE + "" + " desc");
        if (cursor.moveToFirst()) { // assume phone is a unique field
            reminders.add(cursorToBean(cursor));
        }
        cursor.close();
        return reminders;
    }

    public void deleteAllBeforeDate(Date beforeDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(TABLE, ReminderInfo.DATE + " < ?", new String[]{sdfDate.format(new Date(beforeDate.getTime()))});
    }

    private ContentValues beanToCV(ReminderInfo info) {
        ContentValues cv = new ContentValues();
        if (!info.isNew()) {
            cv.put(ReminderInfo.ID, info.getId());
        }
        cv.put(ReminderInfo.PHONE, info.getPhone());
        cv.put(ReminderInfo.DATE, sdfDate.format(new Date(info.getDate())));
        cv.put(ReminderInfo.CALL_ID, info.getCallInfo().getLogId());
        cv.put(ReminderInfo.CALL_DATE, sdfDate.format(new Date(info.getCallInfo().getDate())));
        cv.put(ReminderInfo.CALL_TYPE, info.getCallInfo().getType().toString());

        return cv;
    }

    private ReminderInfo cursorToBean(Cursor cursor) {
        ReminderInfo info = new ReminderInfo();
        info.setId(cursor.getInt(cursor.getColumnIndex(ReminderInfo.ID)));
        try {
            info.setDate(sdfDate.parse(cursor.getString(cursor.getColumnIndex(ReminderInfo.DATE))).getTime());
        } catch (ParseException e) {
            e.printStackTrace();// TODO handle
        }
        info.setPhone(cursor.getString(cursor.getColumnIndex(ReminderInfo.PHONE)));

        CallInfo callInfo = null;
        try {
            callInfo = new CallInfo(cursor.getInt(cursor.getColumnIndex(ReminderInfo.CALL_ID)),
                    cursor.getString(cursor.getColumnIndex(ReminderInfo.PHONE)),
                    sdfDate.parse(cursor.getString(cursor.getColumnIndex(ReminderInfo.CALL_DATE))).getTime(),
                    CallInfo.TYPE.valueOf(cursor.getString(cursor.getColumnIndex(ReminderInfo.CALL_TYPE))));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        info.setCallInfo(callInfo);

        return info;
    }
}
