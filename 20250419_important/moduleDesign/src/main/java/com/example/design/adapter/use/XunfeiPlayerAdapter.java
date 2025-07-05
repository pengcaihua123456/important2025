package com.example.design.adapter.use;

// 3. 讯飞播放器适配器
public class XunfeiPlayerAdapter implements AudioPlayer {
    // 模拟讯飞SDK
    public static class XunfeiSdk {
        public void playAudio(String text) {
            System.out.println("[讯飞] 正在播放: " + text);
        }

        public void pauseAudio() {
            System.out.println("[讯飞] 暂停播放");
        }

        public void stopAudio() {
            System.out.println("[讯飞] 停止播放");
        }

        public void destroy() {
            System.out.println("[讯飞] 资源已销毁");
        }
    }

    private final XunfeiSdk xunfeiPlayer;

    public XunfeiPlayerAdapter() {
        this.xunfeiPlayer = new XunfeiSdk();
        System.out.println("讯飞播放器适配器初始化完成");
    }

    @Override
    public void play(String text) {
        // 适配器将统一接口转换为讯飞SDK特定调用
        xunfeiPlayer.playAudio(text);
    }

    @Override
    public void pause() {
        xunfeiPlayer.pauseAudio();
    }

    @Override
    public void stop() {
        xunfeiPlayer.stopAudio();
    }

    @Override
    public void release() {
        xunfeiPlayer.destroy();
    }
}