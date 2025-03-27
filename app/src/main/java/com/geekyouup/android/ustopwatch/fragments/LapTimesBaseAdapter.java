package com.geekyouup.android.ustopwatch.fragments;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geekyouup.android.ustopwatch.R;
import com.geekyouup.android.ustopwatch.util.TimeUtils;

public class LapTimesBaseAdapter extends BaseAdapter {

    /**
     * 数据记录方式（二维表， ArrayList里每个元素都是ArrayList）
     * 表里每个元素记录的是点击记圈按钮时当前的码表记录值，展示（获取view）的时候减法计算出差值
     */
    private final ArrayList<LapTimeBlock> mDataSet;
    private LayoutInflater mLayoutInflater;
    private final Context mContext;

    public LapTimesBaseAdapter(Context cxt, ArrayList<LapTimeBlock> dataSet) {
        mContext = cxt;
        mDataSet = dataSet;
    }

    @Override
    public int getCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mLayoutInflater == null)
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layoutView = convertView;
        if (layoutView == null) layoutView = mLayoutInflater.inflate(R.layout.laptimes_holder_list_item, null);

        LinearLayout listItemHolder = layoutView.findViewById(R.id.laptimes_list_item_holder);
        listItemHolder.removeAllViews();

        LapTimeBlock ltb = mDataSet.get(position);
        ArrayList<Double> lapTimes = ltb.getLapTimes();

        for (int i = 0; i < lapTimes.size(); i++) {
            View lapItemView = mLayoutInflater.inflate(R.layout.laptime_item, null);
            if (i == 0) {
                TextView t = lapItemView.findViewById(R.id.laptime_text);
                t.setText(TimeUtils.createBoldString(lapTimes.get(i)));
            }

            TextView t2 = lapItemView.findViewById(R.id.laptime_text2);
            if (i < lapTimes.size() - 1) {
                double laptime = lapTimes.get(i) - lapTimes.get(i + 1);
                if (laptime < 0) laptime = lapTimes.get(i);
                t2.setText(TimeUtils.createBoldString(laptime));
            } else {
                t2.setText(TimeUtils.createBoldString(lapTimes.get(i)));
            }

            listItemHolder.addView(lapItemView);
        }
        return layoutView;
    }
}
