package com.jokin.cbarrage;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jokin.cbarrage.cbarrage.CBarrageDataAdapter;

/**
 * Created by jokinkuang on 2017/9/13.
 */

public class BarrageDataAdapter extends CBarrageDataAdapter<Barrage> {
    private static final String TAG = "BarrageDataAdapter";

    @Override
    public View createView(ViewGroup root, View converView, Barrage obj) {
        if (Barrage.TEXT.equals(obj.type)) {
            return createTextBarrage(root, converView);
        } else if (Barrage.IMAGE.equals(obj.type)) {
            return createImageBarrage(root, converView);
        }
        return null;
    }

    @Override
    public void destroyView(ViewGroup root, Barrage obj, View view) {
        Log.d(TAG, "destroyView "+view);
    }

    @Override
    public boolean isViewFromObject(View view, Barrage obj) {
        return view.getTag().equals(obj.type);
    }

    private int num = 0;
    private View createTextBarrage(final ViewGroup root, View converView) {
        if (converView != null) {
            ((TextView)converView).setText("重用文本弹幕"+num);
            converView.setX(0);
            Log.d(TAG, String.format("x %f y %f l %d t %d r %d",
                    converView.getX(), converView.getY(), converView.getLeft(), converView.getTop(), converView.getRight()));
            return converView;
        }
        TextView textView = new TextView(root.getContext());
        num += 1;
        textView.setText("这是一条弹幕"+num);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(root.getContext(), "clicked", Toast.LENGTH_SHORT).show();
            }
        });
        textView.setTag(Barrage.TEXT);
        return textView;
    }

    private View createImageBarrage(ViewGroup root, View converView) {
        if (converView != null) {
            ((TextView)converView.findViewById(R.id.text)).setText("重用图片弹幕"+num);
            return converView;
        }
        View imageView = LayoutInflater.from(root.getContext()).inflate(R.layout.barrage_image, root, false);
        ((TextView)imageView.findViewById(R.id.text)).setText("图片弹幕"+num);
        num += 1;
        imageView.setTag(Barrage.IMAGE);
        return imageView;
    }
}
