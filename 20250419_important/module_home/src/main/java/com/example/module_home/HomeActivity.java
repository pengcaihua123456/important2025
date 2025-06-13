package com.example.module_home;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.api.ICardJumpInterface;
import com.example.api.INoticeListener;
import com.example.common.ICartServiceAidl;
import com.example.export_cart.CartServiceUtil;
import com.example.export_cart.router.CartRouterTable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Route(path = "/homepage/homeActivity")
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bindCartService();
        Log.d("HomeActivity","---------");

        //跳转到购物车页面
        findViewById(R.id.btn_go_cart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过路由跳转到 购物车组件的购物车页面（但没有依赖购物车组件）,
//                ARouter.getInstance()
//                        .build("/cart/cartActivity")
//                        .withString("key1","param1")//携带参数1
//                        .withString("key2","param2")//携带参数2
//                        .navigation();

                CartServiceUtil.navigateCartPage("param1", "param1");  // 携带参数

                myArunter();

                notifyCardFragment();

                updateCartValue(333);
            }
        });


        //调用购物车组件服务：获取购物车商品数量
        TextView tvCartProductCount = findViewById(R.id.tv_cart_product_count);
        tvCartProductCount.setText("购物车商品数量:"+ CartServiceUtil.getCartProductCount().productCount);



        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction= manager.beginTransaction();

        // 拿到cart_fragment ,但是不是完整的实例，获取实例不合理
        Fragment cart_Fragment = (Fragment) ARouter.getInstance().build(CartRouterTable.PATH_FRAGMENT_CART).navigation();
        transaction.add(R.id.fl_test_fragment, cart_Fragment, "tag");
        transaction.commit();
    }

    /***
     * 调用fragment里面的的方法
     */
    public void startByOtherModule() {
        CartServiceUtil.startByOtherModule();
    }

    /***
     * 通知cardFragment去关注
     */
    private void notifyCardFragment() {
        INoticeListener iNoticeListener=CartServiceUtil.getNoticeListener();
        iNoticeListener.fouce(true);
    }

    private void myArunter() {
        Log.d("HomeActivity","----myArunter-----");
        ICardJumpInterface iCardJumpInterface=getByReflect();
        iCardJumpInterface.jumpcardModuleActivity(null,HomeActivity.this);
    }

    private ICardJumpInterface getByReflect(){
        try {
            Class<?> clazz = Class.forName("com.example.export.CardModuleImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            // 如果需要突破私有构造方法限制
            constructor.setAccessible(true);
            ICardJumpInterface instance = (ICardJumpInterface) constructor.newInstance();
            return instance;
        } catch (ClassNotFoundException e) {
            System.err.println("类未找到，请检查包路径");
        } catch (NoSuchMethodException e) {
            System.err.println("无参构造方法不存在");
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("实例创建失败: " + e.getCause());
        }

        return null;
    }

    private ICartServiceAidl cartService;


    private  void bindCartService() {
        Intent intent = new Intent();
        // 设置Cart服务的完整包名
        intent.setComponent(new ComponentName(
                "com.example.cart",  // Cart模块包名
                "com.example.cart.CartServiceImpl" // 服务类全名
        ));

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            cartService = ICartServiceAidl.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            cartService = null;
        }
    };

    private void updateCartValue(int newValue) {
        if (cartService != null) {
            try {
                cartService.updateValue(newValue);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}