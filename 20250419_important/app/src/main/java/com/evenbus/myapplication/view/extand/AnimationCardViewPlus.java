package com.evenbus.myapplication.view.extand;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.evenbus.myapplication.R;

/**
 * 带流光边框效果的可展开卡片,
 */
public class AnimationCardViewPlus  extends FrameLayout {
    // 基本动画参数
    private static final int ANIM_DURATION = 400;

    // 流光效果参数
    private static  int DEFAULT_LIGHT_DURATION = 2000;
    private static final int DEFAULT_LIGHT_WIDTH = 6;
    private static final int DEFAULT_LIGHT_COLOR = Color.argb(200, 100, 200, 255);
    private static final float DEFAULT_CORNER_RADIUS = 20f;

    // 视图组件
    private TextView mHeaderText;
    private View mContentView;

    // 状态标志
    private boolean mIsExpanded = false;

    // 动画控制
    private ValueAnimator mAnimator;
    private ValueAnimator mLightAnimator;

    // 尺寸参数
    private int mCollapsedHeight = 120;
    private int mExpandedHeight;

    // 文本处理
    private String mFullText = "今天的天气是非常的热的";
    private String mShortText = "今天";
    private int mCurrentTextLength = 0;
    private int mHeaderHeight = 0;
    private int mMaxContentWidth = 0;

    // 交互控制
    private long mLastClickTime = 0;
    private int mFixedLeftPosition = 0;

    // 流光效果相关
    private Paint mLightPaint;
    private Path mBorderPath;
    private Path mLightPath;
    private PathMeasure mPathMeasure;
    private float mPathLength;
    private float mCurrentProgress = 0;
    private RectF mCardRect;
    private float mCornerRadius = DEFAULT_CORNER_RADIUS;
    private boolean mIsLightEnabled = true;

    public AnimationCardViewPlus(Context context) {
        this(context, null);
    }

    public AnimationCardViewPlus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationCardViewPlus(Context context, AttributeSet attrs, int defStyleAttr) {
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
            mIsLightEnabled = ta.getBoolean(R.styleable.AnimationCardView_lightEnabled, true);

            // 初始化流光画笔
            mLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLightPaint.setStyle(Paint.Style.STROKE);
            mLightPaint.setStrokeWidth(lightWidth);
            mLightPaint.setColor(lightColor);
            mLightPaint.setShadowLayer(10, 0, 0, lightColor);

            // 初始化路径
            mBorderPath = new Path();
            mLightPath = new Path();
            mCardRect = new RectF();

            DEFAULT_LIGHT_DURATION = lightDuration;
        } finally {
            ta.recycle();
        }

        // 设置卡片背景和阴影
        setBackgroundResource(R.drawable.card_bg);
        setElevation(8f);

        // 禁用硬件加速以获得更好的发光效果
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderText = findViewById(R.id.headerTextplus);
        mContentView = findViewById(R.id.contentLayoutplus);

