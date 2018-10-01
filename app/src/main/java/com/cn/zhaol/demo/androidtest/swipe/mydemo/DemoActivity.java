
package com.cn.zhaol.demo.androidtest.swipe.mydemo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;


import com.cn.zhaol.demo.androidtest.R;
import com.cn.zhaol.demo.androidtest.swipe.PreferenceUtils;
import com.cn.zhaol.demo.androidtest.swipe.SwipeBackActivity;
import com.cn.zhaol.demo.androidtest.swipe.SwipeBackLayout;


/**
 * 使用第三方侧滑框架
 * Created by Issac on 8/11/13.
 */
public class DemoActivity extends SwipeBackActivity implements View.OnClickListener {
    private static final int VIBRATE_DURATION = 20;

    //private int[] mBgColors;

    //private static int mBgIndex = 0;

    private String mKeyTrackingMode;

    private RadioGroup mTrackingModeGroup;

    private SwipeBackLayout mSwipeBackLayout;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        findViews();
        //changeActionBarColor();
        mKeyTrackingMode = getString(R.string.key_tracking_mode);
        mSwipeBackLayout = getSwipeBackLayout();

        mTrackingModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int edgeFlag;
                switch (checkedId) {
                    case R.id.mode_left:
                        edgeFlag = SwipeBackLayout.EDGE_LEFT;
                        break;
                    case R.id.mode_right:
                        edgeFlag = SwipeBackLayout.EDGE_RIGHT;
                        break;
                    case R.id.mode_bottom:
                        edgeFlag = SwipeBackLayout.EDGE_BOTTOM;
                        break;
                    default:
                        edgeFlag = SwipeBackLayout.EDGE_ALL;
                }
                //设置滑动方向
                mSwipeBackLayout.setEdgeTrackingEnabled(edgeFlag);
                //保存状态
                saveTrackingMode(edgeFlag);
            }
        });
        mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
                //对应滑动状态改变(一直在变)
                //state 1 用户正在拖动
                //state 2 动画在自动执行
                //state 0 视图当前没有被拖动，或者动画效果结束
                Log.e("DemoActivity","onScrollStateChange state = " + state +",scrollPercent = " + scrollPercent);
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                //触摸边际
                //1 left
                //2 right
                //8 bottom
                //vibrate(VIBRATE_DURATION);
                Log.e("DemoActivity","onEdgeTouch edgeFlag = " +edgeFlag);
            }

            @Override
            public void onScrollOverThreshold() {
                //第一次滑动的比例超出了设置的阀值的时候调用
                //vibrate(VIBRATE_DURATION);
                Log.e("DemoActivity","onScrollOverThreshold!!!!");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreTrackingMode();
    }

    private void saveTrackingMode(int flag) {
        PreferenceUtils.setPrefInt(getApplicationContext(), mKeyTrackingMode, flag);
    }

    private void restoreTrackingMode() {
        int flag = PreferenceUtils.getPrefInt(getApplicationContext(), mKeyTrackingMode,
                SwipeBackLayout.EDGE_LEFT);
        mSwipeBackLayout.setEdgeTrackingEnabled(flag);
        switch (flag) {
            case SwipeBackLayout.EDGE_LEFT:
                mTrackingModeGroup.check(R.id.mode_left);
                break;
            case SwipeBackLayout.EDGE_RIGHT:
                mTrackingModeGroup.check(R.id.mode_right);
                break;
            case SwipeBackLayout.EDGE_BOTTOM:
                mTrackingModeGroup.check(R.id.mode_bottom);
                break;
            case SwipeBackLayout.EDGE_ALL:
                mTrackingModeGroup.check(R.id.mode_all);
                break;
        }
    }

//    private void changeActionBarColor() {
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColors()[mBgIndex]));
//        mBgIndex++;
//        if (mBgIndex >= getColors().length) {
//            mBgIndex = 0;
//        }
//    }

    private void findViews() {
        //setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_finish).setOnClickListener(this);
        mTrackingModeGroup = (RadioGroup) findViewById(R.id.tracking_mode);
    }

//    private int[] getColors() {
//        if (mBgColors == null) {
//            Resources resource = getResources();
//            mBgColors = new int[] {
//                    resource.getColor(R.color.androidColorA),
//                    resource.getColor(R.color.androidColorB),
//                    resource.getColor(R.color.androidColorC),
//                    resource.getColor(R.color.androidColorD),
//                    resource.getColor(R.color.androidColorE),
//            };
//        }
//        return mBgColors;
//    }

    private void vibrate(long duration) {
        //手机震动
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {
                0, duration
        };
        vibrator.vibrate(pattern, -1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                startActivity(new Intent(DemoActivity.this, DemoActivity.class));
                break;
            case R.id.btn_finish:
                //直接滑动退出页面
                //在SwipeBackActivity调用去滑动删除当前activity
                scrollToFinishActivity();
                break;
        }
    }





}
