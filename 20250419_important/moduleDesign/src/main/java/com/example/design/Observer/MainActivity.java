package com.example.design.Observer;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.design.Observer.use.CloudSyncerObs;
import com.example.design.Observer.use.DatabaseLoggerObs;
import com.example.design.Observer.use.DeviceControllerObs;
import com.example.design.Observer.use.UiUpdaterObs;
import com.example.design.Observer.use.VoiceRecognizerObs;
import com.example.module_design.R;

public class MainActivity extends AppCompatActivity {

    private VoiceRecognizerObs voiceRecognizer;
    private DatabaseLoggerObs dbLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView resultView = findViewById(R.id.tv_page_name);

        // 创建被观察者（Obs版本）
        voiceRecognizer = new VoiceRecognizerObs();

        // 创建并注册观察者（Obs版本）
        voiceRecognizer.registerObserver(new UiUpdaterObs(resultView));
        voiceRecognizer.registerObserver(new DatabaseLoggerObs(this));
        voiceRecognizer.registerObserver(new DeviceControllerObs());
        voiceRecognizer.registerObserver(new CloudSyncerObs());

        // 模拟语音识别
        simulateVoiceRecognition();
    }

    private void simulateVoiceRecognition() {
        new Handler().postDelayed(() -> {
            // 发送命令1
            voiceRecognizer.onVoiceCommandReceived("打开灯光");

            // 2秒后发送命令2
            new Handler().postDelayed(() -> {
                voiceRecognizer.onVoiceCommandReceived("关闭灯光");
            }, 2000);
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        if (dbLogger != null) {
            dbLogger.closeDatabase();
        }
    }
}

