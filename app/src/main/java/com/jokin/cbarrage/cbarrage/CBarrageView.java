package com.jokin.cbarrage.cbarrage;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.SystemClock;
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
import java.util.Random;

/**
 * 1. 弹幕(自然布局|平均布局)
 * 2. 霸屏弹幕动画
 * 3. 弹幕循环（需要外部提供用于循环的数组，因为内部不保存已结束的弹幕）
 * Created by jokinkuang on 2017/9/8.
 */

public class CBarrageView extends FrameLayout {
    private static final String TAG = "CBarrageView";

    private List<CBarrageRow> mRows = new ArrayList<>(20);
    private Queue<Object> mPendingQueue = new ArrayDeque<>(100);
    private Queue<Object> mPendingPriorityQueue = new ArrayDeque<>(100);
    private TreeObserver observer = new TreeObserver(this);
    private CBarrageDataAdapter mAdapter;
    private CRecycleBin mRecycleBin = new CRecycleBin();

    private boolean mIsLoopingMode;
    private static final long LoopInterval = 100;
    private Queue<Object> mLoopQueue = new ArrayDeque<>();
    private static final long MAX_IDLE_TIME = 1*60*1000;   // 空闲时间要比一条弹幕的动画时间长！
    private boolean mIsIdleTimerStarted = false;
    private CountDownTimer mIdleCountDownTimer = new CountDownTimer(MAX_IDLE_TIME, MAX_IDLE_TIME) {
        @Override
        public void onTick(long l) {
        }
        @Override
        public void onFinish() {
            // @bugfix reset boolean when finish
            mIsIdleTimerStarted = false;
            mIsLoopingMode = true;
            if (mListener != null) {
                mListener.onIdle(MAX_IDLE_TIME, CBarrageView.this);
            }
            startLoop();
        }
    };
    private void startLoop() {
        onRowIdle(null);
    }

    private RowListener mRowListener = new RowListener(this);

    public interface CBarrageViewListener {
        /**
         * should init in prepared
         **/
        void onPrepared(CBarrageView view);

        /**
         * 弹幕空闲Nms后回调，在此函数设置循环数组
         * should set loop queue here!!!
         */
        void onIdle(long idleTimeMs, CBarrageView view);
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
    private boolean mIsPaused;
    private boolean mIsReleasing;

    private int mRowNum = 1;
    private int mRowGap;
    private int mRowHeight;
    private int mRowSpeed;

    private int mItemGap;
    private int mItemGravity;


    /**
     * @param mode 设置弹幕的布局方式 正常(default)/平均
     **/
    public void setMode(int mode) {
        mBarrageMode = mode;
    }

    public int getMode() {
        return mBarrageMode;
    }

    public void setAdapter(CBarrageDataAdapter adapter) {
        mAdapter = adapter;
        mAdapter.setBarrageView(this);
    }
    public CBarrageDataAdapter getAdapter() {
        return mAdapter;
    }

    public void setLoopQueue(List list) {
        mLoopQueue = new ArrayDeque<>(list);
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
     * @param gravity Gravity.TOP / Gravity.CENTER(default) / Gravity.BOTTOM
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
            row.setContainerView(this);
            row.setBarrageView(this);

            row.setIndex(i);
            row.setWidth(getWidth());
            row.setHeight(mRowHeight);
            row.setLeft(getLeft());
            row.setRight(getRight());
            row.setTop(getRowTopByIndex(i));
            row.setBottom(row.getTop()+mRowHeight);

            row.setItemSpeed(mRowSpeed);
            row.setItemGap(mItemGap);
            row.setItemGravity(mItemGravity);

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
        postDelayed(mCheckRowIdleTask, 50);
    }

    private Runnable mCheckRowIdleTask = new Runnable() {
        @Override
        public void run() {
            if (mIsReleasing) {
                return;
            }
            checkRowIdle();
            postDelayed(this, 50);
        }
    };

    private void checkRowIdle() {
        onRowIdle(null);
    }

    public boolean isPaused() {
        return mIsPaused;
    }

    public boolean isStarted() {
        return mIsStarted;
    }

    public boolean isLooping() {
        return mIsLoopingMode;
    }

    public void start() {
        mIsStarted = true;
        if (mIsPrepared && mPendingQueue.isEmpty() == false) {
            addBarrage(mPendingQueue.poll());
        }
    }

    // 如果为true，表示暂停不会立即暂停，会等待当前屏幕中的弹幕消失；为false，表示立即暂停
    private static final boolean FitPC = false;
    public void pause() {
        Log.d(TAG, "stop");
        if (FitPC) {
            pauseLikePC();
            return;
        }
        mIsPaused = true;
        for (int i = 0; i < mRows.size(); ++i) {
            mRows.get(i).pause();
        }
    }

