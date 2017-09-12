package com.jokin.cbarrage.cbarrage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;

/**
 * Created by jokinkuang on 2017/9/8.
 */

public class CBarrageItem {
    private static final String TAG = "CBarrageItem";

    private CBarrageRow mRow;
    private WeakReference<View> mContentView;
    private ObjectAnimator mAnimator = new ObjectAnimator();
    private AnimatorListener mAnimatorListener = new AnimatorListener();
    private TreeObserver observer = new TreeObserver(this);


    public interface BarrageItemListener {
        void onAnimationCancel(CBarrageItem item);
        void onAnimationEnd(CBarrageItem item);
        void onAnimationRepeat(CBarrageItem item);
        void onAnimationStart(CBarrageItem item);
        void onAnimationPause(CBarrageItem item);
        void onAnimationResume(CBarrageItem item);
        void onAnimationUpdate(CBarrageItem item);
    }
    private BarrageItemListener mListener;
    public void setListener(BarrageItemListener listener) {
        mListener = listener;
    }

    protected int mDistance = 0;
    protected int mSpeed = 0;
    protected int mGravity = 0;

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        this.mDistance = distance;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
    }

    public int getGravity() {
        return mGravity;
    }

    public void setRow(CBarrageRow row) {
        mRow = row;
    }
    @Nullable
    public CBarrageRow getRow() {
        return mRow;
    }

    public void setContentView(View view) {
        mContentView = new WeakReference<View>(view);
    }
    @Nullable
    public View getContentView() {
        if (mContentView == null) {
            return null;
        }
        return mContentView.get();
    }

    public CBarrageItem() {
        mAnimator.addUpdateListener(mAnimatorListener);
        mAnimator.addListener(mAnimatorListener);
        mAnimator.setInterpolator(new LinearInterpolator());
    }

    public void clear() {
        mContentView = null;
        mAnimator.cancel();
    }

    public void start() {
        if (mContentView == null || mContentView.get() == null) {
            Log.e(TAG, "fetal error. content view is null");
            return;
        }
        mContentView.get().getViewTreeObserver().addOnGlobalLayoutListener(observer);
    }

    private void realStart() {
        if (mContentView == null || mContentView.get() == null) {
            Log.e(TAG, "fetal error. content view is null");
            return;
        }

        mContentView.get().setY(getTopByGravity(mGravity));

        mAnimator.setTarget(mContentView.get());
        mAnimator.setPropertyName("translationX");
        mAnimator.setFloatValues(mDistance, -mContentView.get().getWidth());
        mAnimator.setDuration(getDurationBySpeed(mSpeed));

        mAnimator.start();
    }

    /**
     * 坐标 相对于 当前行的Top或Bottom 定位
     **/
    private int getTopByGravity(int gravity) {
        switch (gravity) {
            case Gravity.TOP:
                return mRow.getTop();
            case Gravity.BOTTOM:
                return mRow.getBottom() - mContentView.get().getHeight();
            case Gravity.CENTER:
            default:
                return mRow.getTop() + (mRow.getHeight() - mContentView.get().getHeight())/2;
        }
    }

    /**
     * 不同宽度的物体，划过同一个窗口，规定了总时间，以此获取对应的速度
     **/
    private long getDurationBySpeed(int speed) {
        return (long) ((mDistance + mContentView.get().getWidth()) / (mDistance*1.0) * speed);
    }

    public void pause() {
        mAnimator.pause();
    }

    public void resume() {
        mAnimator.resume();
    }

    public void cancel() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    public boolean isStarted() {
        if (mAnimator == null) {
            return false;
        } else {
            return mAnimator.isStarted();
        }
    }

    public boolean isPaused() {
        if (mAnimator == null) {
            return false;
        } else {
            return mAnimator.isPaused();
        }
    }

    public void onLayoutFinish() {
        realStart();
    }


    private static class TreeObserver implements ViewTreeObserver.OnGlobalLayoutListener {
        private WeakReference<CBarrageItem> mItem = new WeakReference<CBarrageItem>(null);

        public TreeObserver(CBarrageItem view) {
            mItem = new WeakReference<CBarrageItem>(view);
        }
        @Override
        public void onGlobalLayout() {
            if (mItem.get() != null) {
                // only trigger once
                mItem.get().getContentView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mItem.get().onLayoutFinish();
                return;
            }
            Log.d(TAG, "fetal error!!!");
        }
    }

    private class AnimatorListener extends AnimatorListenerAdapter
            implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mListener != null) {
                mListener.onAnimationUpdate(CBarrageItem.this);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationCancel(CBarrageItem.this);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationEnd(CBarrageItem.this);
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationRepeat(CBarrageItem.this);
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationStart(CBarrageItem.this);
            }
        }

        @Override
        public void onAnimationPause(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationPause(CBarrageItem.this);
            }
        }

        @Override
        public void onAnimationResume(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationResume(CBarrageItem.this);
            }
        }
    }
}
