package com.evenbus.myapplication.view.wheel;

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
import java.util.Arrays;
import java.util.List;


public class LoopWheelView extends View {
    private List<String> items = new ArrayList<>();
    private Paint textPaint, selectedTextPaint, dividerPaint;
    private int itemHeight = 150;
    private int visibleItems = 5;
    private int selectedItem = 0;
    private boolean isLoopEnabled = true;

    // 滚动控制
    private Scroller scroller;
    private VelocityTracker velocityTracker;
    private float lastTouchY;
    private float scrollY = 0;
    private static final int MIN_FLING_VELOCITY = 800;
    private static final int ALIGN_DURATION = 300;
    private boolean isAligning = false;

    // 修复慢速滑动问题
    private boolean isDragging = false;

    public LoopWheelView(Context context) {
        this(context, null);
    }

    public LoopWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setItems(Arrays.asList("1. 开心", "2. 难过", "3. 伤心", "4. 开始", "5. 接收"));

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
    }

    private boolean isUse3D=true;

    @Override
    protected void onDraw(Canvas canvas) {
        float centerY = getHeight() / 2f;

        // 绘制可见项
        for (int i = -visibleItems/2 - 2; i <= visibleItems/2 + 2; i++) {
            int virtualIndex = (int)(scrollY / itemHeight) + i;
            int realIndex = getRealIndex(virtualIndex);

            float yPos = centerY + i * itemHeight - (scrollY % itemHeight);
            if (yPos < -itemHeight || yPos > getHeight() + itemHeight) continue;

            boolean isSelected = realIndex == selectedItem;
            if(!isUse3D){
                canvas.drawText(items.get(realIndex), getWidth()/2, yPos,
                        isSelected ? selectedTextPaint : textPaint);
                return;
            }
//            // 3D效果
            float distance = Math.abs(i - (scrollY % itemHeight)/itemHeight);
            float scale = 1.0f - 0.2f * distance;
            float alpha = 1.0f - 0.5f * distance;
            canvas.save();
            canvas.translate(getWidth()/2f, yPos);
            canvas.scale(scale, scale);

            textPaint.setAlpha((int)(alpha * 255));
            selectedTextPaint.setAlpha((int)(alpha * 255));

            canvas.drawText(items.get(realIndex), 0, 0, isSelected ? selectedTextPaint : textPaint);
            canvas.restore();
        }

        // 绘制分割线
        float dividerTop = centerY - itemHeight / 2f;
        float dividerBottom = centerY + itemHeight / 2f;
        canvas.drawLine(0, dividerTop, getWidth(), dividerTop, dividerPaint);
        canvas.drawLine(0, dividerBottom, getWidth(), dividerBottom, dividerPaint);
    }

    // 虚拟索引转实际索引
    private int getRealIndex(int virtualIndex) {
        if (items.isEmpty()) return 0;
        int size = items.size();
        return (virtualIndex % size + size) % size;
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
                isDragging = true; // 标记开始拖动
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isDragging) break;

                float deltaY = lastTouchY - event.getY(); // 修复方向问题
                lastTouchY = event.getY();

                scrollY += deltaY; // 修复方向问题
                selectedItem = getRealIndex(Math.round(scrollY / itemHeight));
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                velocityTracker.computeCurrentVelocity(1000);
                float yVelocity = velocityTracker.getYVelocity();

                if (Math.abs(yVelocity) > MIN_FLING_VELOCITY) {
                    // 惯性滚动
                    scroller.fling(0, (int)scrollY, 0, (int)-yVelocity,
                            0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                } else {
                    // 对齐到最近项
                    alignToNearestItem();
                }
                recycleVelocityTracker();
                invalidate();
                break;
        }
        return true;
    }

    private void alignToNearestItem() {
        isAligning = true;
        int targetVirtualIndex = Math.round(scrollY / itemHeight);
        float targetY = targetVirtualIndex * itemHeight;

        // 直接设置位置避免微小偏移
        if (Math.abs(scrollY - targetY) < 1) {
            scrollY = targetY;
            selectedItem = getRealIndex(targetVirtualIndex);
            invalidate();
        } else {
            scroller.startScroll(0, (int)scrollY, 0, (int)(targetY - scrollY), ALIGN_DURATION);
            selectedItem = getRealIndex(targetVirtualIndex);
            invalidate();
        }
        isAligning = false;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollY = scroller.getCurrY();
            selectedItem = getRealIndex(Math.round(scrollY / itemHeight));
            invalidate();
        } else {
            // 滚动结束后强制对齐
            if (!isAligning && Math.abs(scrollY % itemHeight) > 0.1f) {
                alignToNearestItem();
            }
        }
    }

    public void setItems(List<String> items) {
        this.items = new ArrayList<>(items);
        selectedItem = 0;
        scrollY = 0;
        invalidate();
    }

    public String getSelectedItem() {
        return items.isEmpty() ? null : items.get(selectedItem);
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    public void setItemHeight(int height) {
        this.itemHeight = height;
        requestLayout();
    }

    public void setVisibleItems(int count) {
        this.visibleItems = count;
        requestLayout();
    }
}