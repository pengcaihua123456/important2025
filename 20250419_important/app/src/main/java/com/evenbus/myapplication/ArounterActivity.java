package com.evenbus.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.modulebufferknifeantotations.BindView;
import com.example.modulebufferknifeantotations.OnClick;

public class ArounterActivity extends AppCompatActivity {

    private static final String TAG = "ArounterActivity";

    public TextView tv_arount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    /**
     * 手写bufferKnite
     */
    @BindView(R.id.tv_compler)
    public TextView tv_compler;


    @OnClick(R.id.tv_compler)
    public void onclick(View v){

    }

    private void initView() {
        tv_arount = findViewById(R.id.tv_arount);
        tv_arount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过路由直接打开home组件的HomeActivity，
                ARouter.getInstance().build("/homepage/homeActivity").navigation();
            }
        });

    }
}
