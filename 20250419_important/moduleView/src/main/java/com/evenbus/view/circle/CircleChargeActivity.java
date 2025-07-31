package com.evenbus.view.circle;

import android.app.Activity;
import android.os.Bundle;

import com.evenbus.view.charging.view.HwChargingView;
import com.example.module_view.R;

import java.util.Timer;
import java.util.TimerTask;


public class CircleChargeActivity extends Activity {

    CircleChargeAnimationViewPlus circleChargeAnimationViewPlus;
    int currentPercentage=45;
    int percentage=55;


    CircleChargeAnimationViewReally circleChargeAnimationView;

    HwChargingView hwChargingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_charge);
        getWindow().setBackgroundDrawableResource(R.color.colorAccent);


        hwChargingView=  findViewById(R.id.hw_charging);
        circleChargeAnimationViewPlus = findViewById(R.id.chargeAnimationViewPlus);

        // 模拟充电进度变化
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    currentPercentage = (currentPercentage + 1) % 101;
                    circleChargeAnimationViewPlus.setPercentage(currentPercentage);
                    hwChargingView.setProgress(currentPercentage);

                    if (currentPercentage == 100) {
                        // 充满电后改变提示文本
                        circleChargeAnimationViewPlus.setChargeText("充电完成");
                        cancel();
                    }
                });
            }
        }, 0, 300);


        circleChargeAnimationView = findViewById(R.id.chargeAnimationViewReally);

        // 模拟充电进度变化
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    percentage = (percentage + 1) % 101;
                    circleChargeAnimationView.setPercentage(percentage);

                    if (percentage == 100) {
                        // 充满电后改变提示文本
                        circleChargeAnimationView.setChargeText("充电完成");
                        cancel();
                    }
                });
            }
        }, 0, 300);

    }

}