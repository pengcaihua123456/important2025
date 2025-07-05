package com.example.design.proxy.use;

/**
 * @Author pengcaihua
 * @Date 15:07
 * @describe
 */
// 静态代理实现（对比方案）
public class VoiceRecognizerProxy implements IVoiceRecognizer {
    private final GoogleVoiceRecognizer realRecognizer;

    public VoiceRecognizerProxy() {
        this.realRecognizer = new GoogleVoiceRecognizer();
    }

    @Override
    public String recognize(String audioData) {
        if (!NetworkUtils.isConnected()) {
            throw new RuntimeException("Network unavailable");
        }
        System.out.println("METHOD CALL: recognize | ARGS: [" + audioData + "]");
        long start = System.currentTimeMillis();
        String result = realRecognizer.recognize(audioData);
        long duration = System.currentTimeMillis() - start;
        System.out.println("METHOD PERFORMANCE: recognize executed in " + duration + "ms");
        return result;
    }

    @Override
    public void setLanguage(String language) {
        // 需要为每个方法重复代理逻辑
        if (!NetworkUtils.isConnected()) {
            throw new RuntimeException("Network unavailable");
        }
        System.out.println("METHOD CALL: setLanguage | ARGS: [" + language + "]");
        long start = System.currentTimeMillis();
        realRecognizer.setLanguage(language);
        long duration = System.currentTimeMillis() - start;
        System.out.println("METHOD PERFORMANCE: setLanguage executed in " + duration + "ms");
    }
}