package com.example.design.template.use;

import android.util.Log;

// 1. 抽象基类：定义录音流程模板
public abstract class AudioRecorderTemplate {
    // 模板方法：定义录音流程（final 防止子类覆盖）
    public final void recordAudio() {
        prepareRecording();  // 1. 准备录音
        startRecording();    // 2. 开始录音（子类实现）
        processAudio();      // 3. 处理音频（可选钩子）
        stopRecording();     // 4. 停止录音（子类实现）
        saveRecording();     // 5. 保存录音（默认实现）
        releaseResources(); // 6. 释放资源（可选钩子）
    }

    // 具体步骤实现 ===================================
    private void prepareRecording() {
        // 公共准备逻辑
        Log.i("AudioRecorder", "初始化录音环境...");
        checkMicrophonePermission();
        createTempFile();
    }

    // 抽象方法：必须由子类实现 ========================
    protected abstract void startRecording();  // 开始录音
    protected abstract void stopRecording();   // 停止录音

    // 钩子方法：子类可选择覆盖 ========================
    protected void processAudio() {
        // 默认空实现（子类可扩展）
    }

    protected void releaseResources() {
        // 默认资源释放
        Log.i("AudioRecorder", "释放默认资源");
    }

    // 公共方法 ======================================
    private void checkMicrophonePermission() {
        Log.i("AudioRecorder", "检查麦克风权限...");
    }

    private void createTempFile() {
        Log.i("AudioRecorder", "创建临时音频文件");
    }

    protected void saveRecording() {
        // 默认保存逻辑
        Log.i("AudioRecorder", "音频已保存为WAV格式");
    }
}