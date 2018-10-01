package com.cn.zhaol.demo.androidtest.vlayout.mydemo;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.DefaultLayoutHelper;
import com.alibaba.android.vlayout.layout.FixLayoutHelper;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.alibaba.android.vlayout.layout.SingleLayoutHelper;
import com.alibaba.android.vlayout.layout.StaggeredGridLayoutHelper;
import com.cn.zhaol.demo.androidtest.R;

import java.util.LinkedList;
import java.util.List;

/**
 * 加载数据时有两种方式
 * 第二个demo VirtualLayoutAdapter
 * Created by zhaolei on 2018/9/6.
 */
public class MydemoV2Activity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydemo_vlayout);

        //Step 1
        recyclerView = (RecyclerView) findViewById(R.id.my_demo_recyView);
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(this);
        recyclerView.setLayoutManager(virtualLayoutManager);
        setItemDecoration();

        //Step 2(其实不设置有默认值)
        //设置回收复用池大小
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 4);//默认type=0
        viewPool.setMaxRecycledViews(1, 10);
        viewPool.setMaxRecycledViews(4, 5);

        //Step 3(添加布局文件)加载数据时有两种方式(展示其中一种)
        //VirtualLayoutAdapter (自定义总适配器)
        //构造 layoutHelper 列表
        List<LayoutHelper> helpers = new LinkedList<>();

        //添加所以布局文件
        //Grid布局
        GridLayoutHelper gridLayoutHelper = new GridLayoutHelper(4);//网格布局(spanCount表示网格的列数)
        gridLayoutHelper.setItemCount(4);//设置Item个数(只在VirtualLayoutAdapter模式中使用)
        gridLayoutHelper.setAutoExpand(false);//是否自动扩展
        helpers.add(gridLayoutHelper);

        //线性布局
        helpers.add(DefaultLayoutHelper.newHelper(8));//最简单的线性布局(4+10)
        LinearLayoutHelper linearLayoutHelper = new LinearLayoutHelper();
        linearLayoutHelper.setDividerHeight(1);
        linearLayoutHelper.setItemCount(2);
        helpers.add(linearLayoutHelper);

        //固定布局
        FixLayoutHelper fixLayoutHelper = new FixLayoutHelper(FixLayoutHelper.TOP_LEFT,20,20);//(15)
        helpers.add(fixLayoutHelper);

        //通栏布局，一行
        SingleLayoutHelper slayoutHelper = new SingleLayoutHelper();//(16)
        //slayoutHelper.setBgColor(Color.BLUE);
        slayoutHelper.setMargin(0, 30, 0, 20);//距离边框的长度
        helpers.add(slayoutHelper);


        //设置瀑布流布局
        StaggeredGridLayoutHelper staggeredGridLayoutHelper = new StaggeredGridLayoutHelper();//(>16)
        staggeredGridLayoutHelper.setLane(3);//num为每行显示数目
        staggeredGridLayoutHelper.setGap(5);//gap为两个item的边距
        staggeredGridLayoutHelper.setItemCount(10);
        helpers.add(staggeredGridLayoutHelper);

        //Step 4 放入布局文件
        virtualLayoutManager.setLayoutHelpers(helpers);

        //Step 5 将 adapter 设置给 recyclerView
        recyclerView.setAdapter(new AllVLayoutAdapter(virtualLayoutManager,this));


    }

    private void setItemDecoration() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(5, 5, 5, 5);
            }
        });
    }
}
