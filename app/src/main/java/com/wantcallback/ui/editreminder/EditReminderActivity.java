package com.wantcallback.ui.editreminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.contacts.common.util.MaterialColorMapUtils;
import com.wantcallback.Constants;
import com.wantcallback.R;
import com.wantcallback.dao.impl.ReminderDao;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.helper.ColorHelper;
import com.wantcallback.model.CallInfo;
import com.wantcallback.model.ContactInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.notifications.NotificationsUtil;
import com.wantcallback.phone.ContactsUtil;
import com.wantcallback.reminder.ReminderUtil;
import com.wantcallback.ui.actionbar.ControllableAppBarLayout;
import com.wantcallback.ui.view.EditTextTrackFixed;

import java.util.Calendar;
import java.util.Date;

public class EditReminderActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    public static final String EXTRA_CALL_INFO = "extra_call_info";
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_NOTIF_TAG = "extra_notif_tag";
    public static final String EXTRA_NOTIF_ID = "extra_notif_id";

    private static final int TAG_PICK_CONTACT = 1;

    private final Handler contactImageExpandHandler = new Handler();

    private EditReminderActivity that;

    public enum MODE {
        EDIT, CREATE, BLANK
    }

    private MODE mode;

    private EditTextTrackFixed inputPhone;
    private TextView textTime;
    private TextView textToday;
    private TextView textHaveReminder;
    private FloatingActionButton btnSave;
    private FloatingActionButton btnPickContact;
    private ControllableAppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private PortraitFragment fragContactImage;
    private ViewGroup blockForm;

    private long reminderId;
    private Date remindDate = null;
    private ReminderDao reminderDao;
    private CallInfo callInfo = null;
    private ContactInfo contact = null;
    private ReminderInfo reminderInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        this.that = this;

        appBarLayout = (ControllableAppBarLayout) findViewById(R.id.appBarLayout);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        if (!AppHelper.isApplicationEnabled(that)) {
            btnPickContact.hide();
            btnSave.hide();
            //blockForm = (ViewGroup) findViewById(R.id.blockForm);
            View viewAppDisabled = findViewById(R.id.viewAppDisabledOverlay);
            //blockForm.setVisibility(View.GONE);
            viewAppDisabled.setVisibility(View.VISIBLE);

            return;
        }

        reminderDao = new ReminderDao(this);

        initForm();

        inputPhone = (EditTextTrackFixed) findViewById(R.id.inputPhone);
        inputPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        btnPickContact = (FloatingActionButton) findViewById(R.id.buttonPickUser);
        textTime = (TextView) findViewById(R.id.textTime);
        textToday = (TextView) findViewById(R.id.textToday);
        textHaveReminder = (TextView) findViewById(R.id.textHaveReminder);
        btnSave = (FloatingActionButton) findViewById(R.id.btnSave);

        fragContactImage = new PortraitFragment();
        fragContactImage.setMode(mode);
        fragContactImage.setContact(contact);
        fragContactImage.setReminderInfo(reminderInfo);
        getFragmentManager().beginTransaction().add(R.id.frameContactImage, fragContactImage).commit();

        inputPhone.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String text = inputPhone.getText().toString();
                if ("+".equals(text)) {
                    text = "";
                }
                collapsingToolbar.setTitle(text);
                return false;
            }
        });
        inputPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    onPhoneInput();
                }
                return false;
            }
        });

        /*photoOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact != null) {
                    that.startActivity(AppHelper.Intents.buildContactIntent(contact.getId()));
                }
            }
        });*/

        btnPickContact.setOnClickListener(new View.OnClickListener() {
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
                onPhoneInput();
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

                    ReminderUtil.createNewReminder(EditReminderActivity.this, info);

                    Toast.makeText(EditReminderActivity.this, that.getString(R.string.reminder_created_message, AppHelper.getDateFormat(that).format(remindDate)), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
        //initForm();

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        displayForm(reminderInfo);
    }

    private boolean validateForm() {
        boolean isValid = true;

        Calendar now = Calendar.getInstance();
        Calendar picked = Calendar.getInstance();
        picked.setTime(remindDate);

        if (inputPhone.getText().toString().trim().equals("")) {
            isValid = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(that);
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
                contact = util.getContactFromUri(uri);

                setReminderTime(ReminderUtil.calcDefaultRemindDate(that, Calendar.getInstance().getTimeInMillis()), true);
                // check if
                if (contact != null) {
                    inputPhone.setText(AppHelper.formatPhoneNumber(that, contact.getPhoneNumber()));
                    inputPhone.setFocusable(false);

                    fragContactImage.setContact(contact);
                    fragContactImage.drawUI();

                    ReminderInfo reminderInfo = reminderDao.findByPhone(inputPhone.getText().toString());
                    if (reminderInfo != null) {
                        setReminderTime(reminderInfo.getDate(), false);
                        textHaveReminder.setVisibility(View.VISIBLE);
                        reminderId = reminderInfo.getId();

                        mode = MODE.EDIT;
                        that.supportInvalidateOptionsMenu();
                    } else {
                        textHaveReminder.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(that, "Could not get contact :(", Toast.LENGTH_LONG).show();
                    inputPhone.setText(null);
                }
                fillContactInfo(contact);
                showAppBar(false, false);

                showAppBar(true, true, true);
            }
        }
    }

    private void fillContactInfo(ContactInfo contact) {
        if (contact != null) {
            collapsingToolbar.setTitle(contact.getDisplayName());

            if (contact.getPhotoUri() != null) {
                MaterialColorMapUtils.MaterialPalette palette = ColorHelper.getMaterialPalette(that, contact.getThumbUri() != null ? contact.getThumbUri() : contact.getPhotoUri());
                //colorizeForm(palette);


            } else {
                colorizeIconAndForm(contact.pickIdentifier());
            }
        } else {
            collapsingToolbar.setTitle(inputPhone.getText());
            colorizeIconAndForm(inputPhone.getText().toString());
        }
    }

    private void displayForm(ReminderInfo reminder) {
        if (mode == MODE.EDIT || mode == MODE.CREATE) {
            if (mode == MODE.EDIT) {
                textHaveReminder.setVisibility(View.GONE);
                inputPhone.setText(reminder.getPhone());
                setReminderTime(reminder.getDate(), false);
            } else if (mode == MODE.CREATE) {
                textHaveReminder.setVisibility(View.GONE);
                inputPhone.setText(AppHelper.formatPhoneNumber(that, reminder.getCallInfo().getPhone()));
                setReminderTime(ReminderUtil.calcDefaultRemindDate(that, Calendar.getInstance().getTimeInMillis()), true);
            }

            btnPickContact.setVisibility(View.GONE);
            inputPhone.setFocusable(false);

            ContactsUtil util = new ContactsUtil(that);
            contact = util.findContactByPhone(reminder.getCallInfo().getPhone());

            fillContactInfo(contact);
        } else if (mode == MODE.BLANK) {
            textHaveReminder.setVisibility(View.GONE);
            inputPhone.setFocusable(true);
            btnPickContact.setVisibility(View.VISIBLE);

            setReminderTime(ReminderUtil.calcDefaultRemindDate(that, Calendar.getInstance().getTimeInMillis()), true);

            showAppBar(false, false);

            // TODO move some decorations from this method
            colorizeForm(ColorHelper.getPaletteOnColor(that, that.getResources().getColor(R.color.app_accent)));
        }

    }


    /**
     * check which data we got on the screen, set variables to pass them to correspondent fragments
     */
    private void initForm() {
        mode = MODE.BLANK; // default
        Intent intent = getIntent();

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
                    Toast.makeText(that, "Reminder not found!", Toast.LENGTH_LONG).show();
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

    }

    private void showAppBar(boolean show, boolean animate) {
        showAppBar(show, animate, false);
    }

    private void showAppBar(boolean show, boolean animate, boolean delay) {
        if (show) {
            if (animate) {
                if (delay) {
                    contactImageExpandHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fragContactImage.showPortrait(true);
                            appBarLayout.expandToolbar(true);
                        }
                    }, Constants.ANIM_APP_BAR_DELAY);

                } else {
                    fragContactImage.showPortrait(true);
                    appBarLayout.expandToolbar(true);
                }
            } else {
                fragContactImage.showPortrait(true);
                appBarLayout.expandToolbar(false);
            }
        } else {
            appBarLayout.collapseToolbar(animate);
            fragContactImage.showPortrait(false);
        }
    }

    private void colorizeIconAndForm(String identifier) {
        MaterialColorMapUtils.MaterialPalette palette = ColorHelper.getMaterialColorForPhoneOrName(that, identifier);
        colorizeForm(palette);

    }

    private void colorizeForm(MaterialColorMapUtils.MaterialPalette palette) {
        collapsingToolbar.setContentScrimColor(palette.mPrimaryColor);
        collapsingToolbar.setBackgroundColor(palette.mPrimaryColor);
        ColorHelper.setStatusBarColor(that, palette.mSecondaryColor);

        fragContactImage.drawUI();

        btnPickContact.setBackgroundTintList(ColorStateList.valueOf(palette.mPrimaryColor));
        btnSave.setBackgroundTintList(ColorStateList.valueOf(palette.mPrimaryColor));
        textTime.setTextColor(palette.mPrimaryColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputPhone.setBackgroundTintList(ColorStateList.valueOf(palette.mPrimaryColor));
        }
    }

    private void onPhoneInput() {
        if (!TextUtils.isEmpty(inputPhone.getText().toString())) {
            if (contact == null) {
                contact = new ContactInfo();
                contact.setPhoneNumber(inputPhone.getText().toString().trim());
                fragContactImage.setContact(contact);

                colorizeIconAndForm(inputPhone.getText().toString());

                showAppBar(true, true);
            }
        } else {
            showAppBar(false, true);
        }
    }

    private void setReminderTime(long time, boolean calculate) {
        Calendar calendarRem = Calendar.getInstance();
        calendarRem.setTimeInMillis(time);
        Calendar now = Calendar.getInstance();

        if (!isFutureTime(now, calendarRem) && calculate) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_form, menu);
        MenuItem itemCall = menu.findItem(R.id.action_call);
        MenuItem itemRemove = menu.findItem(R.id.action_remove_reminder);
        if (mode == MODE.EDIT) {
            itemCall.setVisible(true);
            itemRemove.setVisible(true);
        } else if (mode == MODE.CREATE) {
            itemCall.setVisible(true);
            itemRemove.setVisible(false);
        } else if (mode == MODE.BLANK) {
            itemCall.setVisible(false);
            itemRemove.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_call) {
            that.startActivity(AppHelper.Intents.buildDialerIntent(inputPhone.getText().toString()));
        } else if (item.getItemId() == R.id.action_remove_reminder) {
            ReminderInfo reminder = reminderDao.getById(reminderId);
            if (reminder != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditReminderActivity.this);
                builder.setTitle(R.string.contact_remove_confirm_title).setMessage(R.string.contact_remove_confirm_text)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ReminderUtil.cancelAndRemoveReminder(that, reminderDao.getById(reminderId));
                                Toast.makeText(that, R.string.contact_removed_message, Toast.LENGTH_LONG).show();
                                that.finish();
                            }
                        })
                        .show();
            } else {
                Toast.makeText(that, R.string.contact_remove_not_found, Toast.LENGTH_LONG).show();
                that.finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        NavUtils.navigateUpFromSameTask(that);
        super.onDestroy();
    }

    /*private class AnimAppBarDelayTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(Constants.ANIM_APP_BAR_DELAY);
            } catch (InterruptedException e) {
                Toast.makeText(that, e.toString(), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            photoInToolbar.setVisibility(View.VISIBLE);
            appBarLayout.expandToolbar(true);
        }
    }*/
}
