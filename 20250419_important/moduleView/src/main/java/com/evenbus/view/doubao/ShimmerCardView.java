package com.evenbus.view.doubao;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

public class ShimmerCardView extends FrameLayout {
    // 基础画笔：黑色卡片底色
    private Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // 高光画笔：渐变流光
    private Paint mShimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LinearGradient mLinearGradient;
    private Matrix mGradientMatrix = new Matrix();
    // 高光偏移量、控件宽高、圆角
    private float mShimmerOffset = 0f;
    private int mViewW, mViewH;
    private final float mRadius = 24f; // 圆角大小
    // 高光色：中间纯白，两边淡紫透明
    private final int[] mShimmerColors = new int[]{
            Color.TRANSPARENT,
            0xFFE8D8FF, // 淡紫光
            Color.WHITE,
            0xFFE8D8FF,
            Color.TRANSPARENT
    };
    private ValueAnimator mShimmerAnim;
    private RectF mCardRect = new RectF();

    public ShimmerCardView(Context context) {
        super(context);
        init();
    }

    public ShimmerCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Log.e("peng", "ShimmerCardView init");
        setWillNotDraw(false); // 启用自定义绘制
        mBgPaint.setColor(0xFF222222); // 卡片黑底色
        mShimmerPaint.setStyle(Paint.Style.FILL);
        // 动画在 onSizeChanged 中创建，因为需要宽度
    }

    // 初始化无限循环位移动画
    private void createAnim() {
        Log.e("peng", "createAnim, mViewW=" + mViewW);
        // 动画范围：从左侧外一个半宽度到右侧外一个半宽度
        mShimmerAnim = ValueAnimator.ofFloat(-mViewW * 1.5f, mViewW * 1.5f);
        mShimmerAnim.setDuration(2200); // 单次扫光时长
        mShimmerAnim.setRepeatCount(ValueAnimator.INFINITE);
        mShimmerAnim.setInterpolator(new LinearInterpolator());
        mShimmerAnim.addUpdateListener(anim -> {
            mShimmerOffset = (float) anim.getAnimatedValue();
            invalidate(); // 刷新重绘
        });
        mShimmerAnim.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("peng", "onSizeChanged: w=" + w + ", h=" + h);
        mViewW = w;
        mViewH = h;
        // 计算卡片矩形区域（考虑padding）
        mCardRect.set(getPaddingLeft(), getPaddingTop(), 
                      w - getPaddingRight(), h - getPaddingBottom());
        // 斜向线性渐变：高光斜切（和截图斜向光效匹配）
        mLinearGradient = new LinearGradient(
                -mViewW, 0,
                mViewW * 0.7f, mViewH, // 斜向终点，控制光倾斜角度
                mShimmerColors, null, Shader.TileMode.CLAMP
        );
        mShimmerPaint.setShader(mLinearGradient);
        // 创建并启动动画
        if (mShimmerAnim != null) {
            mShimmerAnim.cancel();
        }
        createAnim();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 1. 绘制底层黑色圆角卡片
        canvas.drawRoundRect(mCardRect, mRadius, mRadius, mBgPaint);

        // 2. 如果渐变已初始化，更新渐变平移矩阵，实现高光右移
        if (mLinearGradient != null) {
            mGradientMatrix.setTranslate(mShimmerOffset, 0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);
            mShimmerPaint.setShader(mLinearGradient);
            // 3. 绘制高光流光（和卡片同圆角，裁切超出部分）
            canvas.drawRoundRect(mCardRect, mRadius, mRadius, mShimmerPaint);
        }
        
        // 4. 绘制子视图（文本内容）
        super.dispatchDraw(canvas);
    }

    // 页面销毁停止动画，防内存泄漏
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.e("peng", "onDetachedFromWindow");
        if (mShimmerAnim != null) {
            mShimmerAnim.cancel();
        }
    }
}