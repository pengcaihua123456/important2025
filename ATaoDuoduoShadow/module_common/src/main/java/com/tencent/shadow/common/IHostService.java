package com.tencent.shadow.common;


/**
 * 宿主向插件暴露的能力接口
 * 放在共享模块中，确保 Host 和 Plugin 编译时都能看到
 */
public interface IHostService {

    // 示例：获取宿主的应用名称
    String getHostName();

    // 示例：执行宿主的某个业务逻辑
    void doHostAction(String actionName, Callback callback);

    // 示例：获取宿主的环境信息
    int getHostEnvironmentCode();

    // 简单的回调接口
    interface Callback {
        void onResult(String result);
    }
}