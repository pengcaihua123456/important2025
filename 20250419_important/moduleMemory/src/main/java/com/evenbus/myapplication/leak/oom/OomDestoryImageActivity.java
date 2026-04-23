package com.evenbus.myapplication.leak.oom;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_memory.R;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Native泄露，没有达到
 * 图片被 Activity 持有且没有释放导致 OOM 的 Demo
 *
 * 泄漏原因：
 * 1. Activity 的实例变量（mBitmapList）持有所有加载的 Bitmap
 * 2. onDestroy 中不清空列表，导致 Activity 无法被 GC
 * 3. 旧 Bitmap 没有回收
 */
public class OomDestoryImageActivity extends AppCompatActivity {

    private static final String TAG = "OomDestoryImageActivity";

    private ImageView mPhotoView;
    private TextView textView;
    private TextView tvCount;

    // 🔴 泄漏点1：Activity 的实例变量持有所有 Bitmap
    // Activity 销毁时，如果不清空，所有 Bitmap 都无法释放
    private List<Bitmap> mBitmapList = new ArrayList<>();

    private LoadImageTask loadImageTask;
    private int loadCount = 0;
    private int totalCount = 30;  // 总共加载30次

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat_photo);

        mPhotoView = findViewById(R.id.photo_view);
        textView = findViewById(R.id.tv_click);
//        tvCount = findViewById(R.id.tv_count);

        // 显示加载进度
        updateCountDisplay();

        // 设置点击事件 - 点击后开始循环加载
        textView.setOnClickListener(v -> startLoadImages());
    }

    private void updateCountDisplay() {
        if (tvCount != null) {
            tvCount.setText("已加载: " + loadCount + " / " + totalCount +
                    "\n缓存Bitmap数: " + mBitmapList.size());
        }
    }

    private void startLoadImages() {
        // 禁用按钮，防止重复点击
        textView.setEnabled(false);
        textView.setText("加载中...");

        // 重置计数
        loadCount = 0;
        updateCountDisplay();

        // 开始循环加载
        loadNextImage();
    }

    private void loadNextImage() {
        if (loadCount >= totalCount) {
            // 加载完成
            textView.setEnabled(true);
            textView.setText("加载完成，共 " + totalCount + " 次\n缓存Bitmap数: " + mBitmapList.size());
            Log.d(TAG, "所有图片加载完成，共 " + totalCount + " 次，当前持有Bitmap数量: " + mBitmapList.size());
            return;
        }

        loadCount++;
        updateCountDisplay();
        Log.d(TAG, "开始加载第 " + loadCount + " 张图片");

        // 取消任何正在进行的任务
        cancelPendingTask();

        // 启动新任务
        loadImageTask = new LoadImageTask();
        loadImageTask.execute();
    }

    private void addBitmap(Bitmap newBitmap) {
        if (newBitmap == null) {
            // 加载失败，继续下一张
            loadNextImage();
            return;
        }

        // 🔴 泄漏点2：将 Bitmap 添加到 Activity 持有的列表中
        // 从不移除，导致列表越来越大
        mBitmapList.add(newBitmap);

        long totalMemory = 0;
        for (Bitmap bmp : mBitmapList) {
            totalMemory += bmp.getByteCount();
        }
        Log.d(TAG, "添加 Bitmap 到列表，当前数量: " + mBitmapList.size() +
                ", 总内存约: " + (totalMemory / 1024 / 1024) + "MB");

        // 更新 ImageView 显示最新的一张
        mPhotoView.setImageBitmap(newBitmap);
        updateCountDisplay();

        // 继续加载下一张
        loadNextImage();
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

        Log.d(TAG, "onDestroy, 当前 Activity 持有 " + mBitmapList.size() + " 张 Bitmap");

        // 🔴 泄漏点3：注释掉了清理逻辑，Bitmap 列表没有被清空
        // Activity 虽然销毁了，但 mBitmapList 中的 Bitmap 仍然被这个 Activity 实例持有
        // 由于 Activity 实例本身无法被 GC，所有 Bitmap 也无法释放

        // 正确的做法应该是：
        // for (Bitmap bmp : mBitmapList) {
        //     if (bmp != null && !bmp.isRecycled()) {
        //         bmp.recycle();
        //     }
        // }
        // mBitmapList.clear();
        // cancelPendingTask();
    }

    /**
     * AsyncTask 是内部类，隐式持有外部 Activity 引用
     * 如果任务在 Activity 销毁后仍在运行，会导致 Activity 无法被 GC
     */
    private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            if (isCancelled()) return null;

            try {
                // 加载原图，不压缩，最大化内存占用
                return decodeSampledBitmapFromResource(
                        getResources(), R.mipmap.smart, 6000, 6000);
            } catch (Exception e) {
                Log.e(TAG, "Error decoding bitmap", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                // 加载失败，继续下一张
                loadNextImage();
                return;
            }

            // 检查Activity是否有效
            if (isFinishing() || isDestroyed()) {
                // 🔴 泄漏点4：Activity 已销毁时，Bitmap 没有被回收
                // 直接返回导致 Bitmap 泄漏
                Log.w(TAG, "Activity 已销毁，Bitmap 未回收，造成泄漏");
                return;
            }

            // 添加 Bitmap 到列表
            addBitmap(bitmap);
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            // 🔴 泄漏点5：任务取消时也不回收 Bitmap
            Log.w(TAG, "任务取消，Bitmap 未回收");
            loadNextImage();
        }
    }

    /**
     * 解码图片 - 加载原图（不压缩）
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // 加载原图，不压缩，最大化内存占用
        boolean isOriginal = true;

        if (isOriginal) {
            Log.w(TAG, "isOriginal");
            options.inSampleSize = 1;
        } else {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
        }

        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 计算采样率
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}