package com.evenbus.myapplication.framwork.spi;

import com.google.auto.service.AutoService;

@AutoService(PaymentService.class)
public class WechatPayService implements PaymentService {
    @Override
    public void pay(double amount) {
        System.out.println("微信支付: ¥" + amount);
    }
}
