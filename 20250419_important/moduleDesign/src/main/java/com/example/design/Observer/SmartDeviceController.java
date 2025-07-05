package com.example.design.Observer;

import android.util.Log;

public class SmartDeviceController {
    // 模拟物联网设备控制
    public void turnOnLights() {
        Log.i("DeviceCtrl", "执行：打开灯光");
        // 实际硬件控制代码（如MQTT/HTTP请求）
    }

    public void turnOffLights() {
        Log.i("DeviceCtrl", "执行：关闭灯光");
    }

    // 扩展方法示例
    public void increaseTemperature() {
        Log.i("DeviceCtrl", "温度升高1℃");
    }
}
