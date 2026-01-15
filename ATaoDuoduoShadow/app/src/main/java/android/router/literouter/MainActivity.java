package android.router.literouter;

import android.content.Intent;


import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.taoduoduo.shadow.R;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_route.lib.IntentService;
import com.example.module_route.lib.IntentWrapper;
import com.example.module_route.lib.Interceptor;
import com.example.module_route.lib.LiteRouter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LiteRouter liteRouter = new LiteRouter.Builder().interceptor(new Interceptor() {
            @Override
            public boolean intercept(IntentWrapper intentWrapper) {
                return false;
            }
        }).build();
        final IntentService intentService = liteRouter.create(IntentService.class, this);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentService.intent2ActivityDemo2("android", 2016);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("requestCode: ", String.valueOf(requestCode));
        Log.e("resultCode: ", String.valueOf(resultCode));
    }
}
