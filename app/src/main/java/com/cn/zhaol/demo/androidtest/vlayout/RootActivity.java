package com.cn.zhaol.demo.androidtest.vlayout;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cn.zhaol.demo.androidtest.vlayout.mydemo.MydemoV2Activity;
import com.cn.zhaol.demo.androidtest.vlayout.mydemo.MydemoVlayoutActivity;


/**
 * vlayout demo
 * Created by zhaolei on 2018/9/5.
 */
public class RootActivity extends ListActivity {

    private String[] mTitles = new String[]{
            VLayoutActivity.class.getSimpleName(),
            MainVlayoutActivity.class.getSimpleName(),
            TestActivity.class.getSimpleName(),
            OnePlusNLayoutActivity.class.getSimpleName(),
            DebugActivity.class.getSimpleName(),
            MydemoVlayoutActivity.class.getSimpleName(),
            MydemoV2Activity.class.getSimpleName()
    };

    private Class[] mActivities = new Class[]{
            VLayoutActivity.class,
            MainVlayoutActivity.class,
            TestActivity.class,
            OnePlusNLayoutActivity.class,
            DebugActivity.class,
            MydemoVlayoutActivity.class,
            MydemoV2Activity.class
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
