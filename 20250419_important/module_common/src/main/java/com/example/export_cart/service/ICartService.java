package com.example.export_cart.service;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.example.api.INoticeListener;
import com.example.export_cart.bean.CartInfo;

public interface ICartService extends IProvider {

    /**
     * 获取购物车中商品数量
     * @return
     */
    CartInfo getProductCountInCart();


    INoticeListener getNoticeListener();


    void   startByOtherModule();
}