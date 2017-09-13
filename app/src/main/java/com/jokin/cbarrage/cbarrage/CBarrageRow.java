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
    private Queue<Object> mPendingPriorityQueue = new ArrayDeque<>(100);

    @NonNull
    private WeakReference<ViewGroup> mContainerView = new WeakReference<ViewGroup>(null);

    private int mIndex = -1;

    private int mHeight = 0;
    private int mWidth = 0;
    private int mLeft = 0;
    private int mRight = 0;
    private int mTop = 0;
    private int mBottom = 0;

    private int mItemGap = 0;
    private int mItemSpeed = 0;
    private int mItemGravity = 0;


    public interface BarrageRowListener {
        View onViewCreate(CBarrageRow row, Object obj);
        void onViewDestroy(CBarrageRow row, Object obj, @NonNull View view);
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

    public int getLeft() {
        return mLeft;
    }

    public void setLeft(int left) {
        mLeft = left;
    }

    public int getRight() {
        return mRight;
    }

    public void setRight(int right) {
        mRight = right;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int rowIndex) {
        this.mIndex = rowIndex;
    }

    public int getTop() {
        return mTop;
    }

    public void setTop(int rowTop) {
        this.mTop = rowTop;
    }

    public int getBottom() {
        return mBottom;
    }

    public void setBottom(int rowBottom) {
        this.mBottom = rowBottom;
    }

    public int getRowPendingSize() {
        return mPendingPriorityQueue.size();
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

    public void pause() {
        for (CBarrageItem item : mItems) {
            item.pause();
        }
    }

    public void resume() {
        for (CBarrageItem item : mItems) {
            item.resume();
        }
    }

    public void clear() {
        mPendingPriorityQueue.clear();
        while (mItems.size() > 0) {
            CBarrageItem item = mItems.poll();
            if (mListener != null && item.getContentView() != null) {
                mListener.onViewDestroy(this, item.getData(), item.getContentView());
            }
            item.clear();
            mRecycleBin.add(item);
        }
    }


    public void appendItem(Object obj) {
        if (mListener == null) {
            Log.e(TAG, "snbh. listener is null.");
            return;
        }
        View view = mListener.onViewCreate(this, obj);
        if (view == null) {
            return;
        }

        CBarrageItem item = obtainBarrageItem();
        item.setRow(this);
        item.setData(obj);
        item.setContentView(view);
        item.setDistance(mWidth);
        item.setSpeed(mItemSpeed);
        item.setGravity(mItemGravity);
        item.setListener(mItemListener);
        item.start();

        mItems.addLast(item);
        Log.d(TAG, String.format("distance %d speed %d", mWidth, mItemSpeed));
    }

    /**
     * 行优先队列，比View优先队列更优先
     * 仅用于动画，确保动画消失所在的行会出现对应的动画对象
     * @param obj
     */
    public void appendPriorityItem(Object obj) {
        if (! mPendingPriorityQueue.isEmpty()) {
            mPendingPriorityQueue.add(obj);
            return;
        }
        if (! isIdle()) {
            mPendingPriorityQueue.add(obj);
            return;
        }
        appendItem(obj);
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
        if (mItems.remove(item)) {
            mRecycleBin.add(item);

            if (mListener != null && item.getContentView() != null) {
                mListener.onViewDestroy(this, item.getData(), item.getContentView());
            }
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
                getIndex(), getItemCount(), mRecycleBin.size(), mPendingPriorityQueue.size()));
    }
}
