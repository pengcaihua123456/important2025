package com.example.design.Observer;

import android.graphics.Color;
import android.widget.TextView;

public class UIUpdater {
    private final TextView commandTextView; // Android视图组件

    public UIUpdater(TextView textView) {
        this.commandTextView = textView;
    }

    public void updateCommandText(String text) {
        // 确保在主线程更新UI
        commandTextView.post(() -> {
            commandTextView.setText(text);
            commandTextView.setTextColor(Color.GREEN);
        });
    }
}

