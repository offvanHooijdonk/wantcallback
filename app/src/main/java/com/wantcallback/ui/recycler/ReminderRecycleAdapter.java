package com.wantcallback.ui.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wantcallback.R;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.helper.ColorHelper;
import com.wantcallback.model.ContactInfo;
import com.wantcallback.model.ReminderInfo;
import com.wantcallback.phone.ContactsUtil;

import java.util.Date;
import java.util.List;

/**
 * Created by off on 26.07.2015.
 */
public class ReminderRecycleAdapter extends RecyclerView.Adapter<ReminderRecycleAdapter.ViewHolder> implements ItemTouchCallback.OnItemSwipedListener {

    private Context ctx;
    private List<ReminderInfo> reminders;
    private OnItemActionListener listener;
    private ContactsUtil contactsUtil;

    public ReminderRecycleAdapter(Context context, List<ReminderInfo> reminders, OnItemActionListener listener) {
        this.ctx = context;
        this.reminders = reminders;
        this.listener = listener;
        contactsUtil = new ContactsUtil(ctx);

        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_reminder_main, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, final int position) {
        final ReminderInfo reminder = reminders.get(position);

        LoadReminderInfoAsyncTask task = new LoadReminderInfoAsyncTask(vh, reminder);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public int getPositionById(long id) {
        int position = -1;
        int i = 0;
        for (ReminderInfo r : reminders) {
            if (r.getId() == id) {
                position = i;
                break;
            }
            i++;
        }

        return position;
    }

    public void onItemClick(long reminderId) {
        if (listener != null) {
            listener.onListItemClicked(reminderId);
        }
    }

    private Bitmap createContactIcon(String phoneOrName) {
        Bitmap b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        int bkgrColor = ColorHelper.getMaterialColorForPhoneOrName(ctx, phoneOrName).mPrimaryColor;
        b.eraseColor(bkgrColor);
        return b;
    }

    @Override
    public void onItemSwiped(int position) {
        long itemId = getItemId(position);

        if (listener != null) {
            listener.onListItemDismissed(itemId);
        }
    }

    @Override
    public long getItemId(int position) {
        return reminders.get(position).getId();
    }

    public interface OnItemActionListener {
        void onListItemClicked(long id);

        void onListItemDismissed(long id);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textPhone;
        public TextView textWhen;
        public TextView textTime;
        private com.pkmmte.view.CircularImageView imageCircle;
        private ImageView imageDefaultContact;
        public ViewGroup blockReminder;

        public ViewHolder(View v) {
            super(v);

            blockReminder = (ViewGroup) v.findViewById(R.id.blockReminder);
            textPhone = (TextView) v.findViewById(R.id.textPhoneNumber);
            textWhen = (TextView) v.findViewById(R.id.textTypeAndWhen);
            textTime = (TextView) v.findViewById(R.id.textTime);
            imageCircle = (com.pkmmte.view.CircularImageView) v.findViewById(R.id.imageCircle);
            imageDefaultContact = (ImageView) v.findViewById(R.id.imageDefaultContact);
        }
    }

    private class LoadReminderInfoAsyncTask extends AsyncTask<Void, Void, ContactInfo> {
        private ViewHolder vh;
        private ReminderInfo reminder;
        private String displayName;
        private Uri imageUri;
        private Bitmap bitmapIcon;
        private String textTime;
        private String callInfoString;

        public LoadReminderInfoAsyncTask(ViewHolder viewHolder, ReminderInfo reminderInfo) {
            this.vh = viewHolder;
            this.reminder = reminderInfo;
        }

        @Override
        protected ContactInfo doInBackground(Void... params) {
            final ContactInfo contact = contactsUtil.findContactByPhone(reminder.getPhone());

            if (contact != null) {
                displayName = contact.getDisplayName();

                if (contact.getThumbUri() != null) {
                    imageUri = contact.getThumbUri();
                } else if (contact.getPhotoUri() != null) {
                    imageUri = contact.getPhotoUri();
                } else {
                    imageUri = null;
                    bitmapIcon = createContactIcon(contact.pickIdentifier());
                }
            } else {
                displayName = reminder.getPhone();

                imageUri = null;
                bitmapIcon = createContactIcon(reminder.getPhone());
            }

            textTime = AppHelper.getTimeFormat(ctx).format(new Date(reminder.getDate()));

            switch (reminder.getCallInfo().getType()) {
                case MISSED: {
                    callInfoString = ctx.getString(R.string.missed_at_time, AppHelper.getTimeFormat(ctx).format(new Date(reminder.getCallInfo().getDate())));
                }
                break;
                case REJECTED: {
                    callInfoString = ctx.getString(R.string.rejected_at_time, AppHelper.getTimeFormat(ctx).format(new Date(reminder.getCallInfo().getDate())));
                }
                break;
                case CREATED: {
                    callInfoString = ctx.getString(R.string.created_at_time, AppHelper.getTimeFormat(ctx).format(new Date(reminder.getCallInfo().getDate())));
                }
                break;
                default: {
                    callInfoString = AppHelper.getTimeFormat(ctx).format(new Date(reminder.getCallInfo().getDate()));
                }
            }

            return contact;
        }

        @Override
        protected void onPostExecute(final ContactInfo contact) {
            vh.textPhone.setText(displayName);

            if (imageUri != null) {
                vh.imageCircle.setImageURI(imageUri);
                vh.imageDefaultContact.setVisibility(View.INVISIBLE);
            } else {
                vh.imageCircle.setImageBitmap(bitmapIcon);
                vh.imageDefaultContact.setVisibility(View.VISIBLE);
            }

            if (contact != null) {
                vh.imageCircle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ctx.startActivity(AppHelper.Intents.buildContactIntent(contact.getId()));
                    }
                });
            } else {
                vh.imageCircle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ctx.startActivity(AppHelper.Intents.buildDialerIntent(reminder.getPhone()));
                    }
                });
            }

            vh.blockReminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(reminder.getId());
                }
            });

            vh.textTime.setText(textTime);

            vh.textWhen.setText(callInfoString);
        }
    }

}
