package com.cn.zhaol.demo.androidtest.vlayout.mydemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.VirtualLayoutAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.cn.zhaol.demo.androidtest.R;

import java.util.List;

/**
 * 总布局文件适配器（自定义总适配器,不同布局合一）
 * Created by zhaolei on 2018/9/7.
 */
public class AllVLayoutAdapter extends VirtualLayoutAdapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private final static int Grid_VIEW = 0;
    private final static int Linear_VIEW = 1;
    private final static int Fix_VIEW = 2;
    private final static int Single_VIEW = 3;
    private final static int Staggered_VIEW = 4;

    private Integer[] images = new Integer[]{
            R.drawable.ic_data_music,
            R.drawable.ic_data_picture,
            R.drawable.ic_data_reader,
            R.drawable.ic_data_video};


    public AllVLayoutAdapter(@NonNull VirtualLayoutManager layoutManager,Context context) {
        super(layoutManager);
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("AllVLayoutAdapter","onCreateViewHolder()*********viewType = " + viewType);
        if(viewType == Grid_VIEW) {
            TopViewHolder holder = new TopViewHolder((LayoutInflater.from(mContext).inflate(R.layout.top_item, parent, false)));
            return holder;
        } else  {
            return new AllViewHolder((LayoutInflater.from(mContext).inflate(R.layout.center_item, parent, false)));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //分析所有布局文件，生成数据
        VirtualLayoutManager.LayoutParams layoutParams = new VirtualLayoutManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 200);
        holder.itemView.setLayoutParams(layoutParams);

        int viewType = holder.getItemViewType();

        Log.e("AllVLayoutAdapter","onBindViewHolder()*********viewType = " + viewType);

        //布局文件的显示
        if(viewType == Grid_VIEW) {
            TopViewHolder topHolder = (TopViewHolder) holder;
            topHolder.tv1.setText(Integer.toString(position));
            if(position < 4) {
                topHolder.imageView1.setImageResource(images[position]);
            }

        } else {
            AllViewHolder allHolder = (AllViewHolder) holder;
            allHolder.tv.setText(Integer.toString(position));
            if (position % 2 == 0) {
                holder.itemView.setBackgroundColor(0xaa00ff00);
            } else {
                holder.itemView.setBackgroundColor(0xccff00ff);
            }
        }

    }



    @Override
    public int getItemViewType(int position) {
        //默认是0,在这里修改 viewType 的值
        //这里使用简单的行数判断布局类型
        if(position <  4) {
            return Grid_VIEW;
        } else if(position >= 4 && position < 14) {
            return Linear_VIEW;
        } else if(position == 14) {
            return Fix_VIEW;
        } else if(position == 15) {
            return Single_VIEW;
        } else {
            return Staggered_VIEW;
        }
    }

    @Override
    public int getItemCount() {
        List<LayoutHelper> helpers = getLayoutHelpers();
        if (null == helpers) {
            return 0;
        }
        int count = 0;
        for (int i = 0, size = helpers.size(); i < size; i++) {
            count += helpers.get(i).getItemCount();//计算多少个子布局
        }
        return count;
    }

    //第一个适配器
    class AllViewHolder extends RecyclerView.ViewHolder{

        TextView tv;

        public AllViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.center_item_tv);
        }
    }

    //第二个适配器
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
