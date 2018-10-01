package com.cn.zhaol.demo.androidtest.swipe.mydemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

import com.cn.zhaol.demo.androidtest.R;

/**
 * 第二个例子的改造
 * 改造的目的在于 添加滑动位置(最左侧的范围可以滑动)
 */
public class Second2Activity extends AppCompatActivity {

    private View decorView;
    private float downX;
    private float screenWidth;
    private float maxLeft = 200;//从左边0~200的位置可以滑动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second2);

        // 获得decorView
        decorView = getWindow().getDecorView();

        // 获得手机屏幕的宽度和高度，单位像素
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        //screenHeight = metrics.heightPixels;
    }

    /**
     * 通过重写该方法，对触摸事件进行处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 获得按下时的X坐标
                downX = event.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                // 当手指滑动时
                if(downX < maxLeft) {
                    //0~200有效区
                    float moveDistanceX = event.getX() - downX;

                    if(moveDistanceX > 0){// 如果是向右滑动
                        decorView.setX(moveDistanceX); // 设置界面的X到滑动到的位置
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                // 当抬起手指时
                if(downX < maxLeft){
                    //0~200有效区
                    float moveDistanceX = event.getX() - downX;

                    if(moveDistanceX > screenWidth / 2){
                        // 如果滑动的距离超过了手机屏幕的一半, 滑动处屏幕后再结束当前Activity
                        continueMove(moveDistanceX);
                    }else{
                        // 如果滑动距离没有超过一半, 往回滑动
                        rebackToLeft(moveDistanceX);
                    }
                }
                break;
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

        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 位移
                float x = (float) (animation.getAnimatedValue());
                decorView.setX(x);
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
    }


}
