package com.cn.zhaol.demo.androidtest.vlayout.mydemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.cn.zhaol.demo.androidtest.R;

/**
 * 顶部布局
 * Created by zhaolei on 2018/9/6.
 */
public class TopAdapter extends DelegateAdapter.Adapter<TopAdapter.TopViewHolder>{

    private Context mContext;
    private LayoutHelper mLayoutHelper;
    private VirtualLayoutManager.LayoutParams mLayoutParams;
    private int mCount = 0;
    private Integer[] images = new Integer[]{
            R.drawable.ic_data_music,
            R.drawable.ic_data_picture,
            R.drawable.ic_data_reader,
            R.drawable.ic_data_video};

    public TopAdapter(Context context, LayoutHelper layoutHelper, int count) {
        this(context,layoutHelper,count,
                new VirtualLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
    }

    public TopAdapter(Context context, LayoutHelper layoutHelper, int count,
                      VirtualLayoutManager.LayoutParams layoutParams) {
        this.mContext = context;
        this.mLayoutHelper = layoutHelper;
        this.mCount = count;
        this.mLayoutParams = layoutParams;
    }

    @Override
    public int getItemViewType(int position) {
        //默认是0,在这里修改 viewType 的值
        return 2;
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        //可以在这里生产布局文件，也可以直接用穿进来的
        return mLayoutHelper;
    }

    @Override
    public TopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("TopAdapter","TopAdapter.viewType = " + viewType);
        TopViewHolder holder = new TopViewHolder((LayoutInflater.from(mContext).inflate(R.layout.top_item, parent, false)));
        return holder;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    @Override
    public void onBindViewHolder(TopViewHolder holder, int position) {
        //绑定数据
        if(null != mLayoutParams) {
            holder.itemView.setLayoutParams(new VirtualLayoutManager.LayoutParams(mLayoutParams));
        }

        holder.tv1.setText(Integer.toString(position));
        if(position < 4) {
            holder.imageView1.setImageResource(images[position]);
        }
    }

    class TopViewHolder extends RecyclerView.ViewHolder {
        TextView tv1;
        ImageView imageView1;

        public TopViewHolder(View itemView) {
            super(itemView);
            tv1 = (TextView) itemView.findViewById(R.id.top_item_tv);
            imageView1 = (ImageView) itemView.findViewById(R.id.top_item_img);
        }
    }
}
