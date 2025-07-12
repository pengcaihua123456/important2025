package com.example.design.compose;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class VoiceServiceProxy implements InvocationHandler {
    private final Object target;

    public VoiceServiceProxy(Object target) {
        this.target = target;
    }

    public static IVoiceService createProxy() {
        return (IVoiceService) Proxy.newProxyInstance(
                VoiceServiceProxy.class.getClassLoader(),
                new Class[]{IVoiceService.class},
                new VoiceServiceProxy(new RealVoiceService()));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("==> Calling method: " + method.getName());
        long start = System.currentTimeMillis();
        Object result = method.invoke(target, args);
        long duration = System.currentTimeMillis() - start;
        System.out.println("<== Method " + method.getName() + " executed in " + duration + "ms");
        return result;
    }
}
