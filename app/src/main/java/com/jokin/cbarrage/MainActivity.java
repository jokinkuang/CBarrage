package com.jokin.cbarrage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jokin.cbarrage.cbarrage.CBarrageView;

public class MainActivity extends AppCompatActivity {

    private CBarrageView mBarrageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mBarrageView = (CBarrageView) findViewById(R.id.barrageView);
        findViewById(R.id.addBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBarrage();
            }
        });

        mBarrageView.setListener(new CBarrageView.CBarrageViewListener() {
            @Override
            public void onPrepared(CBarrageView view) {
                view.setItemGap(300);
                view.setRowNum(1);
                view.setRowHeight(400);
                view.setRowSpeed(5000);

                view.start();
            }
        });
    }

    private int num = 0;

    private void addBarrage() {
        TextView textView = new TextView(this);
        num += 1;
        textView.setText("这是一条弹幕"+num);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mBarrageView.addBarrage(textView);
    }
}
