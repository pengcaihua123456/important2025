package com.evenbus.myapplication.view.flow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 可滚动的流式布局实现
 * 功能特点：
 * 1. 流式布局：子View自动换行排列
 * 2. 支持滚动：垂直方向可滑动
 * 3. 支持fling：快速滑动后惯性滚动
 * 4. 支持padding和margin
 */
public class ScrollableFlowLayout extends ViewGroup {
    // 水平间距(dp)
    private int horizontalSpacing = 16;
    // 垂直间距(dp)
    private int verticalSpacing = 16;
    // 滚动控制器，用于实现平滑滚动
    private Scroller mScroller;
    // 速度跟踪器，用于计算fling速度
    private VelocityTracker mVelocityTracker;
    // 系统认为的滑动最小距离
    private int mTouchSlop;
    // 系统认为的最小fling速度
    private int mMinimumVelocity;
    // 系统认为的最大fling速度
    private int mMaximumVelocity;

    // 记录上次触摸事件的Y坐标
    private float mLastY;
    // 是否正在拖动
    private boolean mIsDragging;

    // 内容总高度
    private int mContentHeight = 0;

    // 构造方法
    public ScrollableFlowLayout(Context context) {
        super(context);
        init(context);
    }

    public ScrollableFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollableFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化方法
     * @param context 上下文
     */
    private void init(Context context) {
        // 创建Scroller实例，用于处理平滑滚动
        mScroller = new Scroller(context);
        // 获取系统视图配置
        ViewConfiguration configuration = ViewConfiguration.get(context);
        // 获取系统认为的滑动最小距离
        mTouchSlop = configuration.getScaledTouchSlop();
        // 获取系统认为的最小fling速度
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        // 获取系统认为的最大fling速度
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        // 启用垂直滚动条
        setVerticalScrollBarEnabled(true);
    }

