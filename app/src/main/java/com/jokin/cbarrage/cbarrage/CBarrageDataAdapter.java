package com.jokin.cbarrage.cbarrage;

import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * Created by jokinkuang on 2017/9/13.
 */

public abstract class CBarrageDataAdapter<T> {
    private WeakReference<CBarrageView> mBarrageView = new WeakReference<CBarrageView>(null);
    void setBarrageView(CBarrageView view) {
        mBarrageView = new WeakReference<CBarrageView>(view);
    }
    public CBarrageView getBarrageView() {
        return mBarrageView.get();
    }


    public abstract View createView(ViewGroup root, View convertView, T obj);
    public abstract void destroyView(ViewGroup root, T obj, View view);
    public abstract boolean isViewFromObject(View view, T obj);

    public void addBarrage(T obj) {
        if (mBarrageView.get() == null) {
            return;
        }
        mBarrageView.get().addBarrage(obj);
    }

    public void addPriorityBarrage(T obj) {
        if (mBarrageView.get() == null) {
            return;
        }
        mBarrageView.get().addPriorityBarrage(obj);
    }

    public void addRowBarrage(T obj) {
        if (mBarrageView.get() == null) {
            return;
        }
        mBarrageView.get().addRowBarrage(obj);
    }

    public void addBarrageToRow(int rowIndex, T obj) {
        if (mBarrageView.get() == null) {
            return;
        }
        mBarrageView.get().addBarrageToRow(rowIndex, obj);
    }
}
