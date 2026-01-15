package com.example.module_a;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


import com.example.module_A.R;
import com.example.module_route.lib.IntentService;
import com.example.module_route.lib.IntentWrapper;
import com.example.module_route.lib.Interceptor;
import com.example.module_route.lib.LiteRouter;

public class ActivityA extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);

        Intent intent = getIntent();
        String platform = intent.getStringExtra("platform");
        int year = intent.getIntExtra("year", 0);
        Log.e("platform: ", platform);
        Log.e("year: ", String.valueOf(year));

        setResult(Activity.RESULT_OK);


        findViewById(R.id.btnB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("ActivityA", "444444444");



                LiteRouter liteRouter = new LiteRouter.Builder().interceptor(new Interceptor() {
                    @Override
                    public boolean intercept(IntentWrapper intentWrapper) {
                        return false;
                    }
                }).build();
                final IntentService intentService = liteRouter.create(IntentService.class, ActivityA.this);
                IntentWrapper intentWrapper = intentService.intent2ActivityDemo2Raw("android", 2016);
                // intent
                Intent intent = intentWrapper.getIntent();
                // add your flags
                intentWrapper.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                // start
                intentWrapper.start();
            }
        });

    }
}
