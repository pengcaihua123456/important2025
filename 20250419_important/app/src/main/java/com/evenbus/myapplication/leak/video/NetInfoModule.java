package com.evenbus.myapplication.leak.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// 网络信息模块 - NetInfoModule.java
public class NetInfoModule {

    @Override
    public String toString() {

        return "toString NetInfoModule{" +
                "receiver=" + receiver +
                '}';
    }

    private List<NetChangeListener> listeners = new ArrayList<>();
    private NetworkBroadcastReceiver receiver;

    public void registerNetChangeListener(NetChangeListener listener,Context contextInput) {
        listeners.add(listener);
        if (receiver == null) {
            receiver = new NetworkBroadcastReceiver();
            // 注册广播接收器（使用ApplicationContext可以避免这个问题）
            Context context=contextInput.getApplicationContext();// 注意看看
            context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    public void unregisterNetChangeListener(NetChangeListener listener,Context contextInput) {
        listeners.remove(listener);
        if (listeners.isEmpty() && receiver != null) {
            Context context=contextInput.getApplicationContext();
            context.unregisterReceiver(receiver);
            receiver = null;
        }
    }

    // 网络广播接收器
    private class NetworkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 通知所有监听器
            Log.d("NetInfoModule","onReceive" +this);
            for (NetChangeListener listener : listeners) {
                listener.onNetChange(true);
            }
        }
    }
}

