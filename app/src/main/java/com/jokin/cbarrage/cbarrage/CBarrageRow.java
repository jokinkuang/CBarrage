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

public class CBarrageRow {
    private static final String TAG = "CBarrageRow";

    // | head ... tail |
    private Deque<CBarrageItem> mItems = new ArrayDeque<>();
    private Deque<CBarrageItem> mRecycleBin = new ArrayDeque<>();
    private ItemListener mItemListener = new ItemListener();

    @NonNull
    private WeakReference<ViewGroup> mContainerView = new WeakReference<ViewGroup>(null);

    private int mRowIndex = -1;
    private int mRowTop = 0;
    private int mRowBottom = 0;

    private int mHeight = 0;
    private int mWidth = 0;

    private int mItemGap = 0;
    private int mItemSpeed = 0;


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
            CBarrageItem item = mItems.poll();
            item.recycle();
            mRecycleBin.add(item);
        }
    }


    public void appendItem(View view) {
        CBarrageItem item = obtainBarrageItem();
        item.setRow(this);
        item.setContentView(view);
        item.setDistance(mWidth);
        item.setSpeed(mItemSpeed);
        item.setListener(mItemListener);
        item.start();

        mItems.addLast(item);

        Log.d(TAG, String.format("distance %d speed %d", mWidth, mItemSpeed));
        // add view to container the last!! for listen its layout
        if (mContainerView.get() != null) {
            mContainerView.get().addView(view);
        }
    }

    private CBarrageItem obtainBarrageItem() {
        if (mRecycleBin.isEmpty()) {
            return new CBarrageItem();
        }
        return mRecycleBin.poll();
    }

    public void onItemUpdate(CBarrageItem item) {
        checkIdle();
    }

    public void onItemFinish(CBarrageItem item) {
        Log.d(TAG, "remove item "+item.toString());
        // remove view from container first!!
        if (mContainerView.get() != null) {
            mContainerView.get().removeView(item.getContentView());
        }
        mItems.remove(item);
        mRecycleBin.add(item);
    }

    private void checkIdle() {
        if (isIdle()) {
            if (mListener != null) {
                mListener.onRowIdle(this);
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
                // means the last item was out of screen
                return false;
            }
            return true;
        }
        return false;
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
            onItemFinish(item);
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
}
