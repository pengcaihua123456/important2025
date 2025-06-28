package com.evenbus.view.wheel;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * @Author pengcaihua
 * @Date 13:47
 * @describe
 */
public class WheelViewMini extends View {
    // 数据集合
    private List<String> items = Arrays.asList(
            "开心", "难过", "伤心", "开始",
            "接收", "没有", "哎你", "天哪");

    // 画笔
    private Paint textPaint;
    private Paint selectedTextPaint;
    private Paint dividerPaint;

    // 布局参数
    private int itemHeight = 150; // 每项高度
    private int visibleItems = 5; // 可见项数量
    private int selectedItem = 2; // 默认选中第3项

    public WheelViewMini(Context context) {
        this(context, null);
    }

    public WheelViewMini(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        // 普通文本画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 选中文本画笔
        selectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedTextPaint.setColor(Color.BLACK);
        selectedTextPaint.setTextSize(60);
        selectedTextPaint.setTextAlign(Paint.Align.CENTER);

        // 分割线画笔
        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(Color.LTGRAY);
        dividerPaint.setStrokeWidth(4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算View的宽高
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = itemHeight * visibleItems;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 计算中间位置
        int centerY = getHeight() / 2;

        // 1. 绘制所有可见项
        for (int i = -visibleItems/2; i <= visibleItems/2; i++) {
            int itemIndex = selectedItem + i;
            if (itemIndex < 0 || itemIndex >= items.size()) {
                continue; // 跳过超出范围的项
            }

            String text = items.get(itemIndex);
            int yPos = centerY + i * itemHeight;

            // 判断是否是选中项
            Paint paint = (i == 0) ? selectedTextPaint : textPaint;
            canvas.drawText(text, getWidth()/2, yPos, paint);
        }

        // 2. 绘制固定分割线
        int dividerTop = centerY - itemHeight/2;
        int dividerBottom = centerY + itemHeight/2;
        canvas.drawLine(0, dividerTop, getWidth(), dividerTop, dividerPaint);
        canvas.drawLine(0, dividerBottom, getWidth(), dividerBottom, dividerPaint);
    }
}
