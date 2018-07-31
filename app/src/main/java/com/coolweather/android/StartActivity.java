package com.coolweather.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv_start;
    private TextView tiShi;
    private Animation animation = null;
    private Timer timer = new Timer();
    private String mystr="跳 过 ";
    private CustomTextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.start_layout);
        iv_start = (ImageView) findViewById(R.id.iv_start);
        mTextView=(CustomTextView)findViewById(R.id.myTextView);
        mTextView.setOnClickListener(this);
        tiShi =(TextView)findViewById(R.id.tishi);
        tiShi.setOnClickListener(this);
        timer.schedule(mTimerTask, 900,950);
        //initImage();
        initanim();

    }
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tishi:
                //animation.cancel();
                //iv_start.clearAnimation();
                startActivity();
                break;
            case R.id.myTextView:
                //animation.cancel();
                //iv_start.clearAnimation();
                startActivity();
                break;
        }
    }
    private void initImage() {

        iv_start.setImageResource(R.drawable.start);
        //进行缩放动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.4f, 1.0f, 1.4f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(4000);
        //动画播放完成后保持形状
        scaleAnimation.setFillAfter(true);

        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //可以在这里先进行某些操作
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv_start.startAnimation(scaleAnimation);
    }

    private void initanim() {
        animation = AnimationUtils.loadAnimation(this,
                R.anim.anim_set);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //可以在这里先进行某些操作
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        iv_start.startAnimation(animation);
    }

    private  NewTask mTimerTask=new NewTask();
    private  class NewTask extends TimerTask {
        private int count=4;
        @Override
        public void run(){
            count--;
          final String str=mystr+count;
            runOnUiThread(new Runnable() {
                public void run() {
                    tiShi.setText(str);
                    mTextView.setmText(str);
                }
            });

            if(count==0){
                if (mTimerTask != null){
                    mTimerTask.cancel();  //将原任务从队列中移除
                }
                timer.cancel();
            }
            Log.e("start",count+""+str);
            //execute the task

        }

    }



    private void startActivity() {

        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onDestroy(){

            timer.cancel();
        super.onDestroy();

    }



}
