package com.jokin.cbarrage;

import android.graphics.Color;
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

    /**
     * Barrage Templates
     **/
    public static class BarrageType {
        public static final String TEXT = "text";
        public static final String IMAGE = "image";
        public static final String IMAGE_TEXT = "image_text";
    }

    @Override
    public View createView(ViewGroup root, View converView, Barrage barrage) {
        if (BarrageType.TEXT.equals(barrage.getType())) {
            return createTextBarrage(root, converView, barrage);
        } else if (BarrageType.IMAGE_TEXT.equals(barrage.getType())) {
            return createImageTextBarrage(root, converView, barrage);
        }
        return null;
    }

    @Override
    public void destroyView(ViewGroup root, Barrage obj, View view) {
        Log.d(TAG, "destroyView "+view);
    }

    @Override
    public boolean isViewFromObject(View view, Barrage obj) {
        return view.getTag().equals(obj.getType());
    }

    private View createTextBarrage(final ViewGroup root, View converView, Barrage barrage) {
        if (converView != null) {
            ((TextView)converView).setText("复用View："+barrage.getText());
            converView.setX(0);
            Log.d(TAG, String.format("x %f y %f l %d t %d r %d",
                    converView.getX(), converView.getY(), converView.getLeft(), converView.getTop(), converView.getRight()));
            return converView;
        }
        TextView textView = new TextView(root.getContext());
        textView.setText("新建View："+barrage.getText());
        textView.setTextColor(Color.BLACK);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(root.getContext(), "clicked", Toast.LENGTH_SHORT).show();
            }
        });
        textView.setTag(BarrageType.TEXT);
        return textView;
    }

    private View createImageTextBarrage(ViewGroup root, View converView, Barrage barrage) {
        if (converView != null) {
            ((TextView)converView.findViewById(R.id.text)).setText("重用View："+barrage.getText());
            return converView;
        }
        View imageView = LayoutInflater.from(root.getContext()).inflate(R.layout.barrage_image, root, false);
        ((TextView)imageView.findViewById(R.id.text)).setText("新建View："+barrage.getText());
        imageView.setTag(BarrageType.IMAGE_TEXT);
        return imageView;
    }
}
