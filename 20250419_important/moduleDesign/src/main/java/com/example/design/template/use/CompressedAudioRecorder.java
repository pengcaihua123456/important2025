package com.example.design.template.use;

import android.util.Log;

// 3. 具体实现类：压缩MP3录音器（带额外处理）
public class CompressedAudioRecorder extends AudioRecorderTemplate {
    @Override
    protected void startRecording() {
        // 实现MP3录音逻辑
        Log.d("Mp3Recorder", "开始录制MP3音频...");
    }

    @Override
    protected void stopRecording() {
        Log.d("Mp3Recorder", "停止MP3录音");
    }

    // 覆盖钩子方法：添加音频压缩处理
    @Override
    protected void processAudio() {
        super.processAudio();
        Log.d("Mp3Recorder", "实时压缩音频数据...");
    }

    // 覆盖保存方法
    @Override
    protected void saveRecording() {
        Log.i("Mp3Recorder", "音频已保存为MP3格式");
    }

    // 添加额外资源释放
    @Override
    protected void releaseResources() {
        super.releaseResources();
        Log.d("Mp3Recorder", "释放MP3编码器资源");
    }
}