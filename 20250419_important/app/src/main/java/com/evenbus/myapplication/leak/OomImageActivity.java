package com.evenbus.myapplication.leak;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OomImageActivity  extends AppCompatActivity {
    // 静态缓存导致Activity无法被回收
    /**
     *  静态集合持有Bitmap引用
     * sImageCache 是静态List，生命周期与Application相同
     * 所有加载过的Bitmap都会被永久保留
     * 浏览10张48MB的图片 → 480MB内存占用
     */
    private static final Map<String, Bitmap> sPhotoCache = new HashMap<>();

    private ImageView mPhotoView;
     String photoUrl="";
    // 静态集合导致内存泄漏的根本原因
    private static final List<Bitmap> sImageCache = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat_photo);
        mPhotoView = findViewById(R.id.photo_view);



         loadCachePhoto();
         putPhotoCache(null);
         loadImage("");
         loadListener(loadImage(photoUrl));
    }

    private void putPhotoCache(Bitmap bitmap) {
        // 错误2：加入静态缓存
        sImageCache.add(bitmap);
    }

    private void loadCachePhoto() {
        // 情况2：未缓存则加载大图（未做任何优化）
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(photoUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStream input = connection.getInputStream();

                    // 关键问题1：直接解码大图
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 关键问题2：加入静态缓存
                            sPhotoCache.put(photoUrl, bitmap);
                            mPhotoView.setImageBitmap(bitmap);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadListener(Bitmap bitmap) {
        // 错误2：加入静态缓存
        sImageCache.add(bitmap);

        mPhotoView.setImageBitmap(bitmap);

        // 错误3：监听器未正确移除
        mPhotoView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {}

            @Override
            public void onViewDetachedFromWindow(View v) {
                // 应该回收但未实现
            }
        });
    }


    private Bitmap loadImage(String path) {
        // 错误1：直接加载原图，无采样压缩
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        mPhotoView.setImageBitmap(bitmap);

        // 典型的高清图片可能达到4000x3000像素，每个像素占4字节
        // 单张图片内存 = 4000 * 3000 * 4 = 48MB
        /***
         * 未使用inSampleSize进行下采样
         *
         * 4000x3000的图片占用内存：
         *
         * ARGB_8888格式：4000×3000×4 = 48MB
         *
         * 即使设备屏幕只需1080x1920(约8MB)
         */
        return bitmap;
    }

}