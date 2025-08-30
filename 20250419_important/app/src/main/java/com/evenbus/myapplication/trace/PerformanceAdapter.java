package com.evenbus.myapplication.trace;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.evenbus.myapplication.R;

import java.util.List;
import java.util.Random;

public class PerformanceAdapter extends RecyclerView.Adapter<PerformanceAdapter.ViewHolder> {

    private final List<String> items;
    private final BindTimeListener bindTimeListener;
    private long totalBindTime = 0;
    private int bindCount = 0;

    public interface BindTimeListener {
        void onBindTime(long bindTime);
    }

    public PerformanceAdapter(List<String> items, BindTimeListener listener) {
        this.items = items;
        this.bindTimeListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleText;
        public final TextView subtitleText;

        public ViewHolder(View view) {
            super(view);
            titleText = view.findViewById(R.id.titleText);
            subtitleText = view.findViewById(R.id.subtitleText);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        // 模拟耗时操作
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        long startTime = System.currentTimeMillis();

        // 模拟复杂数据绑定
        holder.titleText.setText("Item " + items.get(position));
        holder.subtitleText.setText("This is a long subtitle for item " + items.get(position) +
                " that will cause text measurement to take more time");

        // 故意制造性能问题 - 在主线程进行耗时操作
        if (position % 5 == 0) {
            simulateHeavyWork();
        }

        if (position % 3 == 0) {
            holder.titleText.setTextColor(Color.RED);
        } else {
            holder.titleText.setTextColor(Color.BLACK);
        }

        // 记录绑定耗时
        long bindTime = System.currentTimeMillis() - startTime;
        totalBindTime += bindTime;
        bindCount++;

        // 回调报告绑定时间
        if (bindTimeListener != null) {
            bindTimeListener.onBindTime(bindTime);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public double getAverageBindTime() {
        return bindCount > 0 ? (double) totalBindTime / bindCount : 0;
    }

    // 模拟耗时操作
    private void simulateHeavyWork() {
        // 1. 主线程耗时计算
        Random random = new Random();
        long sum = 0;
        for (int i = 0; i <= 100000000; i++) {
            sum += random.nextLong();
        }

    }
}