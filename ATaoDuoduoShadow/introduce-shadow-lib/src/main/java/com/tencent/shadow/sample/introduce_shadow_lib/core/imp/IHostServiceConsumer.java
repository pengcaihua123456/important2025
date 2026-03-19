package com.tencent.shadow.sample.introduce_shadow_lib.core.imp;


import com.tencent.shadow.common.IHostService;

/**
 * 插件服务消费者接口
 *
 * 【设计目的】
 * 定义一个标准契约，规定任何想要接收“宿主服务”的插件类都必须实现此方法。
 * 这样宿主在加载插件时，可以通过反射检测并统一注入依赖，无需知道插件的具体业务类名。
 *
 * 【放置位置】
 * 必须放在 Host 和 Plugin 都依赖的公共 Module (如 sample-constant) 中。
 */
public interface IHostServiceConsumer {

    /**
     * 设置宿主服务实例
     *
     * @param hostService 宿主实现的 IHostService 接口实例
     */
    void setHostService(IHostService hostService);
}