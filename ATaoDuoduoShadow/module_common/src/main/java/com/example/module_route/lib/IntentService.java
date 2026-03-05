package com.example.module_route.lib;


import com.example.module_route.lib.annotations.ClassName;
import com.example.module_route.lib.annotations.Key;
import com.example.module_route.lib.annotations.RequestCode;

/**
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 16/10/21 12:23]
 */

public interface IntentService {
    @ClassName("com.example.module_a.ActivityA")
    @RequestCode(100)
    void intent2ActivityDemo2(@Key("platform") String platform, @Key("year") int year);

    @ClassName("com.example.module_b.ActivityB")
    IntentWrapper intent2ActivityDemo2Raw(@Key("platform") String platform, @Key("year") int year);
}
