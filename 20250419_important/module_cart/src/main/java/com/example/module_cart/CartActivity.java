package com.example.module_cart;

import android.app.Activity;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.export_cart.router.CartRouterTable;

@Route(path = CartRouterTable.PATH_PAGE_CART)
public class CartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


//        FragmentManager manager = getSupportFragmentManager();
//        FragmentTransaction  transaction= manager.beginTransaction();
//        Fragment userFragment = CartFragment.newInstance("param1","param2");
//        transaction.add(R.id.fl_test_fragment, userFragment, "tag");
//        transaction.commit();
    }
}