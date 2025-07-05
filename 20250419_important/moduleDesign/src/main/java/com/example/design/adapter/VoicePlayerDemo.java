package com.example.design.adapter;

import com.example.design.adapter.use.AudioDeviceManager;
import com.example.design.adapter.use.BaiduPlayerAdapter;
import com.example.design.adapter.use.PlayerController;
import com.example.design.adapter.use.XunfeiPlayerAdapter;

// 6. 客户端使用示例
public class VoicePlayerDemo {
    public static void main(String[] args) {
        // 创建控制器
        PlayerController controller = new PlayerController();

        // 创建设备管理器
        AudioDeviceManager deviceManager = new AudioDeviceManager();

        // 使用百度播放器
        System.out.println("\n=== 使用百度播放器 ===");
        controller.setPlayer(new BaiduPlayerAdapter());
        deviceManager.setOutputToSpeaker();
        controller.playText("你好，欢迎使用百度语音服务");
        controller.pause();
        controller.playText("继续播放百度语音");
        controller.stop();

        // 切换到讯飞播放器
        System.out.println("\n=== 切换到讯飞播放器 ===");
        controller.setPlayer(new XunfeiPlayerAdapter());
        deviceManager.setOutputToHeadphones();
        controller.playText("科大讯飞为您服务");
        controller.pause();

        // 释放资源
        controller.release();
    }
}