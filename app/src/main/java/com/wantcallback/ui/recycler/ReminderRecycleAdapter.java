package com.wantcallback.ui.recycler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by off on 26.07.2015.
 */
public class ReminderRecycleAdapter extends RecyclerView.Adapter<ReminderRecycleAdapter.ViewHolder> implements View.OnClickListener, OnItemSwipedListener {

    private static final int UNDO_TIME_SEC = 3;

    private Context ctx;
    private List<ReminderInfo> reminders;
    private OnItemActionListener listener;

    private Map<Long, UndoWaitTask> tasks = new HashMap<>();

    public ReminderRecycleAdapter(Context context, List<ReminderInfo> reminders, OnItemActionListener listener) {
        this.ctx = context;
        this.reminders = reminders;
        this.listener = listener;

        setHasStableIds(true);
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
    public void onBindViewHolder(ViewHolder vh, final int position) {
        final ReminderInfo info = reminders.get(position);

        if (!info.isAboutToDelete()) {
            vh.blockReminder.setVisibility(View.VISIBLE);
            vh.blockUndo.setVisibility(View.GONE);

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
        } else {
            vh.blockReminder.setVisibility(View.GONE);
            vh.blockUndo.setVisibility(View.VISIBLE);

            vh.buttonUndo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUndoDeleteItem(getItemId(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    private void onUndoDeleteItem(long reminderId) {
        int position = getPositionById(reminderId);
        if (position != -1) {
            reminders.get(position).setAboutToDelete(false);
            notifyItemChanged(position);

            if (tasks.containsKey(reminderId)) {
                tasks.get(reminderId).cancel(true);
            }
        }
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
        long itemId = getItemId(position);
        reminders.get(position).setAboutToDelete(true);
        notifyItemChanged(position);

        UndoWaitTask undoWaitTask = new UndoWaitTask();
        undoWaitTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemId);
        tasks.put(itemId, undoWaitTask);
    }

    @Override
    public long getItemId(int position) {
        return reminders.get(position).getId();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textPhone;
        public TextView textWhen;
        public TextView textTime;
        public ImageView imageCall;
        public ViewGroup blockReminder;
        public ViewGroup blockUndo;
        public View buttonUndo;

        public ViewHolder(View v) {
            super(v);

            blockReminder = (ViewGroup) v.findViewById(R.id.blockReminder);
            textPhone = (TextView) v.findViewById(R.id.textPhoneNumber);
            textWhen = (TextView) v.findViewById(R.id.textTypeAndWhen);
            textTime = (TextView) v.findViewById(R.id.textTime);
            imageCall = (ImageView) v.findViewById(R.id.imageCall);

            blockUndo = (ViewGroup) v.findViewById(R.id.blockUndo);
            buttonUndo = v.findViewById(R.id.buttonUndo);
        }
    }

    private class UndoWaitTask extends AsyncTask<Long, Void, Void> {
        private long reminderId;

        @Override
        protected Void doInBackground(Long... params) {
            reminderId = params[0];
            try {
                Thread.sleep(UNDO_TIME_SEC * 1000);
            } catch (InterruptedException e) {
                // TODO handle
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (listener != null) {
                listener.onListItemDismissed(reminderId);
            }
        }
    }

    public interface OnItemActionListener {
        void onListItemClicked(View v);
        void onListItemDismissed(long id);
    }
}
