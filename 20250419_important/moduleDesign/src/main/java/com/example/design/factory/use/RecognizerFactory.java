package com.example.design.factory.use;

// ===== 3. 工厂类封装创建逻辑 =====
public class RecognizerFactory {
    public  SpeechRecognizer createRecognizer(String engineType) {
        switch (engineType) {
            case "Iflytek":
                return new IflytekRecognizer();
            case "Baidu":
                return new BaiduRecognizer();
            // 新增引擎只需扩展此处
            case "Aliyun":
                return new AliyunRecognizer();
            default:
                throw new IllegalArgumentException("未知引擎类型");
        }
    }
}
