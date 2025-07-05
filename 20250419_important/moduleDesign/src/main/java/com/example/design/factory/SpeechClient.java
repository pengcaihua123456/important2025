package com.example.design.factory;


import com.example.design.factory.use.RecognizerFactory;
import com.example.design.factory.use.SpeechRecognizer;

// 客户端代码直接依赖具体实现类
public class SpeechClient {

    // ==== 使用示例 ====
    public static void main(String[] args) {
        SpeechClient client = new SpeechClient();
        byte[] audioData = null;

        // 调用讯飞（配置参数可抽离到配置文件）
        client.recognize("Iflytek", "APP_ID=123456", audioData);

        // 调用百度（客户端代码无需修改）
        client.recognize("Baidu", "API_KEY=7890abc", audioData);
    }

    public void recognize(String engineType, byte[] audioData) {
        if ("Iflytek".equals(engineType)) {
            // 直接实例化讯飞引擎
            IflytekRecognizerWithout iflytek = new IflytekRecognizerWithout();
            iflytek.setAppId("your_iflytek_id");
            iflytek.connectServer();
            String result = iflytek.recognize(audioData);
            System.out.println("讯飞结果: " + result);

        } else if ("Baidu".equals(engineType)) {
            // 直接实例化百度引擎
            BaiduRecognizerWithout baidu = new BaiduRecognizerWithout();
            baidu.setApiKey("your_baidu_key");
            baidu.authToken();
            String result = baidu.processAudio(audioData);
            System.out.println("百度结果: " + result);

        } else {
            throw new IllegalArgumentException("不支持的引擎");
        }
    }

    private final RecognizerFactory factory = new RecognizerFactory();

    public void recognize(String engineType, String config, byte[] audioData) {
        // 通过工厂创建实例（不感知具体类）
        SpeechRecognizer recognizer = factory.createRecognizer(engineType);

        // 统一接口操作
        recognizer.initialize(config);
        String result = recognizer.recognize(audioData);

        System.out.println(engineType + "结果: " + result);
    }
}

