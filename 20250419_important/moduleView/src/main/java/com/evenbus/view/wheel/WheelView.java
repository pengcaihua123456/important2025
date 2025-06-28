package com.evenbus.view.wheel;


/**
 * @Author pengcaihua
 * @Date 13:19
 * @describe
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class WheelView extends View {
    private List<String> items = new ArrayList<>();
    private Paint textPaint;
    private Paint selectedTextPaint;
    private Paint dividerPaint;
    private int itemHeight = 100; // 每个选项的高度
    private int visibleItems = 5; // 可见的选项数量
    private int selectedItem = 0;
    private float scrollY = 0; // 当前垂直滚动位置
    private Scroller scroller;
    private GestureDetector gestureDetector;
    private OnItemSelectedListener listener;

    public interface OnItemSelectedListener {
        void onItemSelected(int position, String item);
    }

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化文本集合
        items.add("开心");
        items.add("难过");
        items.add("伤心");
        items.add("开始");
        items.add("接收");
        items.add("没有");
        items.add("哎你");
        items.add("天哪");
        items.add("没有");
        items.add("我看");
        items.add("的的");
        items.add("什么");

        // 普通文本画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 选中文本画笔
        selectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedTextPaint.setColor(Color.BLACK);
        selectedTextPaint.setTextSize(50);
        selectedTextPaint.setTextAlign(Paint.Align.CENTER);

        // 分割线画笔
        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(Color.LTGRAY);
        dividerPaint.setStrokeWidth(2);

        scroller = new Scroller(getContext());

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // 禁用水平滚动
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    return false;
                }

                // 关键修正：手指上滑(distanceY为负)，内容上移(scrollY增加)
                scrollY += distanceY;

                // 限制滚动范围
                float maxScrollY = (items.size() - 1) * itemHeight;
                scrollY = Math.max(0, Math.min(scrollY, maxScrollY));

                updateSelectedItem();
                invalidate();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // 关键修正：快速滑动方向与滚动方向一致
                float maxScrollY = (items.size() - 1) * itemHeight;
                scroller.fling(0, (int) scrollY,
                        0, (int) velocityY, // 直接使用velocityY
                        0, 0,
                        0, (int) maxScrollY);
                invalidate();
                return true;
            }
        });
    }

    private void updateSelectedItem() {
        int newSelectedItem = (int) ((scrollY + itemHeight / 2) / itemHeight);
        newSelectedItem = Math.max(0, Math.min(newSelectedItem, items.size() - 1));

        if (newSelectedItem != selectedItem) {
            selectedItem = newSelectedItem;
            if (listener != null) {
                listener.onItemSelected(selectedItem, items.get(selectedItem));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = itemHeight * visibleItems;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算中间位置
        int centerY = getHeight() / 2;

        // 计算当前偏移量相对于选中项的位置
        float relativeOffset = scrollY % itemHeight;

        // 绘制所有可见项
        for (int i = -visibleItems / 2; i <= visibleItems / 2; i++) {
            int itemIndex = (int) (scrollY / itemHeight) + i;
            if (itemIndex < 0 || itemIndex >= items.size()) {
                continue;
            }

            String text = items.get(itemIndex);
            // 计算每个项的位置（关键修正：relativeOffset直接用于位置计算）
            int y = centerY + (i * itemHeight) - (int) relativeOffset;

            // 只绘制可见区域内的项
            if (y >= -itemHeight && y <= getHeight() + itemHeight) {
                Paint paint = (itemIndex == selectedItem) ? selectedTextPaint : textPaint;
                canvas.drawText(text, getWidth() / 2, y, paint);
            }
        }

        // 绘制固定不动的分割线
        canvas.drawLine(0, centerY - itemHeight / 2, getWidth(), centerY - itemHeight / 2, dividerPaint);
        canvas.drawLine(0, centerY + itemHeight / 2, getWidth(), centerY + itemHeight / 2, dividerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            adjustPosition();
        }
        return true;
    }

    private void adjustPosition() {
        // 计算需要对齐的位置
        int targetY = (int) ((scrollY + itemHeight / 2) / itemHeight) * itemHeight;

        scroller.startScroll(0, (int) scrollY, 0, targetY - (int) scrollY, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollY = scroller.getCurrY();
            updateSelectedItem();
            postInvalidate();
        }
    }

    // 其他方法保持不变...
    public void setItems(List<String> items) {
        this.items = items;
        invalidate();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public String getSelectedItemText() {
        if (items.isEmpty() || selectedItem < 0 || selectedItem >= items.size()) {
            return null;
        }
        return items.get(selectedItem);
    }
}

