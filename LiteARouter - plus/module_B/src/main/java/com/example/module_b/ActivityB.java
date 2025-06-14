package com.example.module_b;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;


import com.example.module_B.R;
import com.example.module_route.lib.core.RouterActivity;

public class ActivityB extends RouterActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);

        Intent intent = getIntent();
        String platform = intent.getStringExtra("platform");
        int year = intent.getIntExtra("year", 0);
        Log.e("platform: ", ""+platform);
        Log.e("year: ", String.valueOf(year));


        String productId = getPathParam("id", String.class);
        String tabName = getPathParam("tab", String.class);
    }
}
