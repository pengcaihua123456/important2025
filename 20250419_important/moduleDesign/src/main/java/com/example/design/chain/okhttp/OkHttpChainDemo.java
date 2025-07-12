package com.example.design.chain.okhttp;

/*
 * @Author pengcaihua
 * @Date 17:24
 * @describe
 */
import java.util.ArrayList;
import java.util.List;

public class OkHttpChainDemo {

    public static void main(String[] args) throws Exception {
        // 创建拦截器链
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(new RetryAndFollowUpInterceptor());
        interceptors.add(new BridgeInterceptor());
        interceptors.add(new CacheInterceptor());
        interceptors.add(new ConnectInterceptor());
        interceptors.add(new CallServerInterceptor());

        // 创建初始请求
        Request request = new Request("https://api.example.com/data");

        // 创建责任链并处理请求
        RealInterceptorChain chain = new RealInterceptorChain(interceptors, 0, request);
        Response response = chain.proceed(request);

        System.out.println("\n最终响应: " + response);
    }

    // 请求类
    static class Request {
        final String url;
        String headers = "";

        Request(String url) {
            this.url = url;
            System.out.println("创建请求: " + url);
        }

        void addHeader(String header) {
            headers += header + "; ";
        }

        @Override
        public String toString() {
            return "Request{url='" + url + "', headers=" + headers + "}";
        }
    }

    // 响应类
    static class Response {
        final Request request;
        String body;
        String headers = "";

        Response(Request request) {
            this.request = request;
        }

        void addHeader(String header) {
            headers += header + "; ";
        }

        void setBody(String body) {
            this.body = body;
        }

        @Override
        public String toString() {
            return "Response{request=" + request + ", headers=" + headers + ", body='" + body + "'}";
        }
    }

    // 拦截器接口
    interface Interceptor {
        Response intercept(Chain chain) throws Exception;
    }

    // 链接口
    interface Chain {
        Request request();
        Response proceed(Request request) throws Exception;
    }

    // 实际拦截器链实现
    static class RealInterceptorChain implements Chain {
        private final List<Interceptor> interceptors;
        private final int index;
        private final Request request;

        RealInterceptorChain(List<Interceptor> interceptors, int index, Request request) {
            this.interceptors = interceptors;
            this.index = index;
            this.request = request;
        }

        @Override
        public Request request() {
            return request;
        }

        @Override
        public Response proceed(Request request) throws Exception {
            // 1. 创建下一个链
            RealInterceptorChain next = new RealInterceptorChain(interceptors, index + 1, request);

            // 2. 获取当前拦截器
            Interceptor interceptor = interceptors.get(index);

            // 3. 调用当前拦截器并返回响应
            System.out.println("执行: " + interceptor.getClass().getSimpleName() + " | 调用链深度: " + index);
            return interceptor.intercept(next);
        }
    }

    // ================ 五大拦截器实现 ================

    // 1. 重试和重定向拦截器
    static class RetryAndFollowUpInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws Exception {
            Request request = chain.request();
            System.out.println("  → [Retry] 预处理: 添加重试机制");

            // 添加用户代理头
            request.addHeader("User-Agent: OkHttp");

            // 递归调用下一个拦截器
            System.out.println("  → [Retry] 调用下一个拦截器");
            Response response = chain.proceed(request);

            // 后处理：处理重定向
            System.out.println("  ← [Retry] 后处理: 检查重定向");
            if (response.body != null && response.body.contains("redirect")) {
                System.out.println("  ! [Retry] 检测到重定向");
            }

            return response;
        }
    }

    // 2. 桥接拦截器
    static class BridgeInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws Exception {
            Request request = chain.request();
            System.out.println("    → [Bridge] 预处理: 添加必要头部");

            // 添加必要头信息
            request.addHeader("Accept-Language: zh-CN");
            request.addHeader("Connection: keep-alive");

            System.out.println("    → [Bridge] 调用下一个拦截器");
            Response response = chain.proceed(request);

            System.out.println("    ← [Bridge] 后处理: 解压响应体");
            // 模拟解压响应
            if (response.body != null) {
                response.setBody(response.body.replace("(compressed)", ""));
            }

            return response;
        }
    }

    // 3. 缓存拦截器
    static class CacheInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws Exception {
            Request request = chain.request();
            System.out.println("      → [Cache] 预处理: 检查缓存");

            // 检查缓存
            if (request.url.contains("cached")) {
                System.out.println("      √ [Cache] 找到缓存，直接返回");
                Response cachedResponse = new Response(request);
                cachedResponse.setBody("缓存数据");
                return cachedResponse;
            }

            System.out.println("      → [Cache] 未找到缓存，调用下一个拦截器");
            Response response = chain.proceed(request);

            System.out.println("      ← [Cache] 后处理: 缓存响应");
            // 模拟缓存响应
            if (response.body != null && response.body.length() < 100) {
                System.out.println("      √ [Cache] 缓存响应数据");
            }

            return response;
        }
    }

    // 4. 连接拦截器
    static class ConnectInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws Exception {
            Request request = chain.request();
            System.out.println("        → [Connect] 预处理: 建立连接");

            // 建立连接
            System.out.println("        √ [Connect] 连接到: " + request.url);

            System.out.println("        → [Connect] 调用下一个拦截器");
            Response response = chain.proceed(request);

            System.out.println("        ← [Connect] 后处理: 释放连接资源");
            // 模拟释放资源
            System.out.println("        √ [Connect] 连接已关闭");

            return response;
        }
    }

    // 5. 请求服务拦截器
    static class CallServerInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws Exception {
            Request request = chain.request();
            System.out.println("          → [CallServer] 预处理: 发送网络请求");

            // 模拟网络请求
            System.out.println("          √ [CallServer] 发送请求到服务器");

            // 创建响应 - 这是递归的终点
            Response response = new Response(request);
            response.addHeader("Content-Type: application/json");
            response.addHeader("Server: Nginx");
            response.setBody("(compressed)服务器响应数据");

            System.out.println("          ← [CallServer] 返回响应 (递归终点)");
            return response;
        }
    }
}
