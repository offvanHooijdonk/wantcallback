package com.wantcallback.ui.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wantcallback.R;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.helper.ImageHelper;
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

        final ContactInfo contact = contactsUtil.findContactByPhone(reminder.getPhone());
        if (contact != null) {
            vh.textPhone.setText(contact.getDisplayName());
            if (contact.getPhotoUri() != null) {
                vh.imageCircle.setImageURI(contact.getPhotoUri());
                vh.imageDefaultContact.setVisibility(View.INVISIBLE);
            } else if (contact.getThumbUri() != null) {
                vh.imageCircle.setImageURI(contact.getThumbUri());
                vh.imageDefaultContact.setVisibility(View.INVISIBLE);
            } else {
                setColorOverlay(vh, contact.getDisplayName());
            }
            vh.imageCircle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ctx.startActivity(AppHelper.Intents.createContactIntent(contact.getId()));
                }
            });
        } else {
            vh.textPhone.setText(reminder.getPhone());
            setColorOverlay(vh, reminder.getPhone());
            vh.imageCircle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ctx.startActivity(AppHelper.Intents.createDialerIntent(reminder.getPhone()));
                }
            });
        }

        vh.blockReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(reminder.getId());
            }
        });

        vh.textTime.setText(AppHelper.getTimeFormat(ctx).format(new Date(reminder.getDate())));

        String callInfoString;
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
        vh.textWhen.setText(callInfoString);

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

    private void setColorOverlay(ViewHolder vh, String phoneOrName) {
        Bitmap b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        b.eraseColor(ImageHelper.getMaterialColorForPhoneOrName(ctx, phoneOrName));
        vh.imageCircle.setImageBitmap(b);
        vh.imageDefaultContact.setVisibility(View.VISIBLE);

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

}
