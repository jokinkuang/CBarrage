package com.jokin.cbarrage.cbarrage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

/**
 * Created by jokinkuang on 2017/9/8.
 */

public class CBarrageRow {
    private static final String TAG = "CBarrageRow";

    // | head ... tail |
    private Deque<CBarrageItem> mItems = new ArrayDeque<>();
    private Deque<CBarrageItem> mRecycleBin = new ArrayDeque<>();
    private ItemListener mItemListener = new ItemListener();
    private Queue<View> mPendingPriorityQueue = new ArrayDeque<>(100);

    @NonNull
    private WeakReference<ViewGroup> mContainerView = new WeakReference<ViewGroup>(null);

    private int mRowIndex = -1;
    private int mRowTop = 0;
    private int mRowBottom = 0;

    private int mHeight = 0;
    private int mWidth = 0;

    private int mItemGap = 0;
    private int mItemSpeed = 0;
    private int mItemGravity = 0;


    public interface BarrageRowListener {
        void onRowIdle(CBarrageRow row);
    }
    private BarrageRowListener mListener;
    public void setRowListener(BarrageRowListener listener) {
        mListener = listener;
    }


    private CBarrageItem mFirstItem;
    private CBarrageItem mCenterItem;
    private CBarrageItem mLastItem;

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

    public int getRowIndex() {
        return mRowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.mRowIndex = rowIndex;
    }

    public int getRowTop() {
        return mRowTop;
    }

    public void setRowTop(int rowTop) {
        this.mRowTop = rowTop;
    }

    public int getRowBottom() {
        return mRowBottom;
    }

    public void setRowBottom(int rowBottom) {
        this.mRowBottom = rowBottom;
    }

    public int getItemCount() {
        return mItems.size();
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

    public void setItemGravity(int itemGravity) {
        this.mItemGravity = itemGravity;
    }

    public int getItemGravity() {
        return mItemGravity;
    }

    public void setContainerView(ViewGroup view) {
        mContainerView = new WeakReference<ViewGroup>(view);
    }

    public void clear() {
        while (mItems.size() > 0) {
            CBarrageItem item = mItems.poll();
            // remove view from container
            if (mContainerView.get() != null) {
                mContainerView.get().removeView(item.getContentView());
            }
            item.clear();
            mRecycleBin.add(item);
        }
    }


    public void appendItem(View view) {
        CBarrageItem item = obtainBarrageItem();
        item.setRow(this);
        item.setContentView(view);
        item.setDistance(mWidth);
        item.setSpeed(mItemSpeed);
        item.setGravity(mItemGravity);
        item.setListener(mItemListener);
        item.start();

        mItems.addLast(item);

        Log.d(TAG, String.format("distance %d speed %d", mWidth, mItemSpeed));
        // add view to container
        if (mContainerView.get() != null) {
            mContainerView.get().addView(view);
        }
    }

    public void appendPriorityItem(View view) {
        if (! mPendingPriorityQueue.isEmpty()) {
            mPendingPriorityQueue.add(view);
            return;
        }
        if (! isIdle()) {
            mPendingPriorityQueue.add(view);
            return;
        }
        appendItem(view);
    }

    private CBarrageItem obtainBarrageItem() {
        if (mRecycleBin.isEmpty()) {
            return new CBarrageItem();
        }
        return mRecycleBin.poll();
    }

    public void onItemUpdate(CBarrageItem item) {
        if (isIdle()) {
            if (! mPendingPriorityQueue.isEmpty()) {
                appendItem(mPendingPriorityQueue.poll());
                return;
            }
            if (mListener != null) {
                mListener.onRowIdle(this);
            }
        }
    }

    public void onItemFinish(CBarrageItem item) {
        Log.d(TAG, "remove item "+item.toString());
        // remove view from container
        if (mContainerView.get() != null) {
            mContainerView.get().removeView(item.getContentView());
        }
        if (mItems.remove(item)) {
            mRecycleBin.add(item);
        }
    }

    public boolean isIdle() {
        CBarrageItem lastItem = getLastItem();
        if (lastItem == null) {
            return true;
        }
        View contentView = lastItem.getContentView();
        if (contentView == null) {
            return true;
        }
        //  |---[ItemWidth ItemGap]--|
        if (contentView.getX() + contentView.getWidth() + mItemGap <= mWidth) {
            // Log.d(TAG, String.format("Idle x %f l %d w %d g %d sw %d", contentView.getX(), contentView.getLeft(),
            //         contentView.getWidth(), mItemGap, mWidth));
            if (contentView.getX() == 0) {
                // means the last item was adding
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @return 距离下一次空闲时间，当前空闲返回0
     **/
    public int peekNextIdleTime() {
        CBarrageItem lastItem = getLastItem();
        if (lastItem == null) {
            return 0;
        }
        View contentView = lastItem.getContentView();
        if (contentView == null) {
            return 0;
        }
        //  |---[ItemWidth ItemGap]--|
        int moveDist = (int) ((contentView.getX() + contentView.getWidth() + mItemGap) - mWidth);
        if (moveDist <= 0) {
            if (contentView.getX() == 0) {
                // means the last item was adding
                return lastItem.getSpeed();
            }
            return 0;
        }
        return (int) (moveDist*1.0/mWidth * lastItem.getSpeed());
    }

    @Nullable
    private CBarrageItem getLastItem() {
        if (mItems.isEmpty()) {
            return null;
        }
        return mItems.peekLast();
    }

    private class ItemListener implements CBarrageItem.BarrageItemListener {
        @Override
        public void onAnimationCancel(CBarrageItem item) {
        }

        @Override
        public void onAnimationEnd(CBarrageItem item) {
            onItemFinish(item);
        }

        @Override
        public void onAnimationRepeat(CBarrageItem item) {

        }

        @Override
        public void onAnimationStart(CBarrageItem item) {

        }

        @Override
        public void onAnimationPause(CBarrageItem item) {

        }

        @Override
        public void onAnimationResume(CBarrageItem item) {

        }

        @Override
        public void onAnimationUpdate(CBarrageItem item) {
            onItemUpdate(item);
        }
    }

    /**
     * For Debug
     */
    public void dumpMemory() {
        String TAG = "dump";
        Log.d(TAG, String.format("Row index %d itemCount %d recycleBinCount %d pendingQueueSize %d",
                getRowIndex(), getItemCount(), mRecycleBin.size(), mPendingPriorityQueue.size()));
    }
}
