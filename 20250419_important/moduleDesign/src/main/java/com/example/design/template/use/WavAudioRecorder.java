package com.example.design.template.use;

import android.util.Log;

// 2. 具体实现类：标准WAV录音器
public class WavAudioRecorder extends AudioRecorderTemplate {
    @Override
    protected void startRecording() {
        // 实现WAV录音逻辑
        Log.d("WavRecorder", "开始录制无损WAV音频...");
    }

    @Override
    protected void stopRecording() {
        Log.d("WavRecorder", "停止WAV录音");
    }
}