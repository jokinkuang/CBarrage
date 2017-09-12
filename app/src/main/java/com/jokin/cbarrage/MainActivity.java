package com.jokin.cbarrage;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jokin.cbarrage.cbarrage.CBarrageRow;
import com.jokin.cbarrage.cbarrage.CBarrageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private CBarrageView mBarrageView;
    private FrameLayout mBBFrame;
    private Button mAddByTimerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                addBarrage();
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
                addImageBarrage();
            }
        });
        findViewById(R.id.addBBBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBBBarrage();
            }
        });

        mBarrageView.setListener(new CBarrageView.CBarrageViewListener() {
            @Override
            public void onPrepared(CBarrageView view) {
                view.setItemGap(10);
                view.setRowNum(3);
                view.setItemGravity(Gravity.BOTTOM);
                view.setRowHeight(100);
                view.setRowSpeed(5000);
                view.setMode(CBarrageView.AVERAGE);

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

    private void addPriorityBarrage() {
        TextView textView = new TextView(this);
        num += 1;
        textView.setText("这是一条优先弹幕"+num);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mBarrageView.addPriorityBarrage(textView);
    }

    private void addImageBarrage() {
        View view = this.getLayoutInflater().inflate(R.layout.barrage_image, mBarrageView, false);
        ((TextView)view.findViewById(R.id.text)).setText("图片弹幕"+num);
        num += 1;
        mBarrageView.addBarrage(view);
    }

    private void addBBBarrage() {
        mBBFrame.setVisibility(View.VISIBLE);

        CountDownTimer timer = new CountDownTimer(100, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                // note memory leak
                View view = MainActivity.this.getLayoutInflater().inflate(R.layout.barrage_image, mBarrageView, false);
                view.setBackgroundColor(Color.YELLOW);
                ((TextView)view.findViewById(R.id.text)).setText("霸屏弹幕"+num);
                num += 1;
                CBarrageRow row = mBarrageView.addRowBarrage(view);

                // showBBAnimation(row.getLeft(), row.getTop());
                startAnimation(row.getRight()/2, row.getTop());
            }
        };
        timer.start();
    }

    private void addBarrageByTimer() {
            Boolean b = (Boolean) mAddByTimerBtn.getTag();
            timer.cancel();
            if (b == null || !b) {
                mAddByTimerBtn.setText("stop timer");
                timer = new Timer();
                timer.schedule(new AsyncAddTask(), 0, 200);
                mAddByTimerBtn.setTag(true);
                isStop = false;
            } else {
                mAddByTimerBtn.setText("add by timer");
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
