package com.jokin.cbarrage.cbarrage;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by jokinkuang on 2017/9/8.
 */

public class RLAnimator {
    private static final String TAG = "RLAnimator";

    private int mDistance = 0;
    private int mDuration = 0;
    private WeakReference<View> mTarget;
    private ObjectAnimator mAnimator = new ObjectAnimator();


    private RLAnimatorListener mListener;
    public void setAnimatorListener(RLAnimatorListener listener) {
        mListener = listener;
    }

    public RLAnimator() {
    }

    public void setTarget(View target) {
        mTarget = new WeakReference<View>(target);
    }

    public View getTarget() {
        if (mTarget == null || mTarget.get() == null) {
            return null;
        }
        return mTarget.get();
    }

    public void recycle() {
        mTarget = null;
        mAnimator.cancel();
    }

    public void start() {
        if (mTarget == null || mTarget.get() == null) {
            return;
        }

        mAnimator.setTarget(mTarget.get());
        mAnimator.setPropertyName("translationX");
        mAnimator.setFloatValues(-mDistance, 0);
        mAnimator.setDuration(mDuration);

        mAnimator.addUpdateListener(mListener);
        mAnimator.addListener(mListener);

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

    @Nullable
    public Animator getRealAnimator() {
        return mAnimator;
    }


    public abstract class RLAnimatorListener implements Animator.AnimatorListener,
            Animator.AnimatorPauseListener, ValueAnimator.AnimatorUpdateListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationCancel(Animator animation) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationEnd(Animator animation) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationStart(Animator animation) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationPause(Animator animation) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationResume(Animator animation) {
        }
    }
}
