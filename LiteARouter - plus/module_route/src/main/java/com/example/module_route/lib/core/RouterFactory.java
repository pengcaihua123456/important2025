package com.example.module_route.lib.core;

import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import com.example.module_route.lib.annotations.Body;
import com.example.module_route.lib.annotations.GET;
import com.example.module_route.lib.annotations.POST;
import com.example.module_route.lib.annotations.Query;
import com.google.gson.Gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// RouterFactory.java
public class RouterFactory {
    public static <T> T create(Class<T> service) {
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service},
                new RouterInvocationHandler()
        );
    }

    private static class RouterInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1. 解析注解
            GET getAnnotation = method.getAnnotation(GET.class);
            POST postAnnotation = method.getAnnotation(POST.class);
            String path = getAnnotation != null ? getAnnotation.value() :
                    postAnnotation != null ? postAnnotation.value() :
                            null;

            // 在 RouterInvocationHandler.invoke() 中添加调试代码
            System.out.println("Method: " + method.getName());
            System.out.println("Annotations: " + Arrays.toString(method.getAnnotations()));
            if (getAnnotation == null && postAnnotation == null) {
                System.err.println("⚠️ 该方法未标记任何路由注解!");
            }

            if (path == null) {
                // 添加详细错误日志
                System.err.println("ERROR: 方法 " + method.getName() + " 缺少 @GET 或 @POST 注解");
                throw new IllegalArgumentException("Missing path annotation");
            }

            // 2. 构建Uri
            Uri.Builder uriBuilder = new Uri.Builder()
                    .scheme("app")
                    .authority("router")
                    .path(path);

            // 3. 处理@Query参数
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof Query) {
                        String key = ((Query) annotation).value();
                        String value = args[i] != null ? args[i].toString() : null;
                        if (value != null) {
                            uriBuilder.appendQueryParameter(key, value);
                        }
                    }
                }
            }

            // 4. 处理@Body参数
            if (postAnnotation != null) {
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    for (Annotation annotation : parameterAnnotations[i]) {
                        if (annotation instanceof Body) {
                            String json = new Gson().toJson(args[i]);
                            String encoded = Base64.encodeToString(
                                    json.getBytes(StandardCharsets.UTF_8),
                                    Base64.URL_SAFE
                            );
                            uriBuilder.appendQueryParameter("_data", encoded);
                        }
                    }
                }
            }

            // 5. 构建Intent
            return new Intent(Intent.ACTION_VIEW, uriBuilder.build())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }
}