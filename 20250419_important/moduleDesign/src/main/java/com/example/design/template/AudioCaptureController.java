package com.example.design.template;

import com.example.module_design.R;

// 使用示例 - 录音控制器
public class AudioCaptureController {
    private SimpleWavRecorder wavRecorder;
    private CompactMp3Recorder mp3Recorder;

    public void init() {
        wavRecorder = new SimpleWavRecorder();
        mp3Recorder = new CompactMp3Recorder();
    }

    public void startWavRecording() {
        new Thread(() -> {
            wavRecorder.startRecordingSession();
        }).start();
    }

    public void startMp3Recording() {
        new Thread(() -> {
            mp3Recorder.executeRecording();
        }).start();
    }

    // Activity中使用
    public void setupInActivity(RecordingActivity activity) {
        activity.findViewById(R.id.tv_page_name).setOnClickListener(v -> {
            startWavRecording();
        });

        activity.findViewById(R.id.tv_page_name).setOnClickListener(v -> {
            startMp3Recording();
        });
    }
}
