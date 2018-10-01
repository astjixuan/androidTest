package com.cn.zhaol.demo.androidtest.vlayout.mydemo;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.ColumnLayoutHelper;
import com.alibaba.android.vlayout.layout.FixLayoutHelper;
import com.alibaba.android.vlayout.layout.FloatLayoutHelper;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.alibaba.android.vlayout.layout.OnePlusNLayoutHelper;
import com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper;
import com.alibaba.android.vlayout.layout.SingleLayoutHelper;
import com.alibaba.android.vlayout.layout.StaggeredGridLayoutHelper;
import com.alibaba.android.vlayout.layout.StickyLayoutHelper;
import com.cn.zhaol.demo.androidtest.R;

import java.util.LinkedList;
import java.util.List;

/**
 * 加载数据时有两种方式
 * 第一个demo，使用DelegateAdapter
 */
public class MydemoVlayoutActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private int viewType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydemo_vlayout);

        //Step 1
        recyclerView = (RecyclerView) findViewById(R.id.my_demo_recyView);
        //绑定VirtualLayoutManager
        VirtualLayoutManager layoutManager = new VirtualLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //设置Item之间的间隔
        setItemDecoration();

        //Step 2(其实不设置有默认值)
        //设置回收复用池大小
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(viewType, 10);//默认type=0

        //Step 3(添加布局文件)加载数据时有两种方式(先展示其中一种)
        //DelegateAdapter的使用 (适配器集合)
        List<DelegateAdapter.Adapter> adapters = new LinkedList<>();//放入所以布局文件

        //TODO 注意 设置浮动布局(有bug，必须放第一个布局，不然拉动有问题)
        FloatLayoutHelper flayoutHelper = new FloatLayoutHelper();
        flayoutHelper.setAlignType(FixLayoutHelper.BOTTOM_RIGHT);
        flayoutHelper.setDefaultLocation(100, 400);
        VirtualLayoutManager.LayoutParams layoutParams = new VirtualLayoutManager.LayoutParams(150, 150);
        adapters.add(new TopAdapter(this, flayoutHelper, 1, layoutParams));

        //设置Grid布局
        GridLayoutHelper gridLayoutHelper = new GridLayoutHelper(4);
        gridLayoutHelper.setAutoExpand(false);//是否自动扩展
        gridLayoutHelper.setWeights(new float[]{25f, 15f, 35f, 25f});//每个格子的比例
        gridLayoutHelper.setHGap(10);//格子间距
        gridLayoutHelper.setMarginBottom(5);//设置布局底部与下个布局的间隔
//        adapters.add(new TopAdapter(this,gridLayoutHelper,4,
//                new VirtualLayoutManager.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT)));
        adapters.add(new TopAdapter(this,gridLayoutHelper,4,null));


        //设置线性布局
        LinearLayoutHelper linearLayoutHelper = new LinearLayoutHelper();
        //设置Item个数
        //linearLayoutHelper.setItemCount(1);
        //设置间隔高度
        linearLayoutHelper.setDividerHeight(1);
        //设置布局底部与下个布局的间隔
        linearLayoutHelper.setMarginBottom(100);
        adapters.add(new CenterAdapter(this,linearLayoutHelper,10));


        //设置固定布局
        FixLayoutHelper fixLayoutHelper = new FixLayoutHelper(FixLayoutHelper.TOP_LEFT,0,0);
        adapters.add(new CenterAdapter(this,fixLayoutHelper,1,new VirtualLayoutManager.LayoutParams(100,100)));
        FixLayoutHelper fixLayoutHelper2 = new FixLayoutHelper(FixLayoutHelper.TOP_RIGHT,20,20);
        adapters.add(new CenterAdapter(this,fixLayoutHelper2,1,new VirtualLayoutManager.LayoutParams(50,50)));

        //TODO 注意 设置滚动固定布局(其中有bug，如果是最后的or开始的布局文件，无法显示)
        ScrollFixLayoutHelper scrollFixLayoutHelper = new ScrollFixLayoutHelper(ScrollFixLayoutHelper.TOP_RIGHT,70,70);
        scrollFixLayoutHelper.setShowType(ScrollFixLayoutHelper.SHOW_ON_ENTER);
        adapters.add(new TopAdapter(this,scrollFixLayoutHelper,1,null));


        //设置浮动布局(有bug，必须放第一个布局，不然拉动有问题)
