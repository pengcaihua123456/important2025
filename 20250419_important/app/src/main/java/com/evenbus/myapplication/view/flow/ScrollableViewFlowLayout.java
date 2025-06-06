package com.evenbus.myapplication.view.flow;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.widget.TextView;

public class ScrollableViewFlowLayout extends ScrollView {
    private FlowLayout flowLayout;
    private int horizontalSpacing = 16;
    private int verticalSpacing = 16;

    public ScrollableViewFlowLayout(Context context) {
        super(context);
        init();
    }

    public ScrollableViewFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollableViewFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 禁用ScrollView的滚动条
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        // 创建内部的FlowLayout
        flowLayout = new FlowLayout(getContext());
        flowLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(flowLayout);
    }

    public void setHorizontalSpacing(int spacing) {
        this.horizontalSpacing = spacing;
        flowLayout.setHorizontalSpacing(spacing);
    }

    public void setVerticalSpacing(int spacing) {
        this.verticalSpacing = spacing;
        flowLayout.setVerticalSpacing(spacing);
    }

    public void addTag(String tag, int color) {
        TextView textView = new TextView(getContext());
        textView.setText(tag);
        textView.setBackgroundColor(color);
        textView.setPadding(24, 12, 24, 12);
        textView.setTextColor(Color.WHITE);

        // 设置margin
        MarginLayoutParams params = new MarginLayoutParams(
                MarginLayoutParams.WRAP_CONTENT,
                MarginLayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 4, 4, 4);
        textView.setLayoutParams(params);

        flowLayout.addView(textView);
    }

    public void clearTags() {
        flowLayout.removeAllViews();
    }
}
