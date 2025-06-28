package com.evenbus.view.finger;

import android.app.Activity;
import android.os.Bundle;

public class FingerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FingerprintAnimationView(this));
        getWindow().setBackgroundDrawableResource(android.R.color.black);
    }

}
