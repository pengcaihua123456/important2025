package com.evenbus.myapplication.view.flow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 自定义流式布局，实现类似Flexbox的自动换行效果
 *
 * 功能特点：
 * 1. 自动换行：当一行放不下时会自动换到下一行
 * 2. 支持间距：可设置水平和垂直间距
 * 3. 支持margin：子View的margin会被考虑在内
 * 4. 自适应大小：根据内容自动调整宽高
 */
public class FlowLayout extends ViewGroup {

    // 默认间距值
    private int horizontalSpacing = 16; // 水平间距(dp)
    private int verticalSpacing = 16;   // 垂直间距(dp)

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 测量流程：
     * 1. 遍历所有子View，测量每个子View的尺寸
     * 2. 计算每行的宽度和高度
     * 3. 当一行放不下时换行，并累加高度
     * 4. 最终确定FlowLayout的总宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取父容器给出的测量规格和模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 记录FlowLayout的实际宽高
        int totalWidth = 0;   // 最终宽度
        int totalHeight = 0;   // 最终高度

        // 当前行的宽高
        int lineWidth = 0;     // 当前行已使用的宽度
        int lineHeight = 0;    // 当前行的高度

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            // 跳过GONE状态的子View
            if (child.getVisibility() == GONE) {
                continue;
            }

            // 测量子View（这会调用child的onMeasure）
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            // 获取子View的LayoutParams（我们使用MarginLayoutParams）
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            // 计算子View占用的实际空间（包括margin）
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            // 判断是否需要换行（考虑padding）
            if (lineWidth + childWidth > widthSize - getPaddingLeft() - getPaddingRight()) {
                // 换行处理：
                // 1. 取当前行和之前行的最大宽度作为总宽度
                totalWidth = Math.max(totalWidth, lineWidth);
                // 2. 累加行高（加上行间距）
                totalHeight += lineHeight + verticalSpacing;
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
                totalWidth = Math.max(totalWidth, lineWidth);
                totalHeight += lineHeight;
            }
        }

        // 考虑padding对总宽高的影响
        totalWidth += getPaddingLeft() + getPaddingRight();
        totalHeight += getPaddingTop() + getPaddingBottom();

        // 根据测量模式确定最终尺寸：
        // EXACTLY模式：使用父容器给出的精确尺寸
        // AT_MOST/UNSPECIFIED：使用计算出的尺寸
        setMeasuredDimension(
                widthMode == MeasureSpec.EXACTLY ? widthSize : totalWidth,
                heightMode == MeasureSpec.EXACTLY ? heightSize : totalHeight);
    }

    /**
     * 布局流程：
     * 1. 遍历所有子View
     * 2. 计算每个子View的位置
     * 3. 当一行放不下时换行
     * 4. 调用child.layout()确定子View的最终位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 获取可用宽度（减去padding）
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();

        // 当前布局的起始坐标（考虑padding）
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();

        // 当前行的高度
        int lineHeight = 0;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            // 子View的实际宽高（不包括margin）
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 判断是否需要换行
            if (childLeft + childWidth + lp.leftMargin + lp.rightMargin >
                    getWidth() - getPaddingRight()) {
                // 换行处理：
                // 1. 重置到下一行起始位置
                childLeft = getPaddingLeft();
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

    // 设置间距方法

    public void setHorizontalSpacing(int spacing) {
        this.horizontalSpacing = spacing;
        requestLayout(); // 间距改变需要重新布局
    }

    public void setVerticalSpacing(int spacing) {
        this.verticalSpacing = spacing;
        requestLayout(); // 间距改变需要重新布局
    }
}
