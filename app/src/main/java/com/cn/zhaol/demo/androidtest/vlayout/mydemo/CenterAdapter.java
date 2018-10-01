package com.cn.zhaol.demo.androidtest.vlayout.mydemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.cn.zhaol.demo.androidtest.R;

/**
 * Created by zhaolei on 2018/9/6.
 */
public class CenterAdapter extends DelegateAdapter.Adapter<CenterAdapter.CenterViewHolder> {

    private Context mContext;
    private LayoutHelper mLayoutHelper;//一个布局文件
    private VirtualLayoutManager.LayoutParams mLayoutParams;
    private int mCount = 0;

    public CenterAdapter(Context context, LayoutHelper layoutHelper, int count) {
        this(context,layoutHelper,count,
                new VirtualLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
    }

    public CenterAdapter(Context context, LayoutHelper layoutHelper, int count,
                      VirtualLayoutManager.LayoutParams layoutParams) {
        this.mContext = context;
        this.mLayoutHelper = layoutHelper;
        this.mCount = count;
        this.mLayoutParams = layoutParams;
    }

    @Override
    public int getItemViewType(int position) {
        //默认是0,在这里修改 viewType 的值
        return super.getItemViewType(position);
    }

    @Override
    public LayoutHelper onCreateLayoutHelper() {
        return mLayoutHelper;
    }

    @Override
    public CenterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("CenterAdapter","CenterAdapter.viewType = " + viewType);
        return new CenterViewHolder((LayoutInflater.from(mContext).inflate(R.layout.center_item, parent, false)));
    }

    @Override
    public void onBindViewHolder(CenterViewHolder holder, int position) {
        if(null != mLayoutParams) {
            holder.itemView.setLayoutParams(
                    new VirtualLayoutManager.LayoutParams(mLayoutParams));
        }
        if(position == 0) {
            String name = mLayoutHelper.getClass().getSimpleName();
            holder.tv.setText(name);
        } else {
            holder.tv.setText(Integer.toString(position));
        }

        if (position > 7) {
            holder.itemView.setBackgroundColor(0x66cc0000 + (position - 6) * 128);
        } else if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(0xaa22ff22);
        } else {
            holder.itemView.setBackgroundColor(0xccff22ff);
        }
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    class CenterViewHolder extends RecyclerView.ViewHolder{

        public TextView tv;

        public CenterViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.center_item_tv);
        }
    }
}
