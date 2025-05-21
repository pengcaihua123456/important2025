package com.evenbus.myapplication.leak.oom;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class OomQueueImageActivity extends AppCompatActivity {

    private static final String TAG="OomQueueImageActivity";


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
                Log.d(TAG,"onClick");
                Bitmap bitmap2 = decodeSampledBitmapFromResource(getResources(), R.mipmap.smart, 1000, 1000);
                Bitmap bitmap3 = decodeSampledBitmapFromResource(getResources(), R.mipmap.big, 1000, 1000);
                Bitmap bitmap4= decodeSampledBitmapFromResource(getResources(), R.mipmap.biga, 1000, 1000);

                putPhotoCache(bitmap2);
                putPhotoCache(bitmap3);
                putPhotoCache(bitmap4);
            }

        });
    }
    private static final List<Bitmap> sImageCache = new ArrayList<>();
    private void putPhotoCache(Bitmap bitmap) {
        // 错误2：加入静态缓存
        sImageCache.add(bitmap);
    }

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
