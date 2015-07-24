package com.wantcallback.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wantcallback.R;
import com.wantcallback.helper.AppHelper;
import com.wantcallback.model.ReminderInfo;

import java.util.Date;
import java.util.List;

/**
 * Created by Yahor_Fralou on 7/16/2015.
 */
public class ReminderMainAdapter extends BaseAdapter {

    private Context ctx;
    private LayoutInflater inflater;
    private ReminderInteractionListener listener;
    private List<ReminderInfo> reminders;

    public ReminderMainAdapter(Context context, List<ReminderInfo> reminders, ReminderInteractionListener listener) {
        this.ctx = context;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = listener;
        this.reminders = reminders;
    }

    @Override
    public int getCount() {
        return reminders.size();
    }

    @Override
    public Object getItem(int position) {
        return reminders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return reminders.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ReminderInfo info = reminders.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_reminder_main, parent, false);
        }

        TextView textPhone = (TextView) convertView.findViewById(R.id.textPhoneNumber);
        textPhone.setText(info.getPhone());

        TextView textTime = (TextView) convertView.findViewById(R.id.textTime);
        textTime.setText(AppHelper.getTimeFormat(ctx).format(new Date(info.getDate())));

        TextView textTypeAndWhen = (TextView) convertView.findViewById(R.id.textTypeAndWhen);
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
        textTypeAndWhen.setText(callInfoString);

        ImageView imageCall = (ImageView) convertView.findViewById(R.id.imageCall);
        imageCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.startActivity(getCallerIntent(info));
            }
        });

        ImageView imageForget = (ImageView) convertView.findViewById(R.id.imageForget);
        imageForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteReminder(info);
                }
            }
        });

        return convertView;
    }

    public void setReminderInteractionListener(ReminderInteractionListener l) {

    }

    private Intent getCallerIntent(ReminderInfo info) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + info.getPhone()));

        return intent;
    }

    public interface ReminderInteractionListener {
        void onDeleteReminder(ReminderInfo info);
    }
}
