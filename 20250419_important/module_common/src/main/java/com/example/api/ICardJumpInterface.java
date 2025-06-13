package com.example.api;

import android.content.Context;

import com.example.export_cart.bean.CartInfo;

/**
 * @Author pengcaihua
 * @Date 17:24
 * @describe
 */
public interface ICardJumpInterface {
    void jumpcardModuleActivity(CartInfo cartInfo, Context context);
}