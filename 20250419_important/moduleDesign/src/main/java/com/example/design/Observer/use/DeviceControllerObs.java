package com.example.design.Observer.use;

import com.example.design.Observer.SmartDeviceController;

// 设备控制观察者
public class DeviceControllerObs implements VoiceObserverObs {
    private final SmartDeviceController device; // 未改名

    public DeviceControllerObs() {
        this.device = new SmartDeviceController();
    }

    @Override
    public void onVoiceCommandReceived(String command) {
        if ("打开灯光".equals(command)) {
            device.turnOnLights();
        } else if ("关闭灯光".equals(command)) {
            device.turnOffLights();
        }
    }
}