//        FloatLayoutHelper flayoutHelper = new FloatLayoutHelper();
//        flayoutHelper.setAlignType(FixLayoutHelper.BOTTOM_RIGHT);
//        flayoutHelper.setDefaultLocation(100, 400);
//        VirtualLayoutManager.LayoutParams layoutParams = new VirtualLayoutManager.LayoutParams(150, 150);
//        adapters.add(new TopAdapter(this, flayoutHelper, 1, layoutParams));

        //网格测试
        GridLayoutHelper gridLayoutHelper2 = new GridLayoutHelper(3);//每行3个
        gridLayoutHelper2.setAutoExpand(true);//是否自动扩展
        gridLayoutHelper2.setHGap(50);//格子间距
        //gridLayoutHelper2.setItemCount(10);//设置Item个数(这个设置无用，在适配器中设置，这个设置必须在VirtualLayoutAdapter设置里才有用)
        adapters.add(new CenterAdapter(this,gridLayoutHelper2,10));

        //设置栏格布局(只有一行)
        ColumnLayoutHelper columnLayoutHelper = new ColumnLayoutHelper();
        //columnLayoutHelper.setItemCount(5);
        columnLayoutHelper.setWeights(new float[]{30,10,30,20,10});
        adapters.add(new CenterAdapter(this,columnLayoutHelper,5,
                new VirtualLayoutManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        100)));

        //设置通栏布局
        SingleLayoutHelper slayoutHelper = new SingleLayoutHelper();
        //slayoutHelper.setBgColor(Color.BLUE);
        slayoutHelper.setMargin(0, 30, 0, 20);//距离边框的长度
        adapters.add(new CenterAdapter(this,slayoutHelper,1));

        //设置一拖N布局
        OnePlusNLayoutHelper oneHelper = new OnePlusNLayoutHelper();
        oneHelper.setBgColor(0xff876384);
        //oneHelper.setAspectRatio(4.0f);
        oneHelper.setColWeights(new float[]{40f, 45f});
        oneHelper.setMargin(10, 20, 10, 20);
        oneHelper.setPadding(10, 10, 10, 10);
        adapters.add(new CenterAdapter(this, oneHelper, 5));//1 + 4(最大5个，1拖4)


        //设置Sticky布局(吸顶或者吸顶布局，有bug，不能跟瀑布流布局联合使用，起码中间要隔着其他布局)
        StickyLayoutHelper stickyLayoutHelper = new StickyLayoutHelper();
        //stickyLayoutHelper.setStickyStart(false);//固定在底部
        stickyLayoutHelper.setStickyStart(true);//顶部(默认)
        adapters.add(new CenterAdapter(this, stickyLayoutHelper, 1)
        {
            @Override
            public void onBindViewHolder(CenterViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                holder.itemView.setBackgroundColor(Color.YELLOW);
            }
        }
        );


        //设置通栏布局
        SingleLayoutHelper slayoutHelper2 = new SingleLayoutHelper();
        //slayoutHelper.setBgColor(Color.BLUE);
        slayoutHelper2.setMargin(0, 30, 0, 20);//距离边框的长度
        adapters.add(new CenterAdapter(this,slayoutHelper2,1));

        //设置瀑布流布局
        StaggeredGridLayoutHelper staggeredGridLayoutHelper = new StaggeredGridLayoutHelper();
        staggeredGridLayoutHelper.setLane(3);//num为每行显示数目
        staggeredGridLayoutHelper.setGap(5);//gap为两个item的边距
        adapters.add(new CenterAdapter(this,staggeredGridLayoutHelper,30,null) {
            @Override
            public void onBindViewHolder(CenterViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                //为了做成瀑布流的效果，我们对每个item进行一个随机高度的设置：
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,260 + position % 7 * 20);
                holder.itemView.setLayoutParams(layoutParams);
            }
        });


        //Step 4 绑定delegateAdapter(总的适配器)
        boolean hasConsistItemType = false;//当hasConsistItemType=true的时候，不论是不是属于同一个子adapter，相同类型的item都能复用。表示它们共享一个类型。
                                            // 当hasConsistItemType=false的时候，不同子adapter之间的类型不共享
        DelegateAdapter delegateAdapter = new DelegateAdapter(layoutManager,hasConsistItemType);
        delegateAdapter.setAdapters(adapters);
        recyclerView.setAdapter(delegateAdapter);
    }

    private void setItemDecoration() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(5, 5, 5, 5);
            }
        });
    }
}
