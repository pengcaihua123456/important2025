package com.taoduoduo.host.core;


import com.taoduoduo.host.core.imp.HostServiceImpl;
import com.tencent.shadow.common.IPluginInfoProvider;

/**
 * 插件获取宿主的数据
 */
public class HostToPluginDataService {
    public void setHostService(){
        IPluginInfoProvider iPluginInfoProvider=PluginToHostDataService.getProvider(null,"");
        iPluginInfoProvider.setHostService(new HostServiceImpl());
    }
}
