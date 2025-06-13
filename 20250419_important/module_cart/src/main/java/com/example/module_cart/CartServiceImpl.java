package com.example.module_cart;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.api.INoticeListener;
import com.example.export_cart.bean.CartInfo;
import com.example.export_cart.router.CartRouterTable;
import com.example.export_cart.service.ICartService;

/**
 * 购物车组件服务的实现
 * @author hufeiyang
 */
@Route(path = CartRouterTable.PATH_SERVICE_CART)
public class CartServiceImpl implements ICartService {

    @Override
    public CartInfo getProductCountInCart() {
        CartInfo cartInfo = new CartInfo();
        cartInfo.productCount = 666;
        return cartInfo;
    }


    /****
     * CartServiceImpl service = (CartServiceImpl) ARouter.getInstance()
     *                 .build("/cart/service")
     *                 .navigation();
     *         if (service != null) {
     *             service.bindFragment(this);
     *         }
     *
     *      ICartService cartService = (ICartService) ARouter.getInstance()
     *                 .build("/cart/service")
     *                 .navigation();
     *
     *CartServiceImpl service = (CartServiceImpl) ARouter.getInstance()
     *                 .build("/cart/service")
     *                 .navigation();
     *
     *
     * @return
     */
    @Override
    public INoticeListener getNoticeListener() {
        return new INoticeListener() {
            @Override
            public void fouce(boolean isFouce) {
                // 这里可以通知cardFragment，可以通过eventBus
                // 需要搞清楚CartServiceImpl和CartFragment的关系,CartServiceImpl是如何获取的,怎么初始化的
            }
        };
    }

    @Override
    public void startByOtherModule() {
        // 通过eventbus调用fragment里面的方法
    }

    @Override
    public void init(Context context) {
        //初始化工作，服务注入时会调用
    }
}
