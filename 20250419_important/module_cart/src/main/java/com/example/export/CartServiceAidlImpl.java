package com.example.export;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.example.common.ICartServiceAidl;

import java.lang.ref.WeakReference;

public class CartServiceAidlImpl extends Service {

    private int currentValue = 0;

    // 使用回调接口通知 UI
    public interface ValueUpdateListener {
        void onValueUpdated(int newValue);
    }

    private static WeakReference<ValueUpdateListener> listenerRef;

    public static void setUpdateListener(ValueUpdateListener listener) {
        listenerRef = new WeakReference<>(listener);
    }

    private final IBinder binder = new ICartServiceAidl.Stub() {
        @Override
        public void updateValue(int newValue) {
            currentValue = newValue;
            notifyValueUpdate(newValue);
        }

        @Override
        public int getCurrentValue() {
            return currentValue;
        }
    };

    private void notifyValueUpdate(final int newValue) {
        // 确保在主线程更新 UI
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (listenerRef != null && listenerRef.get() != null) {
                    listenerRef.get().onValueUpdated(newValue);
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
