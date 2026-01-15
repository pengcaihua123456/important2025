package com.example.matrixdemo.recycler;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matrixdemo.R;

import java.util.ArrayList;
import java.util.List;

public class RecycleViewActivity extends AppCompatActivity {


        RecyclerView mRecyclerView;
        MyAdapter mMyAdapter ;
        List<News> mNewsList = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.recycler);
            mRecyclerView = findViewById(R.id.recyclerview);
            // 构造一些数据
            for (int i = 0; i < 50; i++) {
                News news = new News();
                news.title = "标题" + i;
                news.content = "内容" + i;
                mNewsList.add(news);
            }
            mMyAdapter = new MyAdapter();
            mRecyclerView.setAdapter(mMyAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(RecycleViewActivity.this);
            mRecyclerView.setLayoutManager(layoutManager);

//            Looper.prepare();
//            Handler handler;
//            handler.obtainMessage();
        }

        class MyAdapter extends RecyclerView.Adapter<MyViewHoder> {

            @NonNull
            @Override
            public MyViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = View.inflate(RecycleViewActivity.this, R.layout.item, null);
                MyViewHoder myViewHoder = new MyViewHoder(view);
                return myViewHoder;
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHoder holder, int position) {
                News news = mNewsList.get(position);
                holder.mTitleTv.setText(news.title);
                holder.mTitleContent.setText(news.content);
            }

            @Override
            public int getItemCount() {
                return mNewsList.size();
            }
        }

        class MyViewHoder extends RecyclerView.ViewHolder {
            TextView mTitleTv;
            TextView mTitleContent;

            public MyViewHoder(@NonNull View itemView) {
                super(itemView);
                mTitleTv = itemView.findViewById(R.id.textView);
                mTitleContent = itemView.findViewById(R.id.textView2);
            }
        }
    }


