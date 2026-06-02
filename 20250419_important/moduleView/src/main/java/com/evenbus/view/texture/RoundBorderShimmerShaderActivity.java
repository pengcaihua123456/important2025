package com.evenbus.view.texture;

import android.app.Activity;
import android.os.Bundle;

import com.example.module_view.R;

public class RoundBorderShimmerShaderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_border_shimmer_shader);
        
        // RoundBorderShimmerView_Shader 已经在布局中定义，并自带动画效果
        // 无需额外初始化
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 确保动画继续
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 可在此处暂停动画以节省资源，但RoundBorderShimmerView_Shader已处理
    }
}