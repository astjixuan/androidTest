package com.cn.zhaol.demo.androidtest.bottomview;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 测试滑动页面
 * flyco.tablayout例子
 */
public class Main2Activity extends ListActivity {

    private String[] mTitles = new String[]{
            CommonTabActivity.class.getSimpleName(),
            SegmentTabActivity.class.getSimpleName(),
            SlidingTabActivity.class.getSimpleName()
    };

    private Class[] mActivities = new Class[]{
            CommonTabActivity.class,
            SegmentTabActivity.class,
            SlidingTabActivity.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTitles));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        startActivity(new Intent(this, mActivities[position]));
    }
}
