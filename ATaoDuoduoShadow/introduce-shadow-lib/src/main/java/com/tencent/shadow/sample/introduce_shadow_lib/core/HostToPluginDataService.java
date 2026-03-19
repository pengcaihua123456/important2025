package com.tencent.shadow.sample.introduce_shadow_lib.core;


import com.tencent.shadow.common.IHostService;
import com.tencent.shadow.common.IPluginInfoProvider;

/**
 * 插件获取宿主的数据
 */
public class HostToPluginDataService {
    public void setHostService(IHostService iHostService){
        IPluginInfoProvider iPluginInfoProvider=PluginToHostDataService.getProvider(null,"");
        iPluginInfoProvider.setHostService(iHostService);
    }
}
