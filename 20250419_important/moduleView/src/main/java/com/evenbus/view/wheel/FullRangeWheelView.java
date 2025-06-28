package com.evenbus.view.wheel;


/**
 * @Author pengcaihua
 * @Date 17:05
 * @describe
 */
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

public class FullRangeWheelView extends View {
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
    private int paddingItems = 2; // 上下额外绘制的item数量

    public FullRangeWheelView(Context context) {
        this(context, null);
    }

    public FullRangeWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 测试数据
        items.add("1. 开心"); items.add("2. 难过"); items.add("3. 伤心");
        items.add("4. 开始"); items.add("5. 接收"); items.add("6. 没有");

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
        // 绘制可见项（增加paddingItems确保边界item可选中）
        for (int i = -visibleItems/2 - paddingItems; i <= visibleItems/2 + paddingItems; i++) {
            int itemIndex = (int)(scrollY / itemHeight) + i;

            // 边界检查
            if (itemIndex < 0 || itemIndex >= items.size()) continue;

            int yPos = centerY + i * itemHeight - (int)(scrollY % itemHeight);

            // 只绘制可见区域内的项
            if (yPos < -itemHeight || yPos > getHeight() + itemHeight) continue;

            boolean isSelected = Math.abs(scrollY - itemIndex * itemHeight) < 1;
            canvas.drawText(items.get(itemIndex), getWidth()/2, yPos,
                    isSelected ? selectedTextPaint : textPaint);
        }

        // 绘制分割线
        canvas.drawLine(0, dividerTop, getWidth(), dividerTop, dividerPaint);
        canvas.drawLine(0, dividerBottom, getWidth(), dividerBottom, dividerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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

                scrollY -= deltaY; // 手指上滑，内容上移
                limitScroll();
                updateSelectedItem();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                float yVelocity = velocityTracker.getYVelocity();

                if (Math.abs(yVelocity) > MIN_FLING_VELOCITY) {
                    scroller.fling(
                            0, (int)scrollY,
                            0, (int)-yVelocity,
                            0, 0,
                            0, (items.size() - 1) * itemHeight
                    );
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
        // 允许滚动到边界item也能居中
        float maxScroll = (items.size() - 1) * itemHeight;
        scrollY = Math.max(0, Math.min(scrollY, maxScroll));
    }

    private void updateSelectedItem() {
        int newSelected = Math.round(scrollY / itemHeight);
        selectedItem = Math.max(0, Math.min(newSelected, items.size() - 1));
    }

    private void alignToNearestItem() {
        // 确保所有item（包括第一个和最后一个）都能居中
        int targetItem = Math.round(scrollY / itemHeight);
        targetItem = Math.max(0, Math.min(targetItem, items.size() - 1));

        int targetY = targetItem * itemHeight;
        scroller.startScroll(0, (int)scrollY, 0, targetY - (int)scrollY, ALIGN_DURATION);

        selectedItem = targetItem;
        invalidate();
    }

    private Runnable flingEndChecker = new Runnable() {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                scrollY = scroller.getCurrY();
                limitScroll();
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
            limitScroll();
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
        scrollY = 0;
        selectedItem = 0;
        invalidate();
    }

    public String getSelectedItem() {
        if (selectedItem >= 0 && selectedItem < items.size()) {
            return items.get(selectedItem);
        }
        return null;
    }
}
