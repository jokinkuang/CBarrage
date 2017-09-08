package com.jokin.cbarrage.cbarrage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;

/**
 * Created by jokinkuang on 2017/9/8.
 */

public class BarrageItem {
    private BarrageRow mRow;

    private WeakReference<View> mContentView;
    private RLAnimator mRLAnimator = new RLAnimator();
    private ObjectAnimator mAnimator = new ObjectAnimator();
    private AnimatorListener mAnimatorListener = new AnimatorListener();


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
    protected int mDuration = 0;

    public void setRow(BarrageRow row) {
        mRow = row;
    }
    @Nullable
    public BarrageRow getRow() {
        return mRow;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        this.mDistance = distance;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void recycle() {
        mContentView = null;
        mAnimator.cancel();
    }

    public void onUpdate() {
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

    public void start() {
        if (mContentView == null || mContentView.get() == null) {
            return;
        }

        mAnimator.setTarget(mContentView.get());
        mAnimator.setPropertyName("translationX");
        mAnimator.setFloatValues(mDistance, 0);
        mAnimator.setDuration(mDuration);
        mAnimator.setInterpolator(new LinearInterpolator());

        mAnimator.addUpdateListener(mAnimatorListener);
        mAnimator.addListener(mAnimatorListener);

        mAnimator.start();
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


    private class AnimatorListener extends AnimatorListenerAdapter
            implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            onUpdate();
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