    public void resume() {
        if (FitPC) {
            resumeLikePC();
            return;
        }
        mIsPaused = false;
        for (int i = 0; i < mRows.size(); ++i) {
            mRows.get(i).resume();
        }
    }

    void pauseLikePC() {
        mIsPaused = true;
        // 为了和PC统一，让动画走完
    }

    void resumeLikePC() {
        // @bugfix 曾经暂停过，会导致空闲循环停止触发，主动开启
        Log.d(TAG, "pause "+mIsPaused+"|"+" timerstared "+mIsIdleTimerStarted);
        mIsPaused = false;
        if (! mIsIdleTimerStarted) {
            Log.d(TAG, "start");
            mIdleCountDownTimer.start();
            mIsIdleTimerStarted = true;

            onRowIdle(null);
        }
    }

    public void clear() {
        mPendingQueue.clear();
        mPendingPriorityQueue.clear();
        for (int i = 0; i < mRows.size(); ++i) {
            mRows.get(i).clear();
        }
        removeAllViews();
    }

    public void release() {
        mIsReleasing = true;
        removeCallbacks(mCheckRowIdleTask);
        // clear would release barrage view animations
        clear();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIdleCountDownTimer.cancel();
        mIsIdleTimerStarted = false;
    }

    private long lastTime = 0;
    /**
     * Idle row callback, show next.
     * @param row if row is null, means check and get the idle row inside.
     **/
    public void onRowIdle(@Nullable CBarrageRow row) {
        row = getIdleRow();
        if (row == null) {
            return;
        }
        row.onItemUpdate(null);

        if (mIsPrepared == false || mIsStarted == false || mIsPaused) {
            return;
        }
        if (! mPendingPriorityQueue.isEmpty()) {
            // IdleRow would not be null here!
            addBarrageToRow(row, mPendingPriorityQueue.poll());
            return;
        }
        if (! mPendingQueue.isEmpty()) {
            // IdleRow would not be null here!
            addBarrageToRow(row, mPendingQueue.poll());
            return;
        }
        if (mIsLoopingMode) {
            // loop mode
            long currentTime = SystemClock.currentThreadTimeMillis();
            if (mLoopQueue.isEmpty() || SystemClock.currentThreadTimeMillis() - lastTime < LoopInterval) {
                return;
            } else {
                lastTime = currentTime;
                addBarrageToRowForLoop(row, mLoopQueue.poll());
            }
            return;
        }
        // All Idle
        if (! mIsIdleTimerStarted) {
            Log.d(TAG, "idle timer start");
            mIdleCountDownTimer.start();
            mIsIdleTimerStarted = true;
        }
    }

    public View onViewCreate(CBarrageRow row, Object obj) {
        if (mAdapter == null) {
            return null;
        }
        View view = mAdapter.createView(this, getViewFromCache(obj), obj);
        if (view == null) {
            return null;
        }
        // reset
        view.setX(0);
        view.setY(0);

        // add view to container
        if (view.getParent() != this) {
            addView(view);
        }
        return view;
    }

    private View getViewFromCache(Object obj) {
        if (mAdapter == null) {
            return null;
        }
        for (int i = 0; i < mRecycleBin.size(); ++i) {
            if (mAdapter.isViewFromObject(mRecycleBin.peek(i), obj)) {
                return mRecycleBin.get(i);
            }
        }
        return null;
    }

    public void onViewDestroy(CBarrageRow row, Object obj, View view) {
        if (mAdapter == null) {
            return;
        }
        Log.d(TAG, "loopmode " + mIsLoopingMode);
        if (mIsLoopingMode) {
            mLoopQueue.add(obj);
            onRowIdle(null);
        }
        // remove view from container
        // removeView(view);
        mRecycleBin.add(view);
        mAdapter.destroyView(this, obj, view);
    }

