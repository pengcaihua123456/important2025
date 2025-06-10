package com.evenbus.myapplication.view.extand;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.evenbus.myapplication.R;

public class AnimationCardViewMiniPlus extends FrameLayout {
    private static  int DEFAULT_LIGHT_DURATION = 2000;
    private static final int DEFAULT_LIGHT_WIDTH = 10;
    private static final int DEFAULT_LIGHT_COLOR = Color.argb(180, 255, 255, 255);
    private static final int DEFAULT_CORNER_RADIUS = 20;
    private static final int DEFAULT_BACKGROUND = Color.parseColor("#3F51B5");

    private Paint mLightPaint;
    private Paint mBackgroundPaint;
    private Path mBorderPath;
    private Path mLightPath;
    private PathMeasure mPathMeasure;
    private float mPathLength;
    private float mCurrentProgress = 0;
    private ValueAnimator mLightAnimator;
    private RectF mCardRect;
    private float mCornerRadius;
    private boolean mIsLightRunning = false;

    public AnimationCardViewMiniPlus(Context context) {
        this(context, null);
    }

    public AnimationCardViewMiniPlus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationCardViewMiniPlus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);

        // 读取自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AnimationCardView);
        try {
            int lightColor = ta.getColor(R.styleable.AnimationCardView_lightColor, DEFAULT_LIGHT_COLOR);
            float lightWidth = ta.getDimension(R.styleable.AnimationCardView_lightWidth, DEFAULT_LIGHT_WIDTH);
            int lightDuration = ta.getInteger(R.styleable.AnimationCardView_lightDuration, DEFAULT_LIGHT_DURATION);
            mCornerRadius = ta.getDimension(R.styleable.AnimationCardView_cornerRadius, DEFAULT_CORNER_RADIUS);
            int cardBackground = ta.getColor(R.styleable.AnimationCardView_cardBackground, DEFAULT_BACKGROUND);

            // 初始化流光画笔
            mLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLightPaint.setStyle(Paint.Style.STROKE);
            mLightPaint.setStrokeWidth(lightWidth);
            mLightPaint.setColor(lightColor);
            mLightPaint.setShadowLayer(10, 0, 0, lightColor); // 添加发光效果

            // 初始化背景画笔
            mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            mBackgroundPaint.setColor(cardBackground);

            // 初始化路径
            mBorderPath = new Path();
            mLightPath = new Path();
            mCardRect = new RectF();

            // 设置默认动画时长
            DEFAULT_LIGHT_DURATION = lightDuration;
        } finally {
            ta.recycle();
        }

        // 禁用硬件加速以获得更好的发光效果
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 更新卡片矩形区域
        mCardRect.set(0, 0, w, h);

        // 创建卡片边框路径(圆角矩形)
        mBorderPath.reset();
        mBorderPath.addRoundRect(mCardRect, mCornerRadius, mCornerRadius, Path.Direction.CW);

        // 初始化路径测量
        mPathMeasure = new PathMeasure(mBorderPath, true);
        mPathLength = mPathMeasure.getLength();

        // 启动流光动画
        startLightAnimation();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 先绘制背景
        canvas.drawRoundRect(mCardRect, mCornerRadius, mCornerRadius, mBackgroundPaint);

        // 绘制子View
        super.dispatchDraw(canvas);

        // 最后绘制流光效果
        if (mIsLightRunning && mLightPath != null && !mLightPath.isEmpty()) {
            canvas.drawPath(mLightPath, mLightPaint);
        }
    }

    private void updateLightPath() {
        mLightPath.reset();

        // 计算流光起点和终点
        float start = mCurrentProgress * mPathLength;
        float end = (start + 0.2f * mPathLength) % mPathLength; // 流光长度为路径的20%

        if (start < end) {
            mPathMeasure.getSegment(start, end, mLightPath, true);
        } else {
            // 处理跨路径起点的情况
            mPathMeasure.getSegment(start, mPathLength, mLightPath, true);
            mPathMeasure.getSegment(0, end, mLightPath, true);
        }

        // 添加箭头效果
        addArrowToLight(end);
    }

    private void addArrowToLight(float endPos) {
        float[] pos = new float[2];
        float[] tan = new float[2];
        mPathMeasure.getPosTan(endPos, pos, tan);

        // 在流光末端添加小箭头
        Path arrowPath = new Path();
        arrowPath.moveTo(pos[0], pos[1]);
        arrowPath.lineTo(pos[0] - tan[1] * 15 - tan[0] * 10, pos[1] + tan[0] * 15 - tan[1] * 10);
        arrowPath.moveTo(pos[0], pos[1]);
        arrowPath.lineTo(pos[0] + tan[1] * 15 - tan[0] * 10, pos[1] - tan[0] * 15 - tan[1] * 10);

        mLightPath.addPath(arrowPath);
    }

    public void startLightAnimation() {
        if (mLightAnimator != null && mLightAnimator.isRunning()) {
            return;
        }

        mIsLightRunning = true;
        mLightAnimator = ValueAnimator.ofFloat(0, 1);
        mLightAnimator.setDuration(DEFAULT_LIGHT_DURATION);
        mLightAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mLightAnimator.addUpdateListener(animation -> {
            mCurrentProgress = (float) animation.getAnimatedValue();
            updateLightPath();
            invalidate();
        });
        mLightAnimator.start();
    }

    public void stopLightAnimation() {
        if (mLightAnimator != null) {
            mLightAnimator.cancel();
            mIsLightRunning = false;
            invalidate();
        }
    }

    public void setLightColor(int color) {
        mLightPaint.setColor(color);
        mLightPaint.setShadowLayer(10, 0, 0, color);
        invalidate();
    }

    public void setLightWidth(float width) {
        mLightPaint.setStrokeWidth(width);
        invalidate();
    }

    public void setCornerRadius(float radius) {
        mCornerRadius = radius;
        mBorderPath.reset();
        mBorderPath.addRoundRect(mCardRect, radius, radius, Path.Direction.CW);
        mPathMeasure.setPath(mBorderPath, true);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLightAnimation();
    }
}