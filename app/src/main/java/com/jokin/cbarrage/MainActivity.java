package com.jokin.cbarrage;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;

import com.jokin.cbarrage.cbarrage.CBarrageView;

import java.util.Timer;
import java.util.TimerTask;

import static com.jokin.cbarrage.BarrageDataAdapter.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Context mContext;
    private FrameLayout mBBFrame;
    private Button mAddByTimerBtn;

    private BarrageDataAdapter mBarrageAdapter;
    private CBarrageView mBarrageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mBarrageView = (CBarrageView) findViewById(R.id.barrageView);
        mBBFrame = (FrameLayout) findViewById(R.id.bbFrame);
        mAddByTimerBtn = (Button) findViewById(R.id.addByTimerBtn);
        mAddByTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBarrageByTimer();
            }
        });

        findViewById(R.id.dumpBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBarrageView.dumpMemory();
            }
        });
        findViewById(R.id.pauseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBarrageView.pause();
            }
        });
        findViewById(R.id.resumeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBarrageView.resume();
            }
        });
        findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBarrageView.clear();
            }
        });
        findViewById(R.id.addBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTextBarrage();
            }
        });
        findViewById(R.id.addPriorityBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPriorityBarrage();
            }
        });
        findViewById(R.id.addImageBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImageTextBarrage();
            }
        });
        findViewById(R.id.addBBBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBBBarrage();
            }
        });


        mBarrageAdapter = new BarrageDataAdapter();
        mBarrageView.setAdapter(mBarrageAdapter);
        mBarrageView.setListener(new CBarrageView.CBarrageViewListener() {
            @Override
            public void onPrepared(CBarrageView view) {
                view.setItemGap(10);
                view.setRowNum(3);
                view.setItemGravity(Gravity.BOTTOM);
                view.setRowHeight(35);  // Row is 25dp
                view.setRowSpeed(8000);
                view.setMode(CBarrageView.NORMAL);

                view.start();
            }

            @Override
            public void onIdle(long idleTimeMs, CBarrageView view) {

            }
        });
    }

    private int num = 0;

    private void addTextBarrage() {
        // TextView textView = new TextView(this);
        // num += 1;
        // textView.setText("这是一条弹幕"+num);
        // textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // textView.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
        //     }
        // });
        // mBarrageView.addTextBarrage(textView);

        Barrage barrage = new Barrage(BarrageType.TEXT);
        barrage.setText("这是一条普通弹幕"+(num++));
        mBarrageAdapter.addBarrage(barrage);
    }

    private void addPriorityBarrage() {
        // TextView textView = new TextView(this);
        // num += 1;
        // textView.setText("这是一条优先弹幕"+num);
        // textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // textView.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
        //     }
        // });
        Barrage barrage = new Barrage(BarrageType.TEXT);
        barrage.setText("## 高级弹幕"+(num++));
        mBarrageAdapter.addPriorityBarrage(barrage);
    }

    private void addImageTextBarrage() {
        // View view = this.getLayoutInflater().inflate(R.layout.barrage_image, mBarrageView, false);
        // ((TextView)view.findViewById(R.id.text)).setText("图片弹幕"+num);
        // num += 1;
        // mBarrageView.addTextBarrage(view);
        Barrage barrage = new Barrage(BarrageType.IMAGE_TEXT);
        barrage.setText("图文");
        mBarrageAdapter.addBarrage(barrage);
    }

    private void addBBBarrage() {
        mBBFrame.setVisibility(View.VISIBLE);

        CountDownTimer timer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                // note memory leak
                // View view = MainActivity.this.getLayoutInflater().inflate(R.layout.barrage_image, mBarrageView, false);
                // view.setBackgroundColor(Color.YELLOW);
                // ((TextView)view.findViewById(R.id.text)).setText("霸屏弹幕"+num);
                // num += 1;
                // CBarrageRow row = mBarrageView.addRowBarrage(view);
                Barrage barrage = new Barrage(BarrageType.IMAGE_TEXT);
                barrage.setText("霸屏");
                mBBFrame.setVisibility(View.GONE);
                mBarrageAdapter.addBarrage(barrage);

                // showBBAnimation(row.getLeft(), row.getTop());
                // startAnimation(row.getRight()/2, row.getTop());
            }
        };
        timer.start();
    }

    private void addBarrageByTimer() {
            Boolean b = (Boolean) mAddByTimerBtn.getTag();
            timer.cancel();
            if (b == null || !b) {
                mAddByTimerBtn.setText("暂停定时器");
                timer = new Timer();
                timer.schedule(new AsyncAddTask(), 0, 200);
                mAddByTimerBtn.setTag(true);
                isStop = false;
            } else {
                mAddByTimerBtn.setText("启动定时器");
                mAddByTimerBtn.setTag(false);
                isStop = true;
            }
    }

    private volatile boolean isStop = false;
    Timer timer = new Timer();
    class AsyncAddTask extends TimerTask {
        @Override
        public void run() {
            for (int i = 0; i < 200; ++i) {
                if (isStop) {
                    return;
                }
                SystemClock.sleep(100);
                final int finalI = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalI % 2 == 0) {
                            addTextBarrage();
                        } else {
                            addImageTextBarrage();
                        }
                    }
                });
            }
        }
    };


    private void showBBAnimation(int left, int top) {
        Log.d("main", ""+top);
        //创建动画，参数表示他的子动画是否共用一个插值器
        AnimationSet animationSet = new AnimationSet(true);
        //添加动画
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
        // animationSet.addAnimation(new TranslateAnimation(
        //         Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1,
        //         Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, top));
        animationSet.addAnimation(new TranslateAnimation(0, 100, 0, 100));
        animationSet.addAnimation(new ScaleAnimation(1, 0.1f, 1, 0.1f));
        //设置插值器
        animationSet.setInterpolator(new AccelerateInterpolator());
        //设置动画持续时长
        animationSet.setDuration(1000);
        //设置动画结束之后是否保持动画的目标状态
        animationSet.setFillAfter(false);
        //设置动画结束之后是否保持动画开始时的状态
        animationSet.setFillBefore(true);
        //设置重复模式
        animationSet.setRepeatMode(AnimationSet.REVERSE);
        //设置重复次数
        animationSet.setRepeatCount(AnimationSet.INFINITE);
        //取消动画
        animationSet.cancel();
        //释放资源
        animationSet.reset();
        //开始动画
        mBBFrame.startAnimation(animationSet);
    }

    private void startAnimation(int x, int y) {
        Log.d("main", ""+x+"|"+y);

        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mBBFrame, View.ALPHA, 1f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mBBFrame, View.SCALE_X, 1f, 0.3f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mBBFrame, View.SCALE_Y, 1f, 0.3f);
        ObjectAnimator moveX = ObjectAnimator.ofFloat(mBBFrame, View.TRANSLATION_X, 0f, 0f);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(mBBFrame, View.TRANSLATION_Y, 0f, 0f);

        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.play(alpha).with(scaleX).with(scaleY).with(moveX).with(moveY);//两个动画同时开始
        animatorSet.start();
    }

}
