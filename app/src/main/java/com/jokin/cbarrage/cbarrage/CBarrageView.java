package com.jokin.cbarrage.cbarrage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by jokinkuang on 2017/9/8.
 */

public class CBarrageView extends FrameLayout {
    private static final String TAG = "CBarrageView";

    private List<CBarrageRow> mRows = new ArrayList<>(20);
    private Queue<View> mPendingQueue = new ArrayDeque<>(100);
    private TreeObserver observer = new TreeObserver(this);

    private RowListener mRowListener = new RowListener(this);

    public interface CBarrageViewListener {
        /**
         * init after prepared
         **/
        void onPrepared(CBarrageView view);
    }
    private CBarrageViewListener mListener;
    public void setListener(CBarrageViewListener listener) {
        mListener = listener;
    }


    private boolean mIsStarted;
    private boolean mIsPrepared;

    private int mRowNum;
    private int mRowGap;
    private int mRowHeight;
    private int mRowSpeed;

    private int mItemGap;


    /**
     * @param speed 划完一行需要的时间(ms)，行宽为弹幕视图宽度
     **/
    public void setRowSpeed(int speed) {
        this.mRowSpeed = speed;
        createRowsIfNotExist();
    }

    public int getRowSpeed() {
        return mRowSpeed;
    }


    /**
     * @param height 行高(dp)
     */
    public void setRowHeight(int height) {
        this.mRowHeight = CBarrageUtil.dip2px(getContext(), height);
        createRowsIfNotExist();
    }
    public int getRowHeight() {
        return mRowHeight;
    }

    /**
     * @param gap 行距(dp)
     **/
    public void setRowGap(int gap) {
        this.mRowGap = CBarrageUtil.dip2px(getContext(), gap);
        createRowsIfNotExist();
    }
    public int getRowGap() {
        return mRowGap;
    }

    public void setRowNum(int num) {
        mRowNum = num;
        createRowsIfNotExist();
    }
    public List<CBarrageRow> getRows() {
        return mRows;
    }

    /**
     * @param gap 弹幕间距(dp)
     **/
    public void setItemGap(int gap) {
        mItemGap = CBarrageUtil.dip2px(getContext(), gap);
        createRowsIfNotExist();
    }
    public int getItemGap() {
        return mItemGap;
    }


    private void createRowsIfNotExist() {
        if (mRows.size() < mRowNum) {
            for (int i = 0; i < mRowNum - mRows.size(); ++i) {
                CBarrageRow row = new CBarrageRow();
                mRows.add(row);
            }
        }
        for (int i = 0; i < mRows.size(); ++i) {
            CBarrageRow row = mRows.get(i);
            row.setWidth(getWidth());
            row.setHeight(mRowHeight);

            row.setContainerView(this);
            row.setItemSpeed(mRowSpeed);
            row.setItemGap(mItemGap);

            row.setRowIndex(i);
            row.setRowTop(getRowTopByIndex(i));
            row.setRowBottom(row.getRowTop()+mRowHeight);

            row.setRowListener(mRowListener);
        }
    }

    private int getRowTopByIndex(int index) {
        return index * (mRowHeight + mRowGap);
    }

    // //

    public CBarrageView(@NonNull Context context) {
        super(context);
        initView();
    }

    public CBarrageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        getViewTreeObserver().addOnGlobalLayoutListener(observer);
    }

    public void start() {
        mIsStarted = true;
        if (mIsPrepared && mPendingQueue.isEmpty() == false) {
            addBarrage(mPendingQueue.poll());
        }
    }

    public void clear() {
        mPendingQueue.clear();
        for (int i = 0; i < mRows.size(); ++i) {
            mRows.get(i).clear();
        }
    }

    public void onRowIdle(CBarrageRow row) {
        if (mIsPrepared == false || mIsStarted == false) {
            return;
        }
        if (mPendingQueue.isEmpty()) {
            return;
        } else {
            addBarrageToRow(row, mPendingQueue.poll());
        }
    }

    public void onLayoutFinish() {
        mIsPrepared = true;

        if (mListener != null) {
            mListener.onPrepared(this);
        }
        // start before prepared
        if (mIsStarted) {
            if (mPendingQueue.isEmpty() == false) {
                addBarrage(mPendingQueue.poll());
            }
        }
    }

    /**
     * add a barrage
     **/
    public void addBarrage(View view) {
        Log.d(TAG, "add pendingsize "+mPendingQueue.size());
        if (mIsStarted == false || mIsPrepared == false) {
            mPendingQueue.add(view);
            return;
        }
        if (mPendingQueue.isEmpty() == false) {
            mPendingQueue.add(view);
            return;
        }
        CBarrageRow row = getIdleRow();
        if (row == null) {
            Log.d(TAG, "add pendingsize row is null");
            mPendingQueue.add(view);
            return;
        }
        Log.d(TAG, "start");
        addBarrageToRow(row, view);
    }

    private void addBarrageToRow(CBarrageRow row, View view) {
        row.appendItem(view);
    }

    private CBarrageRow getIdleRow() {
        for (int i = 0; i < mRows.size(); ++i) {
            CBarrageRow row = mRows.get(i);
            if (row.isIdle()) {
                return row;
            }
        }
        return null;
    }


    private static class TreeObserver implements ViewTreeObserver.OnGlobalLayoutListener {
        private WeakReference<CBarrageView> mView = new WeakReference<CBarrageView>(null);

        public TreeObserver(CBarrageView view) {
            mView = new WeakReference<CBarrageView>(view);
        }
        @Override
        public void onGlobalLayout() {
            if (mView.get() != null) {
                // only trigger once
                mView.get().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mView.get().onLayoutFinish();
            }
        }
    }

    private static class RowListener implements CBarrageRow.BarrageRowListener {
        private WeakReference<CBarrageView> mView = new WeakReference<CBarrageView>(null);

        public RowListener(CBarrageView view) {
            mView = new WeakReference<CBarrageView>(view);
        }

        @Override
        public void onRowIdle(CBarrageRow row) {
            if (mView.get() != null) {
                mView.get().onRowIdle(row);
            }
        }
    }
}
