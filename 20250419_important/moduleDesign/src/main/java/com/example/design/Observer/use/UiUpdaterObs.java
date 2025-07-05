package com.example.design.Observer.use;

import android.graphics.Color;
import android.widget.TextView;

// UI更新观察者
public class UiUpdaterObs implements VoiceObserverObs {
    private final TextView commandTextView;

    public UiUpdaterObs(TextView textView) {
        this.commandTextView = textView;
    }

    @Override
    public void onVoiceCommandReceived(String command) {
        commandTextView.post(() -> {
            commandTextView.setText("命令: " + command);
            commandTextView.setTextColor(Color.GREEN);
        });
    }
}

