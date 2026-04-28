package com.kwai.koom.demo.javaleak.recycle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kwai.koom.demo.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for loading large images.
 * This demonstrates memory pressure caused by loading too many large bitmaps.
 */
public class RecyclerViewLargeImageAdapter extends RecyclerView.Adapter<RecyclerViewLargeImageAdapter.ViewHolder> {

    private static final int ITEM_COUNT = 100;
    private static final int BITMAP_WIDTH = 1080;
    private static final int BITMAP_HEIGHT = 1920;

    private List<WeakReference<Bitmap>> bitmapCache = new ArrayList<>();

    public RecyclerViewLargeImageAdapter() {
        // Pre-create bitmaps to cause memory pressure
        // Each bitmap is about 8MB (1080 * 1920 * 4 bytes per pixel)
        for (int i = 0; i < ITEM_COUNT; i++) {
            bitmapCache.add(new WeakReference<>(createLargeBitmap(i)));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycler_large_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvIndex.setText("Item #" + position);

        WeakReference<Bitmap> ref = bitmapCache.get(position);
        Bitmap bitmap = ref != null ? ref.get() : null;

        if (bitmap == null) {
            bitmap = createLargeBitmap(position);
            bitmapCache.set(position, new WeakReference<>(bitmap));
        }

        holder.ivLargeImage.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    /**
     * Create a large bitmap for testing memory pressure.
     * Each bitmap is approximately 8MB (1080 * 1920 * 4 bytes).
     */
    private Bitmap createLargeBitmap(int index) {
        Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int hue = (index * 15) % 360;
        int color = Color.HSVToColor(new float[]{hue, 0.7f, 0.9f});
        canvas.drawColor(color);

        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Bitmap #" + index, BITMAP_WIDTH / 2f, BITMAP_HEIGHT / 2f, paint);

        paint.setTextSize(60);
        canvas.drawText(BITMAP_WIDTH + " x " + BITMAP_HEIGHT, BITMAP_WIDTH / 2f, BITMAP_HEIGHT / 2f + 150, paint);

        paint.setTextSize(50);
        canvas.drawText("Memory Usage: ~8MB", BITMAP_WIDTH / 2f, BITMAP_HEIGHT / 2f + 220, paint);

        return bitmap;
    }

    public int getTotalBitmapMemorySize() {
        int count = 0;
        for (WeakReference<Bitmap> ref : bitmapCache) {
            Bitmap b = ref != null ? ref.get() : null;
            if (b != null && !b.isRecycled()) {
                count++;
            }
        }
        return count * BITMAP_WIDTH * BITMAP_HEIGHT * 4 / (1024 * 1024);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLargeImage;
        TextView tvIndex;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLargeImage = itemView.findViewById(R.id.iv_large_image);
            tvIndex = itemView.findViewById(R.id.tv_index);
        }
    }
}
