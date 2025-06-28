package com.evenbus.view.extand;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.module_view.R;


/**
 * 自定义可展开卡片视图，继承自FrameLayout
 * 功能特点：
 *  1. 支持折叠/展开两种状态切换
 *  2. 标题文本长度动画（展开时逐渐变长，折叠时逐渐缩短）
 *  3. 内容区域高度动画（展开时高度增加并渐显，折叠时高度减少并渐隐）
 *  4. 卡片位置固定（防止动画过程中位置偏移）
 */
public class AnimationCardView extends FrameLayout {
    // 动画持续时间（毫秒）
    private static final int ANIM_DURATION = 400;

    // 视图组件
    private TextView mHeaderText;    // 标题文本视图
    private View mContentView;        // 内容区域视图

    // 状态标志
    private boolean mIsExpanded = false; // 当前是否展开状态

    // 动画控制
    private ValueAnimator mAnimator; // 动画执行器

    // 尺寸参数
    private int mCollapsedHeight = 120; // 折叠状态高度（单位：dp）
    private int mExpandedHeight;      // 展开状态高度

    // 文本处理
    private String mFullText = "今天的天气是非常的热的"; // 完整标题文本
    private String mShortText = "今天";              // 折叠状态标题文本
    private int mCurrentTextLength = 0;             // 当前显示文本长度

    // 布局参数
    private int mHeaderHeight = 0;      // 标题栏高度
    private int mMaxContentWidth = 0;   // 最大内容宽度

    // 交互控制
    private long mLastClickTime = 0;    // 上次点击时间（防抖）
    private int mFixedLeftPosition = 0; // 固定左侧位置（防止动画偏移）

    // 构造方法
    public AnimationCardView(Context context) {
        this(context, null);
    }

    public AnimationCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // 初始化视图
    private void init() {
        // 设置卡片背景和阴影
        setBackgroundResource(R.drawable.card_bg);
        setElevation(8f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 获取子视图引用
        mHeaderText = findViewById(R.id.headerText);
        mContentView = findViewById(R.id.contentLayout);

        // 初始化标题设置
        mHeaderText.setText(mShortText);
        mCurrentTextLength = mShortText.length();
        mHeaderText.setMaxLines(1); // 折叠时单行显示
        mHeaderText.setEllipsize(TextUtils.TruncateAt.END); // 超出部分省略
    }

    /**
     * 测量布局尺寸
     * 设计思路：
     *  1. 分别测量标题和内容区域
     *  2. 计算最大内容宽度
     *  3. 根据展开状态确定最终高度
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 测量标题区域
        int headerWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int headerHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mHeaderText.measure(headerWidthSpec, headerHeightSpec);
        mHeaderHeight = mHeaderText.getMeasuredHeight();
        mMaxContentWidth = mHeaderText.getMeasuredWidth();

        // 测量内容区域（仅展开状态或文本完全展开时）
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

    /**
     * 布局子视图
     * 设计要点：
     *  1. 首次布局记录左侧固定位置
     *  2. 标题始终固定在顶部
     *  3. 内容区域在展开状态下显示在标题下方
     */
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

    // ================= 动画控制方法 ================= //

    /**
     * 启动展开动画
     * 设计流程：
     *  1. 文本动画：短文本→完整文本（400ms）
     *  2. 文本完成后触发卡片展开动画
     */
    public void startExpandAnimation() {
        // 取消进行中的动画
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        // 确保位置固定（防止父布局移动）
        if (getLeft() != mFixedLeftPosition) {
            layout(mFixedLeftPosition, getTop(),
                    mFixedLeftPosition + getWidth(), getBottom());
        }

        // 文本长度动画（从当前长度到完整长度）
        ValueAnimator textAnimator = ValueAnimator.ofInt(mShortText.length(), mFullText.length());
        textAnimator.setDuration(ANIM_DURATION);
        textAnimator.addUpdateListener(animation -> {
            // 更新当前显示文本长度
            mCurrentTextLength = (int) animation.getAnimatedValue();
            mHeaderText.setText(mFullText.substring(0, mCurrentTextLength));

            // 触发重新布局
            requestLayout();

            // 文本完全展开后启动卡片展开
            if (mCurrentTextLength == mFullText.length()) {
                expandCard();
            }
        });
        textAnimator.start();
    }

    /**
     * 执行卡片展开动画
     * 动画效果：
     *  1. 高度变化：当前高度→展开高度（200ms）
     *  2. 内容区域渐显效果
     */
    private void expandCard() {
        mIsExpanded = true;
        if (mContentView != null) {
            mContentView.setVisibility(View.VISIBLE);
            mContentView.setAlpha(0f); // 初始完全透明
        }

        // 高度变化动画
        ValueAnimator heightAnim = ValueAnimator.ofInt(getHeight(), mExpandedHeight);
        heightAnim.setDuration(ANIM_DURATION / 2);
        heightAnim.addUpdateListener(animation -> {
            // 更新布局高度
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            setLayoutParams(params);

            // 内容区域透明度渐变
            if (mContentView != null) {
                mContentView.setAlpha(animation.getAnimatedFraction());
            }

            // 强制保持左侧位置
            if (getLeft() != mFixedLeftPosition) {
                layout(mFixedLeftPosition, getTop(),
                        mFixedLeftPosition + getWidth(), getBottom());
            }
        });
        heightAnim.start();
    }

    /**
     * 折叠卡片动画
     * 设计流程：
     *  1. 同时执行文本缩短动画和高度减少动画
     *  2. 动画结束隐藏内容区域
     */
    public void collapseCard() {
        // 取消进行中的动画
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        // 文本缩短动画
        ValueAnimator textAnimator = ValueAnimator.ofInt(mCurrentTextLength, mShortText.length());
        textAnimator.setDuration(ANIM_DURATION);
        textAnimator.addUpdateListener(animation -> {
            mCurrentTextLength = (int) animation.getAnimatedValue();
            mHeaderText.setText(mFullText.substring(0, mCurrentTextLength));
            requestLayout(); // 触发重布局

            // 位置修正
            if (getLeft() != mFixedLeftPosition) {
                layout(mFixedLeftPosition, getTop(),
                        mFixedLeftPosition + getWidth(), getBottom());
            }
        });

        // 高度折叠动画
        ValueAnimator heightAnim = ValueAnimator.ofInt(getHeight(), mCollapsedHeight);
        heightAnim.setDuration(ANIM_DURATION / 2);
        heightAnim.addUpdateListener(animation -> {
            // 更新布局高度
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            setLayoutParams(params);

            // 内容区域渐隐
            if (mContentView != null) {
                mContentView.setAlpha(1f - animation.getAnimatedFraction());
            }
        });
        heightAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsExpanded = false;
                // 折叠完成后隐藏内容区域
                if (mContentView != null) {
                    mContentView.setVisibility(View.GONE);
                }
            }
        });

        // 同时启动两个动画
        textAnimator.start();
        heightAnim.start();
    }

    /**
     * 切换卡片状态（带点击防抖）
     * 设计要点：
     *  1. 500ms内防止重复点击
     *  2. 根据当前状态触发展开/折叠
     */
    public void toggleCard() {
        // 点击防抖（500ms间隔）
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

    // 生命周期管理（防止内存泄漏）
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}
