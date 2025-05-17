package com.evenbus.myapplication.leak;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evenbus.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


/****
 * 综合分析之前的泄露和图片导致的OOM问题
 */
public class OomRecyclerActivity extends Activity {

        private static List<OomRecyclerActivity> leakedActivities = new ArrayList<>();
        private RecyclerView recyclerView;
        private LeakyAdapter adapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_memory_leak);

            // 故意将 Activity 添加到静态集合中造成泄漏
            leakedActivities.add(this);

            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // 创建有内存泄漏的 Adapter
            adapter = new LeakyAdapter(this, getDummyData());
            recyclerView.setAdapter(adapter);
        }

        private List<String> getDummyData() {
            List<String> data = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                data.add("Item " + i + " - " + UUID.randomUUID().toString());
            }
            return data;
        }

        // 有内存泄漏的 Adapter
        class LeakyAdapter extends RecyclerView.Adapter<LeakyViewHolder> {
            private Context context; // 持有 Activity 引用
            private List<String> data;
            private List<Bitmap> bitmaps = new ArrayList<>(); // 存储 Bitmap 造成内存溢出

            public LeakyAdapter(Context context, List<String> data) {
                this.context = context; // 直接持有 Activity 引用
                this.data = data;

                // 故意加载大图造成内存溢出
                loadLargeBitmaps();
            }

            private void loadLargeBitmaps() {
                for (int i = 0; i < 20; i++) {
                    // 创建大尺寸 Bitmap
                    Bitmap bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
                    // 用颜色填充
                    Canvas canvas = new Canvas(bitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.rgb(new Random().nextInt(255),
                            new Random().nextInt(255),
                            new Random().nextInt(255)));
                    canvas.drawRect(0, 0, 1000, 1000, paint);
                    bitmaps.add(bitmap); // 添加到集合中，不释放
                }
            }

            @NonNull
            @Override
            public LeakyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_leaky, parent, false);
                return new LeakyViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull LeakyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
                holder.bind(data.get(position), bitmaps.get(position % bitmaps.size()));

                // 设置点击监听器，内部类隐式持有 Activity 引用
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "Clicked " + position, Toast.LENGTH_SHORT).show();
                    }
                });
            }


            @Override
            public int getItemCount() {
                return data.size();
            }
        }

        // 有问题的 ViewHolder
        static class LeakyViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;
            private ImageView imageView;

            public LeakyViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView);
                imageView = itemView.findViewById(R.id.imageView);
            }

            public void bind(String text, Bitmap bitmap) {
                textView.setText(text);
                imageView.setImageBitmap(bitmap); // 直接设置 Bitmap
            }
        }
    }