package com.example.module_a;

import android.app.Activity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_A.R;
import com.example.module_route.lib.CartItem;
import com.example.module_route.lib.core.RouterActivity;
import com.example.module_route.lib.core.RouterFactory;
import com.example.module_route.lib.services.AppRouterService;

import java.util.List;

public class ActivityA extends RouterActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);

        Intent intent = getIntent();
        String platform = intent.getStringExtra("platform");
        int year = intent.getIntExtra("year", 0);
        Log.e("platform", ""+platform);
        Log.e("year", String.valueOf(year));

//        setResult(Activity.RESULT_OK);


        findViewById(R.id.btnB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("ActivityA", "444444444");

                startHomeDetail(ActivityA.this);

            }
        });

    }

    private final AppRouterService router = RouterFactory.create(AppRouterService.class);

    public void startHomeDetail(Context context) {
        Intent intent = router.navigateToHomeDetail("123", "reviews");
        context.startActivity(intent);
    }

    public void startCheckout(Context context, List<CartItem> items) {
        Intent intent = router.navigateToCartCheckout(items);
        context.startActivity(intent);
    }
}
