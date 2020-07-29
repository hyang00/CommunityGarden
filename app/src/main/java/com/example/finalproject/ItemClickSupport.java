package com.example.finalproject;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Hugo Visser.
 * Please see http://www.littlerobots.nl/blog/Handle-Android-RecyclerView-Clicks/
 * <p>
 * Modified by Filippo Beraldo:
 * - Added OnDoubleClickListener in place of the standard OnClickListener.
 * https://gist.github.com/beraldofilippo/d8d2404bc6f4217fd1a76166cf23e2d0
 */

@SuppressWarnings("UnusedReturnValue")
public class ItemClickSupport {
    private final RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;
    private OnDoubleClickListener mOnDoubleClickListener = new OnDoubleClickListener() {
        @Override
        public void onDoubleClick(View v) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                mOnItemClickListener.onItemDoubleClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
        }

        @Override
        public void onSingleClick(View v) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
        }
    };

    private RecyclerView.OnChildAttachStateChangeListener mAttachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnDoubleClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

        }
    };

    private ItemClickSupport(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setTag(R.id.item_click_support, this);
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    public static ItemClickSupport addTo(RecyclerView view) {
        ItemClickSupport support = (ItemClickSupport) view.getTag(R.id.item_click_support);
        if (support == null) {
            support = new ItemClickSupport(view);
        }
        return support;
    }

    public static ItemClickSupport removeFrom(RecyclerView view) {
        ItemClickSupport support = (ItemClickSupport) view.getTag(R.id.item_click_support);
        if (support != null) {
            support.detach(view);
        }
        return support;
    }

    public ItemClickSupport setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    private void detach(RecyclerView view) {
        view.removeOnChildAttachStateChangeListener(mAttachListener);
        view.setTag(R.id.item_click_support, null);
    }

    @SuppressWarnings("unused")
    public interface OnItemClickListener {
        void onItemClicked(RecyclerView recyclerView, int position, View v);

        void onItemDoubleClicked(RecyclerView recyclerView, int position, View v);
    }
}
