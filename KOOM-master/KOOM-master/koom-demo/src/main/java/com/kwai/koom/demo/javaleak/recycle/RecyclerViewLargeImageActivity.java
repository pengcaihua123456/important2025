package com.kwai.koom.demo.javaleak.recycle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kwai.koom.demo.R;

/**
 * Activity demonstrating RecyclerView with large images causing memory pressure.
 * This is NOT a memory leak, but a normal memory usage issue that KOOM can detect.
 *
 * Each item loads a 1080x1920 bitmap (~8MB).
 * With 100 items, total memory usage can be ~800MB.
 */
public class RecyclerViewLargeImageActivity extends AppCompatActivity {

    private RecyclerViewLargeImageAdapter adapter;

    public static void start(Context context) {
        context.startActivity(new Intent(context, RecyclerViewLargeImageActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_large_image);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_large_image);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecyclerViewLargeImageAdapter();
        recyclerView.setAdapter(adapter);

        int estimatedMemoryMB = adapter.getTotalBitmapMemorySize();
        Toast.makeText(this,
                "Loading 100 large bitmaps (~8MB each)\nTotal: ~" + estimatedMemoryMB + "MB",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
