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

    private List<BarrageRow> mRows = new ArrayList<>(20);
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


    public void setRowSpeed(int speed) {
        this.mRowSpeed = speed;
        createRowsIfNotExist();
    }
    public int getRowSpeed() {
        return mRowSpeed;
    }

    public void setRowHeight(int height) {
        this.mRowHeight = height;
        createRowsIfNotExist();
    }
    public int getRowHeight() {
        return mRowHeight;
    }

    public void setRowGap(int gap) {
        this.mRowGap = gap;
        createRowsIfNotExist();
    }
    public int getRowGap() {
        return mRowGap;
    }

    public void setRowNum(int num) {
        mRowNum = num;
        createRowsIfNotExist();
    }
    public List<BarrageRow> getRows() {
        return mRows;
    }


    public void setItemGap(int gap) {
        mItemGap = gap;
        createRowsIfNotExist();
    }
    public int getItemGap() {
        return mItemGap;
    }


    private void createRowsIfNotExist() {
        if (mRows.size() < mRowNum) {
            for (int i = 0; i < mRowNum - mRows.size(); ++i) {
                BarrageRow row = new BarrageRow();
                mRows.add(row);
            }
        }
        for (int i = 0; i < mRows.size(); ++i) {
            BarrageRow row = mRows.get(i);
            row.setWidth(getWidth());
            row.setHeight(mRowHeight);

            row.setContainerView(this);
            row.setItemSpeed(mRowSpeed);
            row.setItemGap(mItemGap);

            row.setRowListener(mRowListener);
        }
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

    public void onRowIdle(BarrageRow row) {
        Log.d(TAG, "idle pendingsize "+mPendingQueue.size());
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
        if (mIsStarted == false || mIsPrepared == false) {
            mPendingQueue.add(view);
            return;
        }
        if (mPendingQueue.isEmpty() == false) {
            mPendingQueue.add(view);
            return;
        }
        BarrageRow row = getIdleRow();
        if (row == null) {
            mPendingQueue.add(view);
            return;
        }
        addBarrageToRow(row, view);
    }

    private void addBarrageToRow(BarrageRow row, View view) {
        row.appendItem(view);
    }

    private BarrageRow getIdleRow() {
        for (int i = 0; i < mRows.size(); ++i) {
            BarrageRow row = mRows.get(i);
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

    private static class RowListener implements BarrageRow.BarrageRowListener {
        private WeakReference<CBarrageView> mView = new WeakReference<CBarrageView>(null);

        public RowListener(CBarrageView view) {
            mView = new WeakReference<CBarrageView>(view);
        }

        @Override
        public void onRowIdle(BarrageRow row) {
            if (mView.get() != null) {
                mView.get().onRowIdle(row);
            }
        }
    }
}
