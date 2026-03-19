package com.taoduoduo.host.core.imp;


import com.tencent.shadow.common.IHostService;

public class HostServiceImpl implements IHostService {


    public HostServiceImpl() {
    }

    @Override
    public String getHostName() {
        return "我是宿主应用 (Host App)";
    }

    @Override
    public void doHostAction(String actionName, Callback callback) {
        // 执行宿主的复杂逻辑
        String result = "宿主已处理动作: " + actionName + ", 时间: " + System.currentTimeMillis();

        // 异步或同步回调
        if (callback != null) {
            callback.onResult(result);
        }
    }

    @Override
    public int getHostEnvironmentCode() {
        return 1001; // 代表正式环境
    }
}