package com.evenbus.myapplication.leak.oom;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_memory.R;


/**
 * 图片没有销毁OOM
 */
public class OomDestoryImageActivity extends AppCompatActivity {

    private ImageView mPhotoView;
    private TextView textView;
    private Bitmap currentBitmap;
    private LoadImageTask loadImageTask;

    private final View.OnAttachStateChangeListener attachListener =
            new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    Log.d("OomDestoryImageActivity", "onViewAttachedToWindow");
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    Log.d("OomDestoryImageActivity", "onViewDetachedFromWindow");
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat_photo);
        mPhotoView = findViewById(R.id.photo_view);
        textView = findViewById(R.id.tv_click);

        // 添加状态变化监听器
        mPhotoView.addOnAttachStateChangeListener(attachListener);

        // 设置点击事件
        textView.setOnClickListener(v -> loadImage());
    }

    private void loadImage() {
        // 取消任何正在进行的任务
        cancelPendingTask();

        // 启动新任务
        loadImageTask = new LoadImageTask();
        loadImageTask.execute();
    }

    private void setNewBitmap(Bitmap newBitmap) {
        if (newBitmap == null) return;

        // 保存旧位图引用
        Bitmap oldBitmap = currentBitmap;

        // 更新当前位图和视图
        currentBitmap = newBitmap;
        mPhotoView.setImageBitmap(newBitmap);

        // 安全回收旧位图
        safeRecycleBitmap(oldBitmap);
    }

    private void safeRecycleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return;

        // Android 8.0+ 的硬件位图无需手动回收
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                return;
            }
        }

        // 回收非硬件位图
        bitmap.recycle();
    }

    private void cancelPendingTask() {
        if (loadImageTask != null) {
            loadImageTask.cancel(true);
            loadImageTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 1. 取消任何正在进行的异步任务
        cancelPendingTask();

        // 2. 移除视图监听器
        mPhotoView.removeOnAttachStateChangeListener(attachListener);

    }

    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... params) {
            // 检查是否已取消
            if (isCancelled()) return null;

            try {
                // 在后台线程解码图片
                return decodeSampledBitmapFromResource(
                        getResources(), R.mipmap.smart, 200, 200);
            } catch (Exception e) {
                Log.e("LoadImageTask", "Error decoding bitmap", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // 检查Activity是否有效
            if (isFinishing() || isDestroyed()) {
                safeRecycleBitmap(bitmap); // 回收未使用的位图
                return;
            }

            // 更新UI
            setNewBitmap(bitmap);
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            // 任务取消时回收位图
            safeRecycleBitmap(bitmap);
        }
    }

    // 采样解码方法保持不变
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // 第一次解析将inJustDecodeBounds设置为true，获取图片尺寸
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // 计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 原始图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 计算最大的inSampleSize值，该值是2的幂，
            // 且保持高度和宽度大于所需的尺寸
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}