    public void onLayoutFinish() {
        mIsPrepared = true;

        // make sure row width is the same as view
        createRowsIfNotExist();

        if (mListener != null) {
            mListener.onPrepared(this);
        }
        // if user start before prepared callback
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
    void addBarrage(Object obj) {
        Log.d(TAG, "add pendingsize "+mPendingQueue.size());
        if (mIsStarted == false || mIsPrepared == false || mIsPaused) {
            mPendingQueue.add(obj);
            return;
        }
        if (mPendingQueue.isEmpty() == false) {
            mPendingQueue.add(obj);
            return;
        }
        CBarrageRow row = getIdleRow();
        if (row == null) {
            Log.d(TAG, "add pendingsize row is null");
            mPendingQueue.add(obj);
            return;
        }
        Log.d(TAG, "start");
        addBarrageToRow(row, obj);
    }

    /**
     * add a more high level barrage which would be added as fast
     **/
    void addPriorityBarrage(Object obj) {
        if (mIsStarted == false || mIsPrepared == false || mIsPaused) {
            mPendingPriorityQueue.add(obj);
            return;
        }
        if (mPendingPriorityQueue.isEmpty() == false) {
            mPendingPriorityQueue.add(obj);
            return;
        }
        CBarrageRow row = getIdleRow();
        if (row == null) {
            Log.d(TAG, "add pendingsize row is null");
            mPendingPriorityQueue.add(obj);
            return;
        }
        Log.d(TAG, "start");
        addBarrageToRow(row, obj);
    }

    @NonNull
    public CBarrageRow peekNextInsertRow() {
        CBarrageRow row = getIdleRow();
        if (row != null) {
            return row;
        } else {
            // no idle rows add to queue.
            List<CBarrageRow> rows = getMinimumPendingRowsInRows(mRows);
            if (rows.size() == 1) {
                row = rows.get(0);
            } else {
                // scale 10 times to make random more random
                row = rows.get(getRandomInt(0, rows.size() * 10 - 1) / 10);
            }
            return row;
        }
    }

    /**
     * add the barrage to the peek row !!!
     * @param rowIndex
     * @param obj
     */
    void addBarrageToRow(int rowIndex, Object obj) {
        mIdleCountDownTimer.cancel();
        mIsIdleTimerStarted = false;
        mIsLoopingMode = false;

        if (rowIndex < 0 || rowIndex >= mRows.size()) {
            return;
        }
        CBarrageRow row = mRows.get(rowIndex);
        if (row == null) {
            return;
        }
        row.appendPriorityItem(obj);
    }

    /**
     * 一奇葩需求，动画要消失到插入的行，但事实上，非常复杂，当插入的动画形成队列，
     * 此时要预判队列里所有动画要插入的行，这非常困难，因为下一次插入的行，与队列中前面的弹幕的宽度相关。
     * 宽度越长占用的行的时间越长。所以每个弹幕需要计算队列前面所有弹幕消失时机，虽然能够做到，但这种策略太复杂。
     * 所以，干脆就让每行维护一个优先队列。
     * 1. 有空闲行，直接插入
     * 2. 没空闲，行队列数量最少的插入
     **/
    CBarrageRow addRowBarrage(Object obj) {
        CBarrageRow row = getIdleRow();
        if (row != null) {
            if (mIsStarted == false || mIsPrepared == false || mIsPaused) {
                row.appendPriorityItem(obj);
            } else {
                // show directly
                addBarrageToRow(row, obj);
            }
        } else {
            // no idle rows add to queue.
            List<CBarrageRow> rows = getMinimumPendingRowsInRows(mRows);
            if (rows.size() == 1) {
                row = rows.get(0);
            } else {
                // scale 10 times to make random more random
                row = rows.get(getRandomInt(0, rows.size() * 10 - 1) / 10);
            }
            if (mIsStarted == false || mIsPrepared == false || mIsPaused) {
                row.appendPriorityItem(obj);
            } else {
                row.appendPriorityItem(obj);
            }
        }
        return row;
    }


    /**
     * 动画需要预知下一次插入的行
     * @deprecated 接口不成立，短时间内会返回同一行，不准确。废弃。
     **/
    public CBarrageRow peekNextIdleRow() {
        return null;
    }

    private void addBarrageToRow(CBarrageRow row, Object obj) {
        mIdleCountDownTimer.cancel();
        mIsIdleTimerStarted = false;
        mIsLoopingMode = false;
        row.appendItem(obj);
    }

    private void addBarrageToRowForLoop(CBarrageRow row, Object obj) {
        mIdleCountDownTimer.cancel();
        mIsIdleTimerStarted = false;
        row.appendItem(obj);
    }

    /**
     * according to the BarrageMode to get an idle row
     * @return null if no idle row
     */
    private CBarrageRow getIdleRow() {
        if (mBarrageMode == NORMAL) {
            return getFirstIdleRow();
        } else {
            return getHighestPriorityIdleRow();
        }
    }

    // 优先级接口 //

    private CBarrageRow getFirstIdleRow() {
        for (int i = 0; i < mRows.size(); ++i) {
            CBarrageRow row = mRows.get(i);
            if (row.isIdle()) {
                return row;
            }
        }
        return null;
    }

    /**
     * 非正常弹幕的接口，要平均弹幕到各行，优先级为：
     * 1. 行空闲才能插入
     * 2. 空闲的行中，行弹幕数量少的优先插入
     * 3. 数量相等，随机（坑爹）
     **/
    private CBarrageRow getHighestPriorityIdleRow() {
        List<CBarrageRow> rows = getIdleRowsInRows(mRows);
        if (! rows.isEmpty()) {
            rows = getMinimumItemRowsInRows(rows);
            if (rows.size() == 1) {
                return rows.get(0);
            }
            // scale 10 times to make random more random
            return rows.get(getRandomInt(0, rows.size()*10-1) / 10);
        } else {
            return null;
        }
    }

    private List<CBarrageRow> getIdleRowsInRows(@NonNull List<CBarrageRow> sRows) {
        List<CBarrageRow> idleRows = new ArrayList<>(10);
        for (int i = 0; i < sRows.size(); ++i) {
            CBarrageRow row = sRows.get(i);
            if (row.isIdle()) {
                idleRows.add(row);
            }
        }
        return idleRows;
    }

    private List<CBarrageRow> getMinimumItemRowsInRows(@NonNull List<CBarrageRow> sRows) {
        List<CBarrageRow> minRows = new ArrayList<>(10);
        if (sRows == null || sRows.isEmpty()) {
            return minRows;
        }

        minRows.add(sRows.get(0));
        for (int i = 1; i < sRows.size(); ++i) {
            CBarrageRow row = sRows.get(i);
            if (row.getItemCount() == minRows.get(0).getItemCount()) {
                minRows.add(row);
            } else if (row.getItemCount() < minRows.get(0).getItemCount()){
                minRows.clear();
                minRows.add(row);
            }
        }
        return minRows;
    }

    private List<CBarrageRow> getMinimumPendingRowsInRows(@NonNull List<CBarrageRow> sRows) {
        List<CBarrageRow> minRows = new ArrayList<>(10);
        if (sRows == null || sRows.isEmpty()) {
            return minRows;
        }

        minRows.add(sRows.get(0));
        for (int i = 1; i < sRows.size(); ++i) {
            CBarrageRow row = sRows.get(i);
            if (row.getRowPendingSize() == minRows.get(0).getRowPendingSize()) {
                minRows.add(row);
            } else if (row.getRowPendingSize() < minRows.get(0).getRowPendingSize()){
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
        // NLog.d(TAG, String.format("max %d int %d a %d", max, min, a));
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
        public View onViewCreate(CBarrageRow row, Object obj) {
            if (mView.get() != null) {
                return mView.get().onViewCreate(row, obj);
            }
            return null;
        }

        @Override
        public void onViewDestroy(CBarrageRow row, Object obj, @NonNull View view) {
            if (mView.get() != null) {
                mView.get().onViewDestroy(row, obj, view);
            }
        }

        @Override
        public void onRowIdle(CBarrageRow row) {
            if (mView.get() != null) {
                mView.get().onRowIdle(row);
            }
        }
    }


    class CRecycleBin {
        private static final int MAX = 50;
        private List<View> mScrapHeap = new ArrayList<>(10);

        public void add(View v) {
            if (mScrapHeap.size() < MAX) {
                mScrapHeap.add(v);
            } else {
                for (int i = 0; i < MAX/2 && i < mScrapHeap.size(); ++i) {
                    mScrapHeap.remove(i);
                }
            }
        }

        public View get() {
            return get(0);
        }

        View peek(int position) {
            return mScrapHeap.get(position);
        }

        View get(int position) {
            if (position < 0 || position >= mScrapHeap.size()) {
                return null;
            }
            View result = mScrapHeap.get(position);
            if (result != null) {
                mScrapHeap.remove(position);
            } else {
            }
            return result;
        }

        int size() {
            return mScrapHeap.size();
        }

        void clear() {
            final List<View> scrapHeap = mScrapHeap;

            final int count = scrapHeap.size();
            for (int i = 0; i < count; i++) {
                final View view = scrapHeap.get(i);
                if (view != null) {
                    removeDetachedView(view, true);
                }
            }

            scrapHeap.clear();
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

        Log.d(TAG, String.format("Barrage recycleBin size %d", mRecycleBin.size()));
        for (int i = 0; i < mRecycleBin.size(); ++i) {
            Log.d(TAG, String.format("Item %d %s", i , mRecycleBin.peek(i)));
        }

        Log.d(TAG, String.format("Barrage rows count %d", mRows.size()));
        for (int i = 0; i < mRows.size(); ++i) {
            CBarrageRow row = mRows.get(i);
            row.dumpMemory();
        }
        Log.d(TAG, "*************** End **************");
    }
}
