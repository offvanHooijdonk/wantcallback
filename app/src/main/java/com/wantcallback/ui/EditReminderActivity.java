package com.wantcallback.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.phone.ContactsUtil;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.model.ContactInfo;
import com.wantcallback.reminder.ReminderUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class EditReminderActivity extends FragmentActivity implements TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_PHONE = "extra_phone";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_CALL_ID = "extra_call_id";
    public static final String EXTRA_CALL_TYPE = "extra_call_type";
    public static final String EXTRA_CALL_DATE = "extra_call_date";
    public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
    public static final String EXTRA_NOTIF_ID = "extra_notif_id";

    private static final int TAG_PICK_CONTACT = 1;

    private EditReminderActivity that;

    private enum MODE {
        EDIT, NEW_PHONE, FROM_SCRATCH
    }

    private MODE mode;

    private TextView textContactName;
    private ImageView ivPhoto;
    private ImageView imageContacts;
    private AutoCompleteTextView inputPhone;
    private TextView textTime;
    private TextView textToday;
    private TextView textHaveReminder;
    private Button btnSave;

    private int reminderId;
    private Date remindDate = null;
    private boolean isToday = true;
    private String phoneNumber;
    private int callId;
    private CallInfo.TYPE callType;
    private long callDate;
    private ReminderDao reminderDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        this.that = this;

        getActionBar().setDisplayHomeAsUpEnabled(true);

        reminderDao = new ReminderDao(this);

        inputPhone = (AutoCompleteTextView) findViewById(R.id.inputPhone);
        textContactName = (TextView) findViewById(R.id.textContactName);
        ivPhoto = (ImageView) findViewById(R.id.photo);
        imageContacts = (ImageView) findViewById(R.id.imageContacts);
        textTime = (TextView) findViewById(R.id.textTime);
        textToday = (TextView) findViewById(R.id.textToday);
        textHaveReminder = (TextView) findViewById(R.id.textHaveReminder);
        btnSave = (Button) findViewById(R.id.btnSave);

        // TODO implement picking a contact
        imageContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
                startActivityForResult(pickContactIntent, TAG_PICK_CONTACT);
            }
        });

        textTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(remindDate);
                TimePickerDialog timePickerDialog = new TimePickerDialog(that, that, calendar.get(Calendar.HOUR_OF_DAY), calendar.get
                        (Calendar.MINUTE), DateFormat.is24HourFormat(that));

                timePickerDialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                Calendar picked = Calendar.getInstance();
                picked.setTime(remindDate);
                if (picked.before(now)) {
                    // Alert that time already expired and need new time
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditReminderActivity.this);
                    builder.setTitle(R.string.date_expired_title).setMessage(R.string.date_expired)
                            .setPositiveButton(R.string.date_experid_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                } else {
                    ReminderInfo info = new ReminderInfo();
                    if (mode == MODE.EDIT) {
                        info.setId(reminderId);
                    }
                    info.setPhone(phoneNumber);
                    info.setDate(remindDate.getTime());
                    info.setCallInfo(new CallInfo(callId, phoneNumber, callDate, callType));
                    // TODO move to task
                    ReminderUtil.createNewReminder(EditReminderActivity.this, info);

                    Toast.makeText(EditReminderActivity.this, "Will remind at " + AppHelper.sdfDateTime.format(remindDate), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        initForm();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAG_PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                ContactsUtil util = new ContactsUtil(that);
                ContactInfo contact = util.getContactFromUri(uri);

                mode = MODE.NEW_PHONE;
                inputPhone.setText(contact.getPhoneNumber());
                setReminderTime(ReminderUtil.calcDefaultRemindDate(new Date().getTime()));
                // check if
                fillContactInfo(contact);
            }
        }
    }

    private void fillContactInfo(ContactInfo contact) {
        if (contact != null) {
            textContactName.setVisibility(View.VISIBLE);
            textContactName.setText(contact.getDisplayName());

            if (contact.getPhotoUri() != null) {
                ivPhoto.setImageURI(contact.getPhotoUri());
            } else {
                ivPhoto.setImageResource(R.drawable.ic_contact_picture);
            }
            ivPhoto.setVisibility(View.VISIBLE);

            // Check if there is already a reminder for the number and reflect it on the activity screen
            ReminderInfo reminderInfo = reminderDao.findByPhone(contact.getPhoneNumber());
            if (reminderInfo != null) {
                setReminderTime(reminderInfo.getDate());
                textHaveReminder.setVisibility(View.VISIBLE);
            } else { // if not reminders yet - set default time to the picker
                setReminderTime(ReminderUtil.calcDefaultRemindDate(new Date().getTime()));
                textHaveReminder.setVisibility(View.GONE);
            }
        } else {
            inputPhone.setText(null);

            textContactName.setVisibility(View.GONE);
            textContactName.setText(null);

            ivPhoto.setImageResource(R.drawable.ic_contact_picture);
            ivPhoto.setVisibility(View.GONE);
        }
    }

    private void displayReminder(ReminderInfo info) {
        if (mode == MODE.EDIT || mode == MODE.NEW_PHONE) {
            if (mode == MODE.EDIT) {
                textHaveReminder.setVisibility(View.VISIBLE);
                setReminderTime(info.getDate());
            } else if (mode == MODE.NEW_PHONE) {
                textHaveReminder.setVisibility(View.GONE);
                setReminderTime(ReminderUtil.calcDefaultRemindDate(Calendar.getInstance().getTime().getTime()));
            }
            inputPhone.setText(info.getPhone());
            imageContacts.setVisibility(View.GONE);
            inputPhone.setEnabled(false);

            ContactsUtil util = new ContactsUtil(that);
            ContactInfo contact = util.findContactByPhone(info.getPhone());

            fillContactInfo(contact);
        } else if (mode == MODE.FROM_SCRATCH) {
            textHaveReminder.setVisibility(View.GONE);
            inputPhone.setEnabled(true);
            imageContacts.setVisibility(View.VISIBLE);
        }

    }

    private void initForm() {
        // TODO check if application enabled, if not - disable controls and show message

        Intent intent = getIntent();
        ReminderInfo reminderInfo = null;
        if (intent.getExtras() != null) {
            if (intent.getExtras().containsKey(EXTRA_REMINDER_ID)) { // we are editing reminder
                mode = MODE.EDIT;
                reminderId = intent.getExtras().getInt(EXTRA_REMINDER_ID);
                reminderInfo = reminderDao.getById(reminderId);
            } else { // some data must come
                callId = intent.getExtras().getInt(EXTRA_CALL_ID, 0);
                callType = CallInfo.TYPE.valueOf(intent.getExtras().getString(EXTRA_CALL_TYPE, CallInfo.TYPE.CREATED.toString()));
                callDate = intent.getExtras().getLong(EXTRA_CALL_DATE, new Date().getTime());
                phoneNumber = intent.getExtras().getString(EXTRA_PHONE, null);
                String tag = intent.getExtras().getString(EXTRA_NOTIF_TAG, null);
                int id = intent.getExtras().getInt(EXTRA_NOTIF_ID);

                if (tag != null) {
                    NotificationsUtil notificationsUtil = new NotificationsUtil(this);
                    notificationsUtil.dismissNotification(tag, id);
                }

                if (phoneNumber != null) {
                    // check if there is already a reminder for that number
                    reminderInfo = reminderDao.findByPhone(phoneNumber);
                    if (reminderInfo == null) { // no reminder so far, we just display info on phone/contact
                        mode = MODE.NEW_PHONE;
                        reminderInfo = new ReminderInfo();
                        reminderInfo.setCallInfo(new CallInfo(callId, phoneNumber, callDate, callType));
                    } else {
                        mode = MODE.EDIT;
                    }
                    reminderId = reminderInfo.getId();
                } else {
                    mode = MODE.FROM_SCRATCH;
                }
            }
        } else {
            mode = MODE.FROM_SCRATCH;
        }

        displayReminder(reminderInfo);
    }

    private void setReminderTime(long time) {
        Calendar calendarRem = Calendar.getInstance();
        calendarRem.setTimeInMillis(time);

        if (isTodayTime(Calendar.getInstance(), calendarRem)) {
            isToday = true;
        } else {
            calendarRem.add(Calendar.DAY_OF_MONTH, 1);
            isToday = false;
        }
        remindDate = calendarRem.getTime();

        String timeString = AppHelper.sdfTime.format(remindDate);
        textTime.setText(timeString);
        setTodayText(isToday);
    }

    private boolean isTodayTime(Calendar now, Calendar picked) {
        return picked.after(now);
    }

    private void setTodayText(boolean today) {
        if (today) {
            textToday.setText(this.getResources().getString(R.string.today));
        } else {
            textToday.setText(this.getResources().getString(R.string.tomorrow));
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // assume we have only one time picker dialog
        Calendar calendarPicked = Calendar.getInstance();
        calendarPicked.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendarPicked.set(Calendar.MINUTE, minute);
        calendarPicked.set(Calendar.SECOND, 0);
        calendarPicked.set(Calendar.MILLISECOND, 0);

        setReminderTime(calendarPicked.getTimeInMillis());
    }
}
