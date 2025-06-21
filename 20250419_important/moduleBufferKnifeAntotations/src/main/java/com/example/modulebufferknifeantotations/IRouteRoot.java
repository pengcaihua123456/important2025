package com.example.modulebufferknifeantotations;



import java.util.Map;

/**
 * 根路由接口（由APT自动实现）
 */
public interface IRouteRoot {

    /**
     * 将路由组信息加载到路由表中
     * @param routes 输入参数：路由表（Key=组名，Value=路由组Class对象）
     */
    void loadInto(Map<String, Class<? extends IRouteGroup>> routes);
}