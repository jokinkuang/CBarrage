package com.jokin.cbarrage;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jokin.cbarrage.cbarrage.CBarrageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private CBarrageView mBarrageView;
    private Button mAddByTimerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mBarrageView = (CBarrageView) findViewById(R.id.barrageView);
        mAddByTimerBtn = (Button) findViewById(R.id.addByTimerBtn);
        mAddByTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBarrageByTimer();
            }
        });

        findViewById(R.id.addBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBarrage();
            }
        });
        findViewById(R.id.addImageBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImageBarrage();
            }
        });

        mBarrageView.setListener(new CBarrageView.CBarrageViewListener() {
            @Override
            public void onPrepared(CBarrageView view) {
                view.setItemGap(10);
                view.setRowNum(3);
                view.setRowHeight(100);
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

    private void addImageBarrage() {
        View view = this.getLayoutInflater().inflate(R.layout.barrage_image, mBarrageView, false);
        mBarrageView.addBarrage(view);
    }

    private void addBarrageByTimer() {
            Boolean b = (Boolean) mAddByTimerBtn.getTag();
            timer.cancel();
            if (b == null || !b) {
                mAddByTimerBtn.setText("stop timer");
                timer = new Timer();
                timer.schedule(new AsyncAddTask(), 0, 100);
                mAddByTimerBtn.setTag(true);
            } else {
                mAddByTimerBtn.setText("add by timer");
                mAddByTimerBtn.setTag(false);
            }
    }

    Timer timer = new Timer();
    class AsyncAddTask extends TimerTask {
        @Override
        public void run() {
            for (int i = 0; i < 200; ++i) {

            SystemClock.sleep(100);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addImageBarrage();

                    // mBarrageView.setItemGap(new Random().nextInt() % 100 + 50);
                }
            });
            }
        }
    };
}
