package com.example.design.proxy.use;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

public class VoiceProxyHandler implements InvocationHandler {
    private final Object target;
    private long lastNetworkCheck = 0;
    private static final long NETWORK_CHECK_INTERVAL = 5000; // 5秒缓存

    public VoiceProxyHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 前置处理：网络状态检查（带缓存机制）
        if (System.currentTimeMillis() - lastNetworkCheck > NETWORK_CHECK_INTERVAL) {
            if (!checkNetwork()) {
                throw new RuntimeException("Network unavailable");
            }
            lastNetworkCheck = System.currentTimeMillis();
        }

        // 方法调用日志
        logMethodCall(method, args);

        // 记录方法开始时间
        long startTime = System.currentTimeMillis();

        // 执行原始方法
        Object result = method.invoke(target, args);

        // 后置处理：性能监控
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("METHOD PERFORMANCE: " + method.getName() +
                " executed in " + duration + "ms");

        return result;
    }

    private boolean checkNetwork() {
        // 模拟网络检查（实际项目中替换为真实网络检查）
        boolean isConnected = Math.random() > 0.2; // 80%成功率
        System.out.println("NETWORK CHECK: " + (isConnected ? "Connected ✓" : "Disconnected ✗"));
        return isConnected;
    }

    private void logMethodCall(Method method, Object[] args) {
        String logMsg = "METHOD CALL: " + method.getName();
        if (args != null && args.length > 0) {
            logMsg += " | ARGS: " + Arrays.toString(args);
        }
        System.out.println(logMsg);
    }
}