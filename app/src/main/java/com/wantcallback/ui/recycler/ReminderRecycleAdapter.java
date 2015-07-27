package com.wantcallback.ui.recycler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wantcallback.R;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.ReminderInfo;

import java.util.Date;
import java.util.List;

/**
 * Created by off on 26.07.2015.
 */
public class ReminderRecycleAdapter extends RecyclerView.Adapter<ReminderRecycleAdapter.ViewHolder> implements View.OnClickListener, OnItemSwipedListener {

    private Context ctx;
    private List<ReminderInfo> reminders;
    private OnItemActionListener listener;

    public ReminderRecycleAdapter(Context context, List<ReminderInfo> reminders, OnItemActionListener listener) {
        this.ctx = context;
        this.reminders = reminders;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_reminder_main, parent, false);

        v.setOnClickListener(this);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        final ReminderInfo info = reminders.get(position);

        vh.textPhone.setText(info.getPhone());

        vh.textTime.setText(AppHelper.getTimeFormat(ctx).format(new Date(info.getDate())));

        String callInfoString;
        switch (info.getCallInfo().getType()) {
            case MISSED: {
                callInfoString = ctx.getString(R.string.missed_at_time, AppHelper.getTimeFormat(ctx).format(new Date(info.getCallInfo().getDate())));
            }
            break;
            case REJECTED: {
                callInfoString = ctx.getString(R.string.rejected_at_time, AppHelper.getTimeFormat(ctx).format(new Date(info.getCallInfo().getDate())));
            }
            break;
            case CREATED: {
                callInfoString = ctx.getString(R.string.created_at_time, AppHelper.getTimeFormat(ctx).format(new Date(info.getCallInfo().getDate())));
            }
            break;
            default: {
                callInfoString = AppHelper.getTimeFormat(ctx).format(new Date(info.getCallInfo().getDate()));
            }
        }
        vh.textWhen.setText(callInfoString);

        vh.imageCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.startActivity(getDialerIntent(info));
            }
        });

    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    private Intent getDialerIntent(ReminderInfo info) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + info.getPhone()));

        return intent;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onListItemClicked(v);
        }
    }

    @Override
    public void onItemSwiped(int position) {
        if (listener != null) {
            listener.onListItemDismissed(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textPhone;
        public TextView textWhen;
        public TextView textTime;
        public ImageView imageCall;

        public ViewHolder(View v) {
            super(v);

            textPhone = (TextView) v.findViewById(R.id.textPhoneNumber);
            textWhen = (TextView) v.findViewById(R.id.textTypeAndWhen);
            textTime = (TextView) v.findViewById(R.id.textTime);
            imageCall = (ImageView) v.findViewById(R.id.imageCall);
        }
    }

    public interface OnItemActionListener {
        void onListItemClicked(View v);
        void onListItemDismissed(int position);
    }
}
