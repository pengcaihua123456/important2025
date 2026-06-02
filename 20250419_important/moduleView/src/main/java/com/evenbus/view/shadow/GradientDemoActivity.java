package com.evenbus.view.shadow;

import android.app.Activity;
import android.os.Bundle;

import com.example.module_view.R;

public class GradientDemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gradient_demo);
        
        // 无需额外初始化，三个自定义View已自带动画
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 确保动画继续
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 可在此处暂停动画以节省资源，但自定义View已处理
    }
}