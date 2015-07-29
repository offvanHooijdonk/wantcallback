package com.wantcallback.ui.recycler;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by off on 27.07.2015.
 */
public class ItemTouchCallback extends ItemTouchHelper.Callback {

    private OnItemSwipedListener listener;

    public ItemTouchCallback(OnItemSwipedListener l) {
        this.listener = l;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int swipeFlagsH = ItemTouchHelper.START | ItemTouchHelper.END;

        return makeMovementFlags(0, swipeFlagsH);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onItemSwiped(viewHolder.getAdapterPosition());
    }

    /**
     * Created by off on 27.07.2015.
     */
    public static interface OnItemSwipedListener {
        void onItemSwiped(int position);
    }
}
