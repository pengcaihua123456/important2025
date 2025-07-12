package com.example.design.compose;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.design.compose.core.VoiceConfigManager;
import com.example.design.compose.facade.VoiceAssistantFacade;
import com.example.design.compose.observer.RecognitionListener;
import com.example.design.compose.processing.AccurateRecognition;
import com.example.design.compose.processing.FastRecognition;
import com.example.design.compose.processing.VoiceRecognitionContext;
import com.example.module_design.R;

public class DesignActivity extends AppCompatActivity implements RecognitionListener {

    private VoiceAssistantFacade voiceAssistant;
    private TextView statusView;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design);

        statusView = findViewById(R.id.status_view);
        resultView = findViewById(R.id.result_view);

        // 初始化语音助手
        voiceAssistant = new VoiceAssistantFacade();
        voiceAssistant.addRecognitionListener(this);

        // 设置配置
        VoiceConfigManager.getInstance().setLanguage("zh-CN");

        // 设置策略
        Button fastBtn = findViewById(R.id.fast_btn);
        Button accurateBtn = findViewById(R.id.accurate_btn);

        fastBtn.setOnClickListener(v -> {
            VoiceRecognitionContext context = new VoiceRecognitionContext();
            context.setStrategy(new FastRecognition());
            updateStatus("Fast recognition strategy set");
        });

        accurateBtn.setOnClickListener(v -> {
            VoiceRecognitionContext context = new VoiceRecognitionContext();
            context.setStrategy(new AccurateRecognition());
            updateStatus("Accurate recognition strategy set");
        });

        // 开始监听
        Button startBtn = findViewById(R.id.start_btn);
        startBtn.setOnClickListener(v -> voiceAssistant.startListening());

        // 语音合成
        Button speakBtn = findViewById(R.id.speak_btn);
        speakBtn.setOnClickListener(v -> {
            String response = voiceAssistant.synthesize("你好，我是语音助手");
            resultView.setText(response);
        });
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> statusView.setText(message));
    }

    @Override
    public void onRecognitionResult(String result) {
        runOnUiThread(() -> {
            resultView.setText(result);
            statusView.setText("Recognition complete");
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            resultView.setText("Error: " + error);
            statusView.setText("Recognition failed");
        });
    }
}