    /**
     * 测量流程
     * 1. 测量所有子View
     * 2. 计算FlowLayout的宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取父容器给出的测量规格
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 记录FlowLayout的实际宽高
        int width = 0;
        int height = 0;
        // 记录当前行的宽高
        int lineWidth = 0;
        int lineHeight = 0;

        // 遍历所有子View
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            // 跳过GONE状态的子View
            if (child.getVisibility() == GONE) {
                continue;
            }

            // 测量子View
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            // 获取子View的LayoutParams（我们使用MarginLayoutParams）
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 计算子View占用的实际空间（包括margin）
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            // 判断是否需要换行
            if (lineWidth + childWidth > widthSize - getPaddingLeft() - getPaddingRight()) {
                // 换行处理：
                // 1. 取当前行和之前行的最大宽度作为总宽度
                width = Math.max(width, lineWidth);
                // 2. 累加行高（加上行间距）
                height += lineHeight + verticalSpacing;
                // 3. 重置新行的宽高
                lineWidth = childWidth;
                lineHeight = childHeight;
            } else {
                // 不换行：
                // 1. 累加行宽（加上水平间距）
                lineWidth += childWidth + horizontalSpacing;
                // 2. 取当前行子View的最大高度
                lineHeight = Math.max(lineHeight, childHeight);
            }

            // 处理最后一个子View
            if (i == childCount - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }

        // 考虑padding对总宽高的影响
        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();

        // 保存内容总高度（用于滚动计算）
        mContentHeight = height;

        // 如果是AT_MOST模式，高度取计算值和给定值的最小值
        if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(height, heightSize);
        }

        // 设置最终测量尺寸
        setMeasuredDimension(
                widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                heightMode == MeasureSpec.EXACTLY ? heightSize : height);
    }

    /**
     * 布局流程
     * 1. 确定每个子View的位置
     * 2. 调用child.layout()布局子View
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 获取可用宽度（减去padding）
        int width = getWidth();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        // 当前布局的起始坐标（考虑padding）
        int childLeft = paddingLeft;
        int childTop = paddingTop;
        // 当前行的高度
        int lineHeight = 0;

        // 遍历所有子View
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            // 获取子View的LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 子View的实际宽高（不包括margin）
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 判断是否需要换行
            if (childLeft + childWidth + lp.leftMargin + lp.rightMargin > width - getPaddingRight()) {
                // 换行处理：
                // 1. 重置到下一行起始位置
                childLeft = paddingLeft;
                // 2. 累加行高（加上垂直间距）
                childTop += lineHeight + verticalSpacing;
                // 3. 重置行高
                lineHeight = 0;
            }

            // 计算子View的四个边界位置（考虑margin）
            int left = childLeft + lp.leftMargin;
            int top = childTop + lp.topMargin;
            int right = left + childWidth;
            int bottom = top + childHeight;

            // 布局子View
            child.layout(left, top, right, bottom);

            // 更新下一个子View的位置
            childLeft += childWidth + lp.leftMargin + lp.rightMargin + horizontalSpacing;
            // 更新行高（取当前行子View的最大高度）
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
        }
    }

    /**
     * 拦截触摸事件
     * 判断是否应该拦截事件自己处理
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        // 如果已经在拖动状态，且是MOVE事件，直接拦截
        if ((action == MotionEvent.ACTION_MOVE) && (mIsDragging)) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 记录按下位置的Y坐标
                mLastY = ev.getY();
                // 如果滚动动画还没结束，认为是拖动状态
                mIsDragging = !mScroller.isFinished();
                break;

            case MotionEvent.ACTION_MOVE:
                final float y = ev.getY();
                final float dy = y - mLastY;
                // 如果移动距离超过系统认为的最小滑动距离，认为是拖动状态
                if (Math.abs(dy) > mTouchSlop) {
                    mIsDragging = true;
                    mLastY = y;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 结束拖动状态
                mIsDragging = false;
                break;
        }

        return mIsDragging;
    }

    /**
     * 处理触摸事件
     * 实现拖动和fling效果
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 初始化速度跟踪器
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        final int action = event.getActionMasked();
        final float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 如果滚动动画还没结束，立即停止
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                // 记录按下位置的Y坐标
                mLastY = y;
                return true;

            case MotionEvent.ACTION_MOVE:
                // 如果还不是拖动状态，但移动距离超过了系统认为的最小滑动距离
                if (!mIsDragging && Math.abs(y - mLastY) > mTouchSlop) {
                    mIsDragging = true;
                    // 调整最后一次的位置，消除触摸误差
                    if (y > mLastY) {
                        mLastY -= mTouchSlop;
                    } else {
                        mLastY += mTouchSlop;
                    }
                }
                // 如果是拖动状态
                if (mIsDragging) {
                    // 计算移动距离
                    final float dy = mLastY - y;
                    mLastY = y;
                    // 执行滚动
                    scrollBy(0, (int) dy);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    // 计算当前速度
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    final float yVelocity = mVelocityTracker.getYVelocity();
                    // 如果速度超过系统认为的最小fling速度，执行fling
                    if (Math.abs(yVelocity) > mMinimumVelocity) {
                        fling(-(int) yVelocity); // 注意取反，因为坐标系方向
                    }
                }
                // 释放速度跟踪器
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mIsDragging = false;
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mIsDragging = false;
                break;
        }

        return true;
    }

    /**
     * 执行fling（惯性滚动）
     * @param velocityY Y方向的速度
     */
    private void fling(int velocityY) {
        // 计算最大滚动范围（考虑paddingBottom）
        int maxScrollY = Math.max(0, mContentHeight - getHeight() + getPaddingBottom());
        // 开始fling
        mScroller.fling(
                getScrollX(),    // 起始X位置
                getScrollY(),    // 起始Y位置
                0,              // X方向速度
                velocityY,      // Y方向速度
                0,              // 最小X位置
                0,              // 最大X位置
                -getPaddingTop(), // 最小Y位置（允许向上滚动到paddingTop）
                maxScrollY      // 最大Y位置
        );
        // 触发重绘
        invalidate();
    }

    /**
     * 限制滚动范围
     */
    @Override
    public void scrollTo(int x, int y) {
        // 计算最大滚动范围
        int maxScrollY = Math.max(0, mContentHeight - getHeight() + getPaddingBottom());
        // 限制Y坐标在有效范围内
        int clampedY = Math.max(-getPaddingTop(), Math.min(y, maxScrollY));
        // 执行滚动
        super.scrollTo(x, clampedY);
    }

    /**
     * 计算滚动动画
     * Scroller会在这里计算新的滚动位置
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // 滚动到Scroller计算的新位置
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 继续请求重绘，直到动画结束
            postInvalidateOnAnimation();
        }
    }

    /**
     * 计算垂直滚动范围（内容总高度）
     */
    @Override
    protected int computeVerticalScrollRange() {
        return mContentHeight + getPaddingTop();
    }

    /**
     * 计算垂直滚动偏移（当前滚动位置）
     */
    @Override
    protected int computeVerticalScrollOffset() {
        return getScrollY() + getPaddingTop();
    }

    /**
     * 计算垂直滚动范围的可视部分高度
     */
    @Override
    protected int computeVerticalScrollExtent() {
        return getHeight();
    }

    /**
     * 滚动位置变化回调
     * 用于唤醒滚动条
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        awakenScrollBars();
    }

    // 以下方法提供对MarginLayoutParams的支持
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    // 设置水平间距
    public void setHorizontalSpacing(int spacing) {
        this.horizontalSpacing = spacing;
        requestLayout();
    }

    // 设置垂直间距
    public void setVerticalSpacing(int spacing) {
        this.verticalSpacing = spacing;
        requestLayout();
    }
}