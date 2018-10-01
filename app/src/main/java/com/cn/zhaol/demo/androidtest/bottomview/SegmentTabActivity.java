package com.cn.zhaol.demo.androidtest.bottomview;

import android.os.Bundle;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cn.zhaol.demo.androidtest.R;
import com.cn.zhaol.demo.androidtest.bottomview.utils.ViewFindUtils;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.flyco.tablayout.widget.MsgView;

import java.util.ArrayList;

/**
 * SegmentTabLayout的使用方法
 * 圆角的tabLayout(里面只能放文字)
 * 分段显示,显示部分只能点击，不能滑动
 */
public class SegmentTabActivity extends AppCompatActivity {

    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private ArrayList<Fragment> mFragments2 = new ArrayList<>();

    private String[] mTitles = {"首页", "消息"};
    private String[] mTitles_2 = {"首页", "消息", "联系人"};
    private String[] mTitles_3 = {"首页", "消息", "联系人", "更多"};
    private View mDecorView;
    private SegmentTabLayout mTabLayout_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segment_tab);


        for (String title : mTitles_3) {
            mFragments.add(SimpleCardFragment.getInstance("Switch ViewPager " + title));
        }

        for (String title : mTitles_2) {
            mFragments2.add(SimpleCardFragment.getInstance("Switch Fragment " + title));
        }

        mDecorView = getWindow().getDecorView();

        //findviewById方法
        SegmentTabLayout tabLayout_1 = ViewFindUtils.find(mDecorView, R.id.tl_1);
        SegmentTabLayout tabLayout_2 = ViewFindUtils.find(mDecorView, R.id.tl_2);
        mTabLayout_3 = ViewFindUtils.find(mDecorView, R.id.tl_3);
        SegmentTabLayout tabLayout_4 = ViewFindUtils.find(mDecorView, R.id.tl_4);
        SegmentTabLayout tabLayout_5 = ViewFindUtils.find(mDecorView, R.id.tl_5);

        tabLayout_1.setTabData(mTitles);//设置数据(文字)
        tabLayout_2.setTabData(mTitles_2);
        tl_3();//跟ViewPager结合
        tabLayout_4.setTabData(mTitles_2, this, R.id.fl_change, mFragments2);//关联数据支持同时切换fragments
        tabLayout_5.setTabData(mTitles_3);

        //显示未读红点(默认就是红点)
        tabLayout_1.showDot(2);
        tabLayout_2.showDot(2);
        mTabLayout_3.showDot(0);
        tabLayout_4.showDot(1);

        //设置未读消息红点
        mTabLayout_3.showDot(2);//
        MsgView rtv_3_2 = mTabLayout_3.getMsgView(2);
        if (rtv_3_2 != null) {
            rtv_3_2.setBackgroundColor(Color.parseColor("#77CEEE"));//设置红点为蓝色
        }
    }

    /**
     * 简单的绑定ViewPager
     */
    private void tl_3() {
        final ViewPager vp_3 = ViewFindUtils.find(mDecorView, R.id.vp_2);
        vp_3.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        mTabLayout_3.setTabData(mTitles_3);
        mTabLayout_3.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                vp_3.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        vp_3.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout_3.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        vp_3.setCurrentItem(0);//默认选第几个
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles_3[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }
}
