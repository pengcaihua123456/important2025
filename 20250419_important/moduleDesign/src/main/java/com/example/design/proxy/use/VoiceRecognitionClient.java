package com.example.design.proxy.use;

// 5. 客户端使用 - 包含错误处理
public class VoiceRecognitionClient {
    public static void main(String[] args) {
        // 创建真实对象
        IVoiceRecognizer realRecognizer = new GoogleVoiceRecognizer();

        // 创建代理处理器
        VoiceProxyHandler handler = new VoiceProxyHandler(realRecognizer);

        // 动态生成代理对象
        IVoiceRecognizer proxy = (IVoiceRecognizer) java.lang.reflect.Proxy.newProxyInstance(
                IVoiceRecognizer.class.getClassLoader(),
                new Class[]{IVoiceRecognizer.class},
                handler
        );

        // 使用代理对象
        try {
            // 设置语言
            proxy.setLanguage("en-US");

            // 模拟多次识别调用
            for (int i = 0; i < 3; i++) {
                String audioData = "audio_sample_" + System.currentTimeMillis();
                String result = proxy.recognize(audioData);
                System.out.println("Recognition Result: " + result);
                Thread.sleep(1000); // 模拟间隔
            }
        } catch (Exception e) {
            System.err.println("Recognition failed: " + e.getMessage());
        }
    }
}
