package com.evenbus.view.wheel;


/**
 * @Author pengcaihua
 * @Date 14:50
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

public class CenterWheelView extends View {

    private List<String> items = new ArrayList<>();
    private Paint textPaint, selectedTextPaint, dividerPaint, indexPaint;
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

    public CenterWheelView(Context context) {
        this(context, null);
    }

    public CenterWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 测试数据
        items.add("开心"); items.add("难过"); items.add("伤心");
        items.add("开始"); items.add("接收"); items.add("没有");

        // 初始化画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(45);
        textPaint.setTextAlign(Paint.Align.CENTER);

        selectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedTextPaint.setColor(Color.BLACK);
        selectedTextPaint.setTextSize(55);
        selectedTextPaint.setTextAlign(Paint.Align.CENTER);

        // 序号画笔
        indexPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indexPaint.setColor(Color.BLUE);
        indexPaint.setTextSize(35);
        indexPaint.setTextAlign(Paint.Align.LEFT);

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
        // 绘制可见项
        for (int i = -visibleItems/2; i <= visibleItems/2; i++) {
            int itemIndex = (int)(scrollY / itemHeight) + i;
            if (itemIndex < 0) itemIndex = 0;
            if (itemIndex >= items.size()) itemIndex = items.size() - 1;

            int yPos = centerY + i * itemHeight - (int)(scrollY % itemHeight);

            boolean isSelected = Math.abs(scrollY - itemIndex * itemHeight) < 1;
            Paint textPaint = isSelected ? selectedTextPaint : this.textPaint;

            // 绘制序号+文本
            String displayText = (itemIndex + 1) + ". " + items.get(itemIndex);
            canvas.drawText(displayText, getWidth()/2, yPos, textPaint);
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

                scrollY -= deltaY; // 正确方向：手指上滑，内容上移
                limitScroll();
                updateSelectedItem();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                float yVelocity = velocityTracker.getYVelocity();

                if (Math.abs(yVelocity) > MIN_FLING_VELOCITY) {
                    // 关键修正：惯性方向与手指滑动方向一致
                    scroller.fling(
                            0, (int)scrollY,
                            0, (int)-yVelocity, // 这里需要取反
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
        float maxScroll = (items.size() - 1) * itemHeight;
        scrollY = Math.max(0, Math.min(scrollY, maxScroll));
    }

    private void updateSelectedItem() {
        int newSelected = Math.round(scrollY / itemHeight);
        selectedItem = Math.max(0, Math.min(newSelected, items.size() - 1));
    }

    private void alignToNearestItem() {
        int targetY = selectedItem * itemHeight;
        scroller.startScroll(0, (int)scrollY, 0, targetY - (int)scrollY, ALIGN_DURATION);
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
        return items.get(selectedItem);
    }
}
