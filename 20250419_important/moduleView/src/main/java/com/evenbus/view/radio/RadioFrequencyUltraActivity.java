package com.evenbus.view.radio;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.view.wheel.PerfectWheelView;
import com.example.module_view.R;

import java.util.Arrays;
import java.util.List;

public class RadioFrequencyUltraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_view);


        PerfectWheelView wheelView = findViewById(R.id.wheelView);

        // 设置数据
        List<String> data = Arrays.asList("1. 北京", "2. 上海", "3. 广州,4. 北京, 5. 上海, 6. 广州");
        wheelView.setItems(data);
        // 启用/禁用循环滚动（默认启用）
        wheelView.setCyclic(true);
        // 获取选中项
        String selected = wheelView.getSelectedItem();
    }

}