        mHeaderText.setText(mShortText);
        mCurrentTextLength = mShortText.length();
        mHeaderText.setMaxLines(1);
        mHeaderText.setEllipsize(TextUtils.TruncateAt.END);
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
        if (mIsLightEnabled) {
            startLightAnimation();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 测量Header
        int headerWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int headerHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mHeaderText.measure(headerWidthSpec, headerHeightSpec);
        mHeaderHeight = mHeaderText.getMeasuredHeight();
        mMaxContentWidth = mHeaderText.getMeasuredWidth();

        // 测量Content
        if (mContentView != null && (mIsExpanded || mCurrentTextLength == mFullText.length())) {
            int contentWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int contentHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            mContentView.measure(contentWidthSpec, contentHeightSpec);
            mExpandedHeight = mHeaderHeight + mContentView.getMeasuredHeight();
            mMaxContentWidth = Math.max(mMaxContentWidth, mContentView.getMeasuredWidth());
        }

        // 添加内边距
        mMaxContentWidth += getPaddingLeft() + getPaddingRight();

        // 确定最终尺寸
        int width = resolveSize(mMaxContentWidth, widthMeasureSpec);
        int height = mIsExpanded ? mExpandedHeight : mCollapsedHeight;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 记录初始左侧位置（仅首次）
        if (mFixedLeftPosition == 0) {
            mFixedLeftPosition = getLeft();
        }

        // 布局标题（顶部居中）
        mHeaderText.layout(
                getPaddingLeft(),
                getPaddingTop(),
                getPaddingLeft() + mHeaderText.getMeasuredWidth(),
                getPaddingTop() + mHeaderHeight
        );

        // 布局内容区域（仅展开状态）
        if (mIsExpanded && mContentView != null) {
            mContentView.layout(
                    getPaddingLeft(),
                    getPaddingTop() + mHeaderHeight,
                    getPaddingLeft() + mContentView.getMeasuredWidth(),
                    getPaddingTop() + mHeaderHeight + mContentView.getMeasuredHeight()
            );
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 先绘制背景
        super.dispatchDraw(canvas);

        // 绘制流光效果
        if (mIsLightEnabled && mLightPath != null && !mLightPath.isEmpty()) {
            canvas.drawPath(mLightPath, mLightPaint);
        }
    }

    // ================= 流光效果相关方法 ================= //

    private void startLightAnimation() {
        if (mLightAnimator != null && mLightAnimator.isRunning()) {
            return;
        }

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

    private void updateLightPath() {
        mLightPath.reset();

        // 计算流光起点和终点
        float start = mCurrentProgress * mPathLength;
        float end = (start + 0.2f * mPathLength) % mPathLength;

        if (start < end) {
            mPathMeasure.getSegment(start, end, mLightPath, true);
        } else {
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

        Path arrowPath = new Path();
        arrowPath.moveTo(pos[0], pos[1]);
        arrowPath.lineTo(pos[0] - tan[1] * 12 - tan[0] * 8, pos[1] + tan[0] * 12 - tan[1] * 8);
        arrowPath.moveTo(pos[0], pos[1]);
        arrowPath.lineTo(pos[0] + tan[1] * 12 - tan[0] * 8, pos[1] - tan[0] * 12 - tan[1] * 8);

        mLightPath.addPath(arrowPath);
    }

    // ================= 原有卡片动画方法 ================= //

    public void startExpandAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        if (getLeft() != mFixedLeftPosition) {
            layout(mFixedLeftPosition, getTop(),
                    mFixedLeftPosition + getWidth(), getBottom());
        }

        ValueAnimator textAnimator = ValueAnimator.ofInt(mShortText.length(), mFullText.length());
        textAnimator.setDuration(ANIM_DURATION);
        textAnimator.addUpdateListener(animation -> {
            mCurrentTextLength = (int) animation.getAnimatedValue();
            mHeaderText.setText(mFullText.substring(0, mCurrentTextLength));
            requestLayout();

            if (mCurrentTextLength == mFullText.length()) {
                expandCard();
            }
        });
        textAnimator.start();
    }

    private void expandCard() {
        mIsExpanded = true;
        if (mContentView != null) {
            mContentView.setVisibility(View.VISIBLE);
            mContentView.setAlpha(0f);
        }

        ValueAnimator heightAnim = ValueAnimator.ofInt(getHeight(), mExpandedHeight);
        heightAnim.setDuration(ANIM_DURATION / 2);
        heightAnim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            setLayoutParams(params);

            if (mContentView != null) {
                mContentView.setAlpha(animation.getAnimatedFraction());
            }

            if (getLeft() != mFixedLeftPosition) {
                layout(mFixedLeftPosition, getTop(),
                        mFixedLeftPosition + getWidth(), getBottom());
            }
        });
        heightAnim.start();
    }

    public void collapseCard() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        ValueAnimator textAnimator = ValueAnimator.ofInt(mCurrentTextLength, mShortText.length());
        textAnimator.setDuration(ANIM_DURATION);
        textAnimator.addUpdateListener(animation -> {
            mCurrentTextLength = (int) animation.getAnimatedValue();
            mHeaderText.setText(mFullText.substring(0, mCurrentTextLength));
            requestLayout();

            if (getLeft() != mFixedLeftPosition) {
                layout(mFixedLeftPosition, getTop(),
                        mFixedLeftPosition + getWidth(), getBottom());
            }
        });

        ValueAnimator heightAnim = ValueAnimator.ofInt(getHeight(), mCollapsedHeight);
        heightAnim.setDuration(ANIM_DURATION / 2);
        heightAnim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            setLayoutParams(params);

            if (mContentView != null) {
                mContentView.setAlpha(1f - animation.getAnimatedFraction());
            }
        });
        heightAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsExpanded = false;
                if (mContentView != null) {
                    mContentView.setVisibility(View.GONE);
                }
            }
        });

        textAnimator.start();
        heightAnim.start();
    }

    public void toggleCard() {
        if (System.currentTimeMillis() - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = System.currentTimeMillis();

        if (mIsExpanded) {
            collapseCard();
        } else {
            startExpandAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        if (mLightAnimator != null) {
            mLightAnimator.cancel();
        }
    }

    // ================= 流光效果控制方法 ================= //

    public void setLightEnabled(boolean enabled) {
        if (mIsLightEnabled != enabled) {
            mIsLightEnabled = enabled;
            if (enabled) {
                startLightAnimation();
            } else if (mLightAnimator != null) {
                mLightAnimator.cancel();
            }
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
}