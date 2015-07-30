package com.wantcallback.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ContactInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.phone.ContactsUtil;
import com.wantcallback.reminder.ReminderUtil;

import java.util.Calendar;
import java.util.Date;

public class EditReminderActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_CALL_INFO = "extra_call_info";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
    public static final String EXTRA_NOTIF_ID = "extra_notif_id";

    private static final int TAG_PICK_CONTACT = 1;

    private EditReminderActivity that;

    public enum MODE {
        EDIT, CREATE, BLANK
    }

    private MODE mode;

    private TextView textContactName;
    private ImageView ivPhoto;
    private AutoCompleteTextView inputPhone;
    private TextView textTime;
    private TextView textToday;
    private TextView textHaveReminder;
    private FloatingActionButton btnSave;
    private FloatingActionButton buttonPickContact;

    private long reminderId;
    private Date remindDate = null;
    private ReminderDao reminderDao;
    private CallInfo callInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        this.that = this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        reminderDao = new ReminderDao(this);

        inputPhone = (AutoCompleteTextView) findViewById(R.id.inputPhone);
        textContactName = (TextView) findViewById(R.id.textContactName);
        ivPhoto = (ImageView) findViewById(R.id.photo);
        buttonPickContact = (FloatingActionButton) findViewById(R.id.buttonPickUser);
        textTime = (TextView) findViewById(R.id.textTime);
        textToday = (TextView) findViewById(R.id.textToday);
        textHaveReminder = (TextView) findViewById(R.id.textHaveReminder);
        btnSave = (FloatingActionButton) findViewById(R.id.btnSave);

        // TODO change icon to rounded and colored
        buttonPickContact.setOnClickListener(new View.OnClickListener() {
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

                if (validateForm()) {
                    ReminderInfo info = new ReminderInfo();
                    if (mode == MODE.EDIT) {
                        info.setId(reminderId);
                    } else if (mode == MODE.BLANK) {
                        callInfo.setDate(Calendar.getInstance().getTimeInMillis());
                    }
                    info.setPhone(inputPhone.getText().toString());
                    info.setDate(remindDate.getTime());
                    info.setCallInfo(callInfo);
                    // TODO move to task
                    ReminderUtil.createNewReminder(EditReminderActivity.this, info);

                    Toast.makeText(EditReminderActivity.this, "Will remind at " + AppHelper.getDateFormat(that).format(remindDate), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        initForm();
    }

    private boolean validateForm() {
        boolean isValid = true;

        Calendar now = Calendar.getInstance();
        Calendar picked = Calendar.getInstance();
        picked.setTime(remindDate);

        if (inputPhone.getText().toString().trim().equals("")) {
            isValid = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(EditReminderActivity.this);
            builder.setTitle(R.string.phone_empty_title).setMessage(R.string.phone_empty)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else if (picked.before(now)) {
            isValid = false;
            // Alert that time already expired and need new time
            AlertDialog.Builder builder = new AlertDialog.Builder(EditReminderActivity.this);
            builder.setTitle(R.string.date_expired_title).setMessage(R.string.date_expired)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }

        return isValid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAG_PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                ContactsUtil util = new ContactsUtil(that);
                ContactInfo contact = util.getContactFromUri(uri);

                setReminderTime(ReminderUtil.calcDefaultRemindDate(that, Calendar.getInstance().getTimeInMillis()), true);
                // check if
                if (contact != null) {
                    ReminderInfo reminderInfo = reminderDao.findByPhone(contact.getPhoneNumber());
                    if (reminderInfo != null) {
                        setReminderTime(reminderInfo.getDate(), false);
                        textHaveReminder.setVisibility(View.VISIBLE);
                    } else {
                        textHaveReminder.setVisibility(View.GONE);
                    }
                    // TODO move phone conversion to a separate class
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        inputPhone.setText(PhoneNumberUtils.formatNumber(contact.getPhoneNumber(), that.getResources().getConfiguration()
                                .locale.getISO3Country()));
                    } else {
                        inputPhone.setText(PhoneNumberUtils.formatNumber(contact.getPhoneNumber()));
                    }
                } else {
                    Toast.makeText(that, "Could not get contact :(", Toast.LENGTH_LONG).show();
                    inputPhone.setText(null);
                }
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
            // TODO make this a link to the contact
        } else {
            textContactName.setVisibility(View.GONE);
            textContactName.setText(null);

            ivPhoto.setImageResource(R.drawable.ic_contact_picture);
            ivPhoto.setVisibility(View.GONE);
        }
    }

    private void displayReminder(ReminderInfo reminder) {
        if (mode == MODE.EDIT || mode == MODE.CREATE) {
            if (mode == MODE.EDIT) {
                textHaveReminder.setVisibility(View.GONE);
                inputPhone.setText(reminder.getPhone());
                // FIXME somehow in this case time always appears Tomorrow
                setReminderTime(reminder.getDate(), false);
            } else if (mode == MODE.CREATE) {
                textHaveReminder.setVisibility(View.GONE);
                inputPhone.setText(reminder.getCallInfo().getPhone());
                setReminderTime(ReminderUtil.calcDefaultRemindDate(that, Calendar.getInstance().getTimeInMillis()), true);
            }

            buttonPickContact.setVisibility(View.GONE);
            inputPhone.setFocusable(false);

            ContactsUtil util = new ContactsUtil(that);
            ContactInfo contact = util.findContactByPhone(reminder.getPhone());

            fillContactInfo(contact);
        } else if (mode == MODE.BLANK) {
            textHaveReminder.setVisibility(View.GONE);
            inputPhone.setFocusable(true);
            buttonPickContact.setVisibility(View.VISIBLE);

            setReminderTime(ReminderUtil.calcDefaultRemindDate(that, Calendar.getInstance().getTimeInMillis()), true);
        }

    }

    private void initForm() {
        // TODO check if application enabled, if not - disable controls and show message

        Intent intent = getIntent();
        ReminderInfo reminderInfo = null;
        String notifTag = null;
        int notifId = -1;
        if (intent.getExtras() != null) {
            mode = MODE.valueOf(intent.getExtras().getString(EXTRA_MODE));

            if (mode == MODE.EDIT) {
                notifTag = intent.getExtras().getString(EXTRA_NOTIF_TAG, null);
                notifId = intent.getExtras().getInt(EXTRA_NOTIF_ID);

                reminderId = intent.getExtras().getLong(EXTRA_REMINDER_ID);
                reminderInfo = reminderDao.getById(reminderId);

                if (reminderInfo != null) {
                    callInfo = reminderInfo.getCallInfo();
                } else {
                    // TODO handle this case
                    mode = MODE.BLANK;
                }
            } else if (mode == MODE.CREATE) {
                callInfo = intent.getExtras().getParcelable(EXTRA_CALL_INFO);

                notifTag = intent.getExtras().getString(EXTRA_NOTIF_TAG, null);
                notifId = intent.getExtras().getInt(EXTRA_NOTIF_ID);

                reminderInfo = new ReminderInfo();
                reminderInfo.setCallInfo(callInfo);
            } else if (mode == MODE.BLANK) {
                callInfo = new CallInfo();
                callInfo.setType(CallInfo.TYPE.CREATED);
            }
        }

        if (notifTag != null) {
            NotificationsUtil notificationsUtil = new NotificationsUtil(this);
            notificationsUtil.dismissNotification(notifTag, notifId);
        }

        displayReminder(reminderInfo);
    }

    private void setReminderTime(long time, boolean calculate) {
        Calendar calendarRem = Calendar.getInstance();
        calendarRem.setTimeInMillis(time);
        Calendar now = Calendar.getInstance();

        if (! isFutureTime(now, calendarRem) && calculate) {
            calendarRem.add(Calendar.DAY_OF_MONTH, 1);
        }
        remindDate = calendarRem.getTime();

        String timeString = AppHelper.getTimeFormat(that).format(remindDate);
        textTime.setText(timeString);
        setDateText(now, calendarRem);
    }

    private boolean isFutureTime(Calendar now, Calendar picked) {
        return picked.after(now);
    }

    private void setDateText(Calendar now, Calendar calendar) {
        if (AppHelper.isSameDay(now, calendar)) {
            textToday.setText(this.getResources().getString(R.string.today));
        } else if (AppHelper.isTomorrow(now, calendar)) {
            textToday.setText(this.getResources().getString(R.string.tomorrow));
        } else {
            textToday.setText(AppHelper.getDateFormat(that).format(calendar));
        }

        if (calendar.before(now)) {
            textToday.setTextColor(that.getResources().getColor(R.color.text_warn));
        } else {
            textToday.setTextColor(that.getResources().getColor(android.R.color.primary_text_light));
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

        setReminderTime(calendarPicked.getTimeInMillis(), true);
    }
}
