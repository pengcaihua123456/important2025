package com.example.design.template;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.design.template.use.AudioRecorderTemplate;
import com.example.design.template.use.CompressedAudioRecorder;
import com.example.design.template.use.WavAudioRecorder;
import com.example.module_design.R;

// 4. 使用示例（在Activity中）
public class RecordingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView btnRecordWav = findViewById(R.id.tv_page_name);
        TextView btnRecordMp3 = findViewById(R.id.btn_go_cart);

        btnRecordWav.setOnClickListener(v -> {
            AudioRecorderTemplate recorder = new WavAudioRecorder();
            recorder.recordAudio();  // 执行模板方法
        });

        btnRecordMp3.setOnClickListener(v -> {
            AudioRecorderTemplate recorder = new CompressedAudioRecorder();
            recorder.recordAudio();  // 执行模板方法
        });
    }
}