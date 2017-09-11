package com.jokin.cbarrage.cbarrage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;

/**
 * Created by jokinkuang on 2017/9/8.
 */

public class BarrageItem {
    private static final String TAG = "BarrageItem";

    private BarrageRow mRow;
    private WeakReference<View> mContentView;
    private ObjectAnimator mAnimator = new ObjectAnimator();
    private AnimatorListener mAnimatorListener = new AnimatorListener();
    private TreeObserver observer = new TreeObserver(this);


    public interface BarrageItemListener {
        void onAnimationCancel(BarrageItem item);
        void onAnimationEnd(BarrageItem item);
        void onAnimationRepeat(BarrageItem item);
        void onAnimationStart(BarrageItem item);
        void onAnimationPause(BarrageItem item);
        void onAnimationResume(BarrageItem item);
        void onAnimationUpdate(BarrageItem item);
    }
    private BarrageItemListener mListener;
    public void setListener(BarrageItemListener listener) {
        mListener = listener;
    }

    protected int mDistance = 0;
    protected int mSpeed = 0;

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

    public void setRow(BarrageRow row) {
        mRow = row;
    }
    @Nullable
    public BarrageRow getRow() {
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

    public BarrageItem() {
        mAnimator.addUpdateListener(mAnimatorListener);
        mAnimator.addListener(mAnimatorListener);
        mAnimator.setInterpolator(new LinearInterpolator());
    }

    public void recycle() {
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

        mContentView.get().setY(mRow.getRowTop());

        mAnimator.setTarget(mContentView.get());
        mAnimator.setPropertyName("translationX");
        mAnimator.setFloatValues(mDistance, -mContentView.get().getWidth());
        mAnimator.setDuration(getDurationBySpeed(mSpeed));

        mAnimator.start();
    }

    /**
     * 不同宽度的物体，划过同一个窗口，规定了总时间，以此获取对应的速度
     **/
    private long getDurationBySpeed(int speed) {
        return (long) ((mDistance + mContentView.get().getWidth()) / (mDistance*1.0) * speed);
    }

    public void pause() {
        if (mAnimator != null) {
            mAnimator.pause();
        }
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
        private WeakReference<BarrageItem> mItem = new WeakReference<BarrageItem>(null);

        public TreeObserver(BarrageItem view) {
            mItem = new WeakReference<BarrageItem>(view);
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
                mListener.onAnimationUpdate(BarrageItem.this);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationCancel(BarrageItem.this);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationEnd(BarrageItem.this);
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationRepeat(BarrageItem.this);
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationStart(BarrageItem.this);
            }
        }

        @Override
        public void onAnimationPause(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationPause(BarrageItem.this);
            }
        }

        @Override
        public void onAnimationResume(Animator animation) {
            if (mListener != null) {
                mListener.onAnimationResume(BarrageItem.this);
            }
        }
    }
}
