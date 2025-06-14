package android.router.literouter;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;


import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_route.lib.CartItem;
import com.example.module_route.lib.core.RouterFactory;
import com.example.module_route.lib.services.AppRouterService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCheckout(MainActivity.this,new ArrayList<CartItem>());
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("requestCode: ", String.valueOf(requestCode));
        Log.e("resultCode: ", String.valueOf(resultCode));
    }

    private final AppRouterService router = RouterFactory.create(AppRouterService.class);

    public void startCheckout(Context context, List<CartItem> items) {
        Intent intent = router.navigateToCartCheckout(items);
        context.startActivity(intent);
    }
}
