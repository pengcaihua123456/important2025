package com.evenbus.myapplication.framwork.spi;

import com.google.auto.service.AutoService;

@AutoService(PaymentService.class)
public class AlipayService implements PaymentService{
    @Override
    public void pay(double amount) {
        System.out.println("支付宝支付: ¥" + amount);
    }
}