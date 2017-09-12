package com.jokin.cbarrage.cbarrage;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * 1. 弹幕
 * 2. 霸屏弹幕动画
 * Created by jokinkuang on 2017/9/8.
 */

public class CBarrageView extends FrameLayout {
    private static final String TAG = "CBarrageView";

    private List<CBarrageRow> mRows = new ArrayList<>(20);
    private Queue<View> mPendingQueue = new ArrayDeque<>(100);
    private Queue<View> mPendingPriorityQueue = new ArrayDeque<>(100);
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

    public static final int NORMAL = 0;
    public static final int AVERAGE = 1;
    private int mBarrageMode = NORMAL;

    private boolean mIsStarted;
    private boolean mIsPrepared;

    private int mRowNum = 1;
    private int mRowGap;
    private int mRowHeight;
    private int mRowSpeed;

    private int mItemGap;
    private int mItemGravity;


    /**
     * @param mode 设置弹幕的布局方式 正常/平均
     **/
    public void setMode(int mode) {
        mBarrageMode = mode;
    }

    public int getMode() {
        return mBarrageMode;
    }

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

    /**
     * Default is 1
     * @param num
     */
    public void setRowNum(int num) {
        if (num < 1) {
            return;
        }
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


    /**
     * @param gravity Gravity.TOP / Gravity.CENTER / Gravity.BOTTOM
     */
    public void setItemGravity(int gravity) {
        mItemGravity = gravity;
    }
    public int getItemGravity() {
        return mItemGravity;
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
            row.setItemGravity(mItemGravity);

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
        createRowsIfNotExist();
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
        if (! mPendingPriorityQueue.isEmpty()) {
            addBarrageToRow(row, mPendingPriorityQueue.poll());
            return;
        }
        if (! mPendingQueue.isEmpty()) {
            addBarrageToRow(row, mPendingQueue.poll());
            return;
        }
    }

    public void onLayoutFinish() {
        mIsPrepared = true;

        if (mListener != null) {
            mListener.onPrepared(this);
        }
        // start before prepared
        if (mIsStarted) {
            if (! mPendingPriorityQueue.isEmpty()) {
                addPriorityBarrage(mPendingPriorityQueue.poll());
                return;
            }
            if (! mPendingQueue.isEmpty()) {
                addBarrage(mPendingQueue.poll());
                return;
            }
        }
    }

    /**
     * add a barrage normal
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

    /**
     * add a more high level barrage which would be added as fast
     **/
    public void addPriorityBarrage(View view) {
        if (mIsStarted == false || mIsPrepared == false) {
            mPendingPriorityQueue.add(view);
            return;
        }
        if (mPendingPriorityQueue.isEmpty() == false) {
            mPendingPriorityQueue.add(view);
            return;
        }
        CBarrageRow row = getIdleRow();
        if (row == null) {
            Log.d(TAG, "add pendingsize row is null");
            mPendingPriorityQueue.add(view);
            return;
        }
        Log.d(TAG, "start");
        addBarrageToRow(row, view);
    }

    private void addBarrageToRow(CBarrageRow row, View view) {
        if (mBarrageMode == NORMAL) {
            row.appendItem(view);
        } else {
            row = getInsertRow();
            row.appendItem(view);
        }
    }

    /**
     * 一奇葩需求，动画要消失到插入的行，但事实上，如果插入非常快，插入的动画形成队列，
     * 此时要预判队列里所有动画要插入的行，这非常困难，因为下一次插入的行，与队列中前面的弹幕的宽度相关。
     * 宽度越长占用的行的时间越长。所以每个弹幕需要计算队列前面所有弹幕消失时机，虽然能够做到，但这种策略太复杂。
     * 所以，干脆就让每行维护一个优先队列。
     **/
    public CBarrageRow addBarrageToRow(View view) {
        if (mRows.isEmpty()) {
            Log.e(TAG, "fetal error!!! should not be here.");
            return null;
        }
        if (mIsStarted == false || mIsPrepared == false) {
            mRows.get(0).appendPriorityItem(view);
            return mRows.get(0);
        }
        return null;
    }

    /**
     * 非正常弹幕的接口，要平均弹幕到各行，优先级为：
     * 1. 行空闲才能插入
     * 2. 空闲的行中，行弹幕数量少的优先插入
     * 3. 数量相等，随机（坑爹）
     **/
    private CBarrageRow getInsertRow() {
        Log.d(TAG, "rows:"+mRows.size());
        List<CBarrageRow> rows = getIdleRows();
        Log.d(TAG, "idle: "+rows);
        if (! rows.isEmpty()) {
            rows = getLessItemRowsInIdle(rows);
            Log.d(TAG, "less: "+rows);
            if (rows.size() == 1) {
                return rows.get(0);
            }
            // scale 10 times to make random more random
            return rows.get(getRandomInt(0, rows.size()*10-1) / 10);
        } else {
            // would not be here !!
            Log.e(TAG, "fetal error! should not be here!");
            return null;
        }
    }

    /**
     * 动画需要预知下一次插入的行
     * @deprecated 接口不成立，短时间内会返回同一行，不准确。废弃。
     **/
    public CBarrageRow peekNextIdleRow() {
        CBarrageRow row = getIdleRow();
        if (row != null) {
            return row;
        } else {
            if (mRows.isEmpty()) {
                return null;
            }
            if (mRows.size() == 1) {
                return mRows.get(0);
            }
            CBarrageRow nextIdleRow = mRows.get(0);
            for (int i = 1; i < mRows.size(); ++i) {
                row = mRows.get(i);
                if (row.peekNextIdleTime() < nextIdleRow.peekNextIdleTime()) {
                    nextIdleRow = row;
                }
            }
            return nextIdleRow;
        }
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

    // 优先级接口 //

    private List<CBarrageRow> getIdleRows() {
        List<CBarrageRow> rows = new ArrayList<>(10);
        for (int i = 0; i < mRows.size(); ++i) {
            CBarrageRow row = mRows.get(i);
            if (row.isIdle()) {
                rows.add(row);
            }
        }
        return rows;
    }

    private List<CBarrageRow> getLessItemRowsInIdle(List<CBarrageRow> rows) {
        List<CBarrageRow> minRows = new ArrayList<>(10);
        if (rows == null || rows.isEmpty()) {
            return minRows;
        }

        minRows.add(rows.get(0));
        for (int i = 1; i < rows.size(); ++i) {
            CBarrageRow row = rows.get(i);
            if (row.getItemCount() == minRows.get(0).getItemCount()) {
                minRows.add(row);
            } else if (row.getItemCount() < minRows.get(0).getItemCount()){
                minRows.clear();
                minRows.add(row);
            }
        }
        return minRows;
    }

    /**
     * @param min
     * @param max if max > min, max would be reset to min
     * @return [min,max]
     */
    private int getRandomInt(int min, int max) {
        if (min >= max) {
            max = min;
        }
        if (min < 0 || max <= 0) {
            return 0;
        }
        int a =  new Random().nextInt(max)%(max-min+1) + min;
        Log.d(TAG, String.format("max %d int %d a %d", max, min, a));
        return a;
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


    /**
     * For Debug
     */
    public void dumpMemory() {
        String TAG = "dump";
        Log.d(TAG, "*************** Dump Memory **************");
        Log.d(TAG, String.format("Barrage children view count %d", getChildCount()));
        Log.d(TAG, String.format("pendingQueueSize %d pendingPriorityQueueSize %d ",
                mPendingQueue.size(), mPendingPriorityQueue.size()));

        Log.d(TAG, String.format("Barrage rows count %d", mRows.size()));
        for (int i = 0; i < mRows.size(); ++i) {
            CBarrageRow row = mRows.get(i);
            row.dumpMemory();
        }
        Log.d(TAG, "*************** End **************");
    }
}
