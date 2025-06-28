package com.evenbus.view.extand;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.module_view.R;


public class ExpandActivity extends Activity {
    private AnimationCardView mSmartCard;
    private AnimationCardViewPlus animationCardViewPlus;
    private Handler mHandler = new Handler(); // 添加Handler用于延时执行

    // 在Activity中
    AnimationCardViewPlus cardViewPlus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand_new);
        mSmartCard = findViewById(R.id.smartCard);
        animationCardViewPlus = findViewById(R.id.smartCardplus);


        // 4秒后执行初始化
        mHandler.postDelayed(this::delayedInitView, 4000);

        plus();
    }

    public void plus(){
        cardViewPlus = findViewById(R.id.smartCardplus);
    }

    // 延时初始化方法
    private void delayedInitView() {
        // 初始状态
        mSmartCard.startExpandAnimation();
        animationCardViewPlus.startExpandAnimation();
    }

    public void toggleCard(View view) {
        mSmartCard.toggleCard();
        animationCardViewPlus.toggleCard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除所有回调，防止内存泄漏
        mHandler.removeCallbacksAndMessages(null);
    }
}
