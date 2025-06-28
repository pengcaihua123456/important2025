package com.evenbus.view.wheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class PerfectWheelView extends View {
    private List<String> items = new ArrayList<>();
    private Paint textPaint, selectedTextPaint, dividerPaint;
    private int itemHeight = 150;
    private int visibleItems = 5;
    private int selectedItem = 0;

    // 滚动控制
    private Scroller scroller;
    private VelocityTracker velocityTracker;
    private float lastTouchY;
    private float scrollY = 0;
    private static final int MIN_FLING_VELOCITY = 800;
    private static final int ALIGN_DURATION = 300;

    // 居中控制
    private int centerY;
    private int dividerTop, dividerBottom;
    private boolean isCyclic = true; // 是否循环滚动

    public PerfectWheelView(Context context) {
        this(context, null);
    }

    public PerfectWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 初始化画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(45);
        textPaint.setTextAlign(Paint.Align.CENTER);

        selectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedTextPaint.setColor(Color.BLACK);
        selectedTextPaint.setTextSize(55);
        selectedTextPaint.setTextAlign(Paint.Align.CENTER);

        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(0xFFDDDDDD);
        dividerPaint.setStrokeWidth(4);

        scroller = new Scroller(getContext());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = itemHeight * visibleItems;
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), height);

        centerY = height / 2;
        dividerTop = centerY - itemHeight / 2;
        dividerBottom = centerY + itemHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 1. 计算当前基准位置
        int baseIndex = (int)(scrollY / itemHeight);
        float offset = scrollY % itemHeight;

        // 2. 绘制可见项（上下各多绘制1个item防止空白）
        for (int i = -visibleItems/2 - 1; i <= visibleItems/2 + 1; i++) {
            int itemIndex = baseIndex + i;
            String text = getItemText(itemIndex);

            // 计算Y坐标（核心修正：使用精确的浮点计算）
            float yPos = centerY + (i * itemHeight) - offset;

            // 只绘制可见区域内的项
            if (yPos < -itemHeight || yPos > getHeight() + itemHeight) {
                continue;
            }

            // 判断是否是选中项（精确到1像素内）
            boolean isSelected = Math.abs(yPos - centerY) < 1;
            canvas.drawText(text, getWidth()/2, yPos,
                    isSelected ? selectedTextPaint : textPaint);
        }

        // 3. 绘制固定分割线
        canvas.drawLine(0, dividerTop, getWidth(), dividerTop, dividerPaint);
        canvas.drawLine(0, dividerBottom, getWidth(), dividerBottom, dividerPaint);
    }

    private String getItemText(int index) {
        if (items.isEmpty()) return "";

        // 循环模式处理
        if (isCyclic) {
            index = index % items.size();
            if (index < 0) index += items.size();
            return items.get(index);
        }

        // 非循环模式处理
        if (index < 0) return items.get(0);
        if (index >= items.size()) return items.get(items.size() - 1);
        return items.get(index);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (items.isEmpty()) return true;

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                lastTouchY = event.getY();
                removeCallbacks(flingEndChecker);
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaY = event.getY() - lastTouchY;
                lastTouchY = event.getY();

                // 核心修正：使用精确的滚动计算
                scrollY -= deltaY;
                limitScroll();
                updateSelectedItem();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                float yVelocity = velocityTracker.getYVelocity();

                if (Math.abs(yVelocity) > MIN_FLING_VELOCITY) {
                    // 根据是否循环设置不同滚动范围
                    if (isCyclic) {
                        scroller.fling(
                                0, (int)scrollY,
                                0, (int)-yVelocity,
                                0, 0,
                                Integer.MIN_VALUE, Integer.MAX_VALUE
                        );
                    } else {
                        scroller.fling(
                                0, (int)scrollY,
                                0, (int)-yVelocity,
                                0, 0,
                                0, (items.size() - 1) * itemHeight
                        );
                    }
                    post(flingEndChecker);
                } else {
                    alignToNearestItem();
                }

                recycleVelocityTracker();
                invalidate();
                break;
        }
        return true;
    }

    private void limitScroll() {
        if (!isCyclic) {
            // 非循环模式限制边界
            float maxScroll = (items.size() - 1) * itemHeight;
            scrollY = Math.max(0, Math.min(scrollY, maxScroll));
        }
        // 循环模式不需要限制
    }

    private void updateSelectedItem() {
        // 精确计算选中项（四舍五入）
        int newSelected = Math.round(scrollY / itemHeight);

        if (isCyclic) {
            newSelected = newSelected % items.size();
            if (newSelected < 0) newSelected += items.size();
        } else {
            newSelected = Math.max(0, Math.min(newSelected, items.size() - 1));
        }

        if (newSelected != selectedItem) {
            selectedItem = newSelected;
        }
    }

    private void alignToNearestItem() {
        int targetItem = Math.round(scrollY / itemHeight);

        if (isCyclic) {
            // 循环模式：直接对齐，不需要调整
            int targetY = targetItem * itemHeight;
            scroller.startScroll(0, (int)scrollY, 0, targetY - (int)scrollY, ALIGN_DURATION);
        } else {
            // 非循环模式：限制边界
            targetItem = Math.max(0, Math.min(targetItem, items.size() - 1));
            int targetY = targetItem * itemHeight;
            scroller.startScroll(0, (int)scrollY, 0, targetY - (int)scrollY, ALIGN_DURATION);
        }

        selectedItem = targetItem;
        invalidate();
    }

    private Runnable flingEndChecker = new Runnable() {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                scrollY = scroller.getCurrY();
                updateSelectedItem();
                postDelayed(this, 16);
                invalidate();
            } else {
                alignToNearestItem();
            }
        }
    };

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollY = scroller.getCurrY();
            updateSelectedItem();
            invalidate();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    public void setItems(List<String> items) {
        this.items = items;
        this.scrollY = 0;
        this.selectedItem = 0;
        invalidate();
    }

    public void setCyclic(boolean cyclic) {
        isCyclic = cyclic;
        invalidate();
    }

    public String getSelectedItem() {
        if (items.isEmpty() || selectedItem < 0 || selectedItem >= items.size()) {
            return null;
        }
        return items.get(selectedItem);
    }
}