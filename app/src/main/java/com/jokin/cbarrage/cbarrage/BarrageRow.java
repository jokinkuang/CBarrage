package com.jokin.cbarrage.cbarrage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by jokinkuang on 2017/9/8.
 */

public class BarrageRow {
    private static final String TAG = "BarrageRow";

    // | head ... tail |
    private Deque<BarrageItem> mItems = new ArrayDeque<>();
    private Deque<BarrageItem> mRecycleBin = new ArrayDeque<>();
    private ItemListener mItemListener = new ItemListener();

    @NonNull
    private WeakReference<ViewGroup> mContainerView = new WeakReference<ViewGroup>(null);

    private int mHeight = 0;
    private int mWidth = 0;

    private int mItemGap = 0;
    private int mItemSpeed = 0;


    public interface BarrageRowListener {
        void onRowIdle(BarrageRow row);
    }
    private BarrageRowListener mListener;
    public void setRowListener(BarrageRowListener listener) {
        mListener = listener;
    }


    private BarrageItem mFirstItem;
    private BarrageItem mCenterItem;
    private BarrageItem mLastItem;

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getItemGap() {
        return mItemGap;
    }

    public void setItemGap(int itemGap) {
        this.mItemGap = itemGap;
    }

    public int getItemSpeed() {
        return mItemSpeed;
    }

    public void setItemSpeed(int itemSpeed) {
        this.mItemSpeed = itemSpeed;
    }

    public void setContainerView(ViewGroup view) {
        mContainerView = new WeakReference<ViewGroup>(view);
    }

    public void clear() {
        for (int i = 0; i < mItems.size(); ++i) {
            BarrageItem item = mItems.poll();
            item.recycle();
            mRecycleBin.add(item);
        }
    }


    public void appendItem(View view) {
        // add view to container
        if (mContainerView.get() != null) {
            mContainerView.get().addView(view);
        }

        BarrageItem item = obtainBarrageItem();
        item.setContentView(view);
        item.setDistance(mWidth);
        item.setDuration(mItemSpeed);
        item.setListener(mItemListener);

        // add to Items before start(), for start() may cause a update() immediately
        mItems.addLast(item);

        Log.d(TAG, String.format("distance %d speed %d", mWidth, mItemSpeed));
        item.start();
    }

    private BarrageItem obtainBarrageItem() {
        if (mRecycleBin.isEmpty()) {
            return new BarrageItem();
        }
        return mRecycleBin.poll();
    }

    public void onItemUpdate(BarrageItem item) {
        checkIdle();
    }

    public void onItemFinish(BarrageItem item) {
        // add view to container
        if (mContainerView.get() != null) {
            mContainerView.get().removeView(item.getContentView());
        }

        mItems.remove(item);
    }

    private void checkIdle() {
        if (isIdle()) {
            if (mListener != null) {
                mListener.onRowIdle(this);
            }
        }
    }

    public boolean isIdle() {
        BarrageItem lastItem = getLastItem();
        if (lastItem == null) {
            return true;
        }
        View contentView = lastItem.getContentView();
        if (contentView == null) {
            return true;
        }
        if (contentView.getX() == 0) {
            // means the last item was adding to container
            return false;
        }
        //  |---[ItemWidth ItemGap]--|
        Log.d(TAG, String.format("x %f l %d w %d g %d", contentView.getX(), contentView.getLeft(),
                mWidth, mItemGap));
        if (contentView.getX() + contentView.getWidth() + mItemGap <= mWidth) {
            return true;
        }
        return false;
    }


    @Nullable
    private BarrageItem getLastItem() {
        if (mItems.isEmpty()) {
            return null;
        }
        return mItems.peekLast();
    }

    private class ItemListener implements BarrageItem.BarrageItemListener {
        @Override
        public void onAnimationCancel(BarrageItem item) {
            onItemFinish(item);
        }

        @Override
        public void onAnimationEnd(BarrageItem item) {
            onItemFinish(item);
        }

        @Override
        public void onAnimationRepeat(BarrageItem item) {

        }

        @Override
        public void onAnimationStart(BarrageItem item) {

        }

        @Override
        public void onAnimationPause(BarrageItem item) {

        }

        @Override
        public void onAnimationResume(BarrageItem item) {

        }

        @Override
        public void onAnimationUpdate(BarrageItem item) {
            onItemUpdate(item);
        }
    }
}
