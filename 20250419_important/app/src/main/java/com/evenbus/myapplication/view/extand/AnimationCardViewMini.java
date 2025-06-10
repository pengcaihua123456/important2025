package com.evenbus.myapplication.view.extand;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.evenbus.myapplication.R;

/**
 * @Author pengcaihua
 * @Date 14:00
 * @describe 仅仅带文本扩展
 */
public class AnimationCardViewMini extends FrameLayout {
    // 动画持续时间
    private static final int ANIM_DURATION = 400;

    // 视图组件
    private TextView mHeaderText;

    // 状态标志
    private boolean mIsExpanded = false;

    // 文本参数
    private String mFullText = "今天的天气是非常的热的";
    private String mShortText = "今天";
    private int mCurrentTextLength = 0;

    // 尺寸参数
    private int mCollapsedHeight;
    private int mExpandedHeight;
    private int mSingleLineHeight;

    // 交互控制
    private long mLastClickTime = 0;

    public AnimationCardViewMini(Context context) {
        this(context, null);
    }

    public AnimationCardViewMini(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 设置卡片样式
        setBackgroundResource(R.drawable.card_bg);
        setElevation(4f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderText = findViewById(R.id.headerText);

        // 初始化为折叠状态
        mHeaderText.setText(mShortText);
        mCurrentTextLength = mShortText.length();
        applyCollapsedTextStyle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 测量折叠状态高度
        measureTextWithStyle(true);
        mCollapsedHeight = mHeaderText.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
        mSingleLineHeight = mCollapsedHeight;

        // 测量展开状态高度
        measureTextWithStyle(false);
        mExpandedHeight = mHeaderText.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();

        // 设置当前高度
        int height = mIsExpanded ? mExpandedHeight : mCollapsedHeight;
        int width = resolveSize(mHeaderText.getMeasuredWidth(), widthMeasureSpec);

        setMeasuredDimension(width, height);
    }

    // 辅助方法：测量文本尺寸（指定样式）
    private void measureTextWithStyle(boolean collapsed) {
        // 保存原始样式
        int originalLines = mHeaderText.getMaxLines();
        TextUtils.TruncateAt originalEllipsize = mHeaderText.getEllipsize();

        // 应用指定样式
        if (collapsed) {
            mHeaderText.setMaxLines(1);
            mHeaderText.setEllipsize(TextUtils.TruncateAt.END);
        } else {
            mHeaderText.setMaxLines(Integer.MAX_VALUE);
            mHeaderText.setEllipsize(null);
        }

        // 测量文本
        mHeaderText.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );

        // 恢复原始样式
        mHeaderText.setMaxLines(originalLines);
        mHeaderText.setEllipsize(originalEllipsize);
    }

    // ================= 动画控制 ================= //

    /**
     * 文本展开动画（从左到右）
     */
    public void expandText() {
        // 设置初始样式（单行无省略）
        applyExpandingTextStyle();

        ValueAnimator animator = ValueAnimator.ofInt(mShortText.length(), mFullText.length());
        animator.setDuration(ANIM_DURATION);
        animator.addUpdateListener(animation -> {
            // 逐步增加文本长度
            mCurrentTextLength = (int) animation.getAnimatedValue();
            mHeaderText.setText(mFullText.substring(0, mCurrentTextLength));

            // 触发重绘
            requestLayout();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束应用展开样式
                applyExpandedTextStyle();
                mIsExpanded = true;
            }
        });
        animator.start();
    }

    /**
     * 文本折叠动画（从右到左）
     */
    public void collapseText() {
        ValueAnimator animator = ValueAnimator.ofInt(mFullText.length(), mShortText.length());
        animator.setDuration(ANIM_DURATION);
        animator.addUpdateListener(animation -> {
            // 逐步减少文本长度
            mCurrentTextLength = (int) animation.getAnimatedValue();
            mHeaderText.setText(mFullText.substring(0, mCurrentTextLength));

            // 触发重绘
            requestLayout();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束应用折叠样式
                applyCollapsedTextStyle();
                mIsExpanded = false;
            }
        });
        animator.start();
    }

    // 文本样式设置
    private void applyCollapsedTextStyle() {
        mHeaderText.setMaxLines(1);
        mHeaderText.setEllipsize(TextUtils.TruncateAt.END);
        mHeaderText.setText(mShortText);
    }

    private void applyExpandingTextStyle() {
        mHeaderText.setMaxLines(1);
        mHeaderText.setEllipsize(null); // 禁用省略号
    }

    private void applyExpandedTextStyle() {
        mHeaderText.setMaxLines(Integer.MAX_VALUE);
        mHeaderText.setEllipsize(null);
        mHeaderText.setText(mFullText);
    }

    /**
     * 切换文本状态
     */
    public void toggleText() {
        // 点击防抖（300ms）
        if (System.currentTimeMillis() - mLastClickTime < 300) return;
        mLastClickTime = System.currentTimeMillis();

        if (mIsExpanded) {
            collapseText();
        } else {
            expandText();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 清理动画资源
    }


}