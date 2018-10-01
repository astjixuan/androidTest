package com.cn.zhaol.demo.androidtest.swipe.mydemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.cn.zhaol.demo.androidtest.R;

/**
 * 侧滑回退页面（例子2）
 * SecondActivity的背景一定要透明的
 * 但是与ListView，或者ScrollView等可以滑动的视图冲突
 */
public class SecondActivity extends AppCompatActivity {

    /**
     * 整个Activity视图的根视图
     */
    private View decorView;

    //private RelativeLayout mainLayout;
    /**
     * 手指按下时的坐标
     */
    private float downX, downY;

    /**
     * 手机屏幕的宽度和高度
     */
    private float screenWidth, screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        // 获得decorView
        decorView = getWindow().getDecorView();
        //mainLayout = (RelativeLayout) findViewById(R.id.second_main_layout);

        // 获得手机屏幕的宽度和高度，单位像素
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

    }

    /**
     * 通过重写该方法，对触摸事件进行处理
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){// 当按下时
            // 获得按下时的X坐标
            downX = event.getX();

        }else if(event.getAction() == MotionEvent.ACTION_MOVE){// 当手指滑动时
            // 获得滑过的距离
            float moveDistanceX = event.getX() - downX;
            if(moveDistanceX > 0){// 如果是向右滑动
                decorView.setX(moveDistanceX); // 设置界面的X到滑动到的位置
                //mainLayout.setX(moveDistanceX);
            }

        }else if(event.getAction() == MotionEvent.ACTION_UP){// 当抬起手指时
            // 获得滑过的距离
            float moveDistanceX = event.getX() - downX;
            if(moveDistanceX > screenWidth / 2){
                // 如果滑动的距离超过了手机屏幕的一半, 结束当前Activity
                //finish();//最原始的

                // 如果滑动的距离超过了手机屏幕的一半, 滑动处屏幕后再结束当前Activity
                continueMove(moveDistanceX);
            }else{ // 如果滑动距离没有超过一半
                // 恢复初始状态
                //decorView.setX(0);//最原始的

                // 如果滑动距离没有超过一半, 往回滑动
                rebackToLeft(moveDistanceX);
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 从当前位置一直往右滑动到消失。
     * 这里使用了属性动画。
     */
    private void continueMove(float moveDistanceX){
        // 从当前位置移动到右侧。
        ValueAnimator anim = ValueAnimator.ofFloat(moveDistanceX, screenWidth);
        anim.setDuration(1000); // 一秒的时间结束, 为了简单这里固定为1秒
        anim.start();

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 位移
                float x = (float) (animation.getAnimatedValue());
                decorView.setX(x);
                //mainLayout.setX(x);
            }
        });

        // 动画结束时结束当前Activity
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
            }

        });
    }

    /**
     * Activity被滑动到中途时，滑回去~
     */
    private void rebackToLeft(float moveDistanceX){
        ObjectAnimator.ofFloat(decorView, "X", moveDistanceX, 0).setDuration(300).start();
        //ObjectAnimator.ofFloat(mainLayout, "X", moveDistanceX, 0).setDuration(300).start();
    }
}
