package com.example.design.adapter;

import com.example.design.adapter.use.BaiduPlayerAdapter;
import com.example.design.adapter.use.XunfeiPlayerAdapter;

public class PlayerControllerWithoutAdapter {
    public void play(String text, String playerType) {
        if ("baidu".equals(playerType)) {
            // 直接调用百度SDK
            BaiduPlayerAdapter.BaiduSdk baidu = new BaiduPlayerAdapter.BaiduSdk();
            baidu.startPlay(text);
        } else if ("xunfei".equals(playerType)) {
            // 直接调用讯飞SDK
            XunfeiPlayerAdapter.XunfeiSdk xunfei = new XunfeiPlayerAdapter.XunfeiSdk();
            xunfei.playAudio(text);
        }
        // 新增厂商需要修改此处代码
    }
    // 其他方法也需要重复if-else逻辑
}