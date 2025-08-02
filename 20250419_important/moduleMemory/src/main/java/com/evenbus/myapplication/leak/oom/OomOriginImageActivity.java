package com.evenbus.myapplication.leak.oom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_memory.R;


public class OomOriginImageActivity extends AppCompatActivity {

    private ImageView mPhotoView;
    private ImageView mPhotoBgView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat_photo);
        mPhotoView = findViewById(R.id.photo_view);
        mPhotoBgView = findViewById(R.id.photo_bg);
        textView = findViewById(R.id.tv_click);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap=loadImage(R.mipmap.smart);
                mPhotoView.setImageBitmap(bitmap);

//                Bitmap bitmap2=loadImage(R.drawable.biga);
//                mPhotoBgView.setImageBitmap(bitmap2);
            }
        });
    }

    private Bitmap loadImage(int res) {
        // 错误1：直接加载原图，无采样压缩
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap=BitmapFactory.decodeResource(getResources(), res ,options);
//        bitmap.mNativePtr;//
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
