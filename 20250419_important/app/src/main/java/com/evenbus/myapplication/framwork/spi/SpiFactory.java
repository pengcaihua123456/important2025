package com.evenbus.myapplication.framwork.spi;

import java.util.ServiceLoader;

/**
 * @Author pengcaihua
 * @Date 15:35
 * @describe
 */
public class SpiFactory {

    /***
     * 有 SPI, 并且是通过autoService实现的
     * 1).不需要因为添加实现类，改动代码
     * 2).不需要手动注册服务
     * @param args
     */
    public static void autoServiceMain(String[] args) {
        ServiceLoader<PaymentService> services =
                ServiceLoader.load(PaymentService.class);
        for (PaymentService service : services) {
            service.pay(100.0);
        }
    }

    /***
     * 有 SPI：
     * 1).不需要因为添加实现类，改动代码
     * @param args
     */
    public static void main(String[] args) {
        ServiceLoader<PaymentService> services =
                ServiceLoader.load(PaymentService.class);
        for (PaymentService service : services) {
            service.pay(100.0);
        }
    }

    /***
     * 无 SPI
     * 需要硬编码,并且改动调用类
     * @param args
     */
    public static void main2(String[] args) {
        // 硬编码实现
        PaymentService service;
        String type="";
        if ("alipay".equals(type)) {
            service = new AlipayService();
        } else if ("wechat".equals(type)) {
            service = new WechatPayService();
        } // 添加新支付需修改此处
    }

}
