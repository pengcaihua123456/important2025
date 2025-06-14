package com.example.arountlib;

import android.content.Context;
import android.content.Intent;

import com.example.routelib.ICardJumpInterface;
import com.example.export_cart.bean.CartInfo;
import com.example.module_cart.CartActivity;

public class CardModuleImpl implements ICardJumpInterface {

    @Override
    public void jumpcardModuleActivity(CartInfo cartInfo, Context context) {
        Intent intent = new Intent(context, CartActivity.class);
        context.startActivity(intent);
    }
}