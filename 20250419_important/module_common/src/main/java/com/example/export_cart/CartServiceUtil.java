package com.example.export_cart;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.api.INoticeListener;
import com.example.export_cart.bean.CartInfo;
import com.example.export_cart.router.CartRouterTable;
import com.example.export_cart.service.ICartService;

public class CartServiceUtil {

    /**
     * 跳转到购物车页面
     * @param param1
     * @param param2
     */
    public static void navigateCartPage(String param1, String param2){
        ARouter.getInstance()
                .build(CartRouterTable.PATH_PAGE_CART)
                .withString("key1",param1)
                .withString("key2",param2)
                .navigation();
    }

    /**
     * 获取服务
     * @return
     */
    public static ICartService getService(){
        //return ARouter.getInstance().navigation(ICartService.class);//如果只有一个实现，这种方式也可以
        return (ICartService) ARouter.getInstance().build(CartRouterTable.PATH_SERVICE_CART).navigation();
    }


    public static INoticeListener getNoticeListener(){
        return getService().getNoticeListener();
    }

    /**
     * 获取购物车中商品数量
     * @return
     */
    public static CartInfo getCartProductCount(){
        return getService().getProductCountInCart();
    }


    public static void startByOtherModule(){
        getService().startByOtherModule();
    }
}