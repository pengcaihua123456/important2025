package com.tencent.shadow.test.cases;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tencent.shadow.dynamic.loader.PluginLoader;
import com.tencent.shadow.dynamic.loader.PluginServiceConnection;
import com.tencent.shadow.test.UiUtil;
import com.tencent.shadow.test.lib.test_manager.SimpleIdlingResource;
import com.tencent.shadow.test.lib.test_manager.TestManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.CountDownLatch;

public class PluginServiceConnectionTestCase {

    private static final String STATUS_VIEW_TAG = "STATUS_VIEW_TAG";
    private static final String PACKAGE_VIEW_TAG = "PACKAGE_VIEW_TAG";
    private static final String CLASS_VIEW_TAG = "CLASS_VIEW_TAG";
    private static final String BIND_BUTTON_TAG = "BIND_BUTTON_TAG";
    private static final String MAGIC_NUMBER_MATCH_TAG = "MAGIC_NUMBER_MATCH_TAG";
    private static final String UNBIND_BUTTON_TAG = "UNBIND_BUTTON_TAG";
    private static final String STOP_BUTTON_TAG = "STOP_BUTTON_TAG";

    final private ViewGroup viewGroup;
    private IBinder service;
    final private PluginLoader pluginLoader;
    final private Intent pluginIntent;
    final private SimpleIdlingResource idlingResource;
    final private Handler uiHandler;
    private long bindServiceMagicNumber;

    public PluginServiceConnectionTestCase(PluginLoader pluginLoader, Intent pluginIntent) {
        this.pluginLoader = pluginLoader;
        this.pluginIntent = pluginIntent;
        viewGroup = (ViewGroup) TestManager.sBindPluginServiceActivityContentView;
        idlingResource = TestManager.TheSimpleIdlingResource;
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public void prepareUi() {
        final Context context = viewGroup.getContext();
        final CountDownLatch waitUiThread = new CountDownLatch(1);

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                viewGroup.addView(
                        UiUtil.makeItem(
                                context,
                                "ServiceConnection Callback",
                                STATUS_VIEW_TAG,
                                ""
                        )
                );

                viewGroup.addView(
                        UiUtil.makeItem(
                                context,
                                "ComponentName.getPackageName()",
                                PACKAGE_VIEW_TAG,
                                ""
                        )
                );

                viewGroup.addView(
                        UiUtil.makeItem(
                                context,
                                "ComponentName.getClassName()",
                                CLASS_VIEW_TAG,
                                ""
                        )
                );

                viewGroup.addView(
                        UiUtil.makeItem(
                                viewGroup.getContext(),
                                "magic_number_matched",
                                MAGIC_NUMBER_MATCH_TAG,
                                Boolean.toString(false)
                        )
                );

                Button bindService = new Button(context);
                bindService.setTag(BIND_BUTTON_TAG);
                bindService.setText("bindService");
                bindService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setEnabled(false);
                        bindService();
                    }
                });

                Button unbindService = new Button(context);
                unbindService.setTag(UNBIND_BUTTON_TAG);
                unbindService.setText("unbindService");
                unbindService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setEnabled(false);
                        unbindService();
                    }
                });

                Button stopService = new Button(context);
                stopService.setTag(STOP_BUTTON_TAG);
                stopService.setText("stopService");
                stopService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setEnabled(false);
                        stopService();
                    }
                });

                viewGroup.addView(bindService);
                viewGroup.addView(unbindService);
                viewGroup.addView(stopService);

                waitUiThread.countDown();
            }
        });

        try {
            waitUiThread.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindService() {
        idlingResource.setIdleState(false);
        try {
            // 在bind service的intent中放入一个参数，以便测试Service收到onUnbind时能获得bind时的intent。
            bindServiceMagicNumber = System.currentTimeMillis();
            pluginIntent.putExtra("magic_number", bindServiceMagicNumber);
            pluginLoader.bindPluginService(pluginIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void unbindService() {
        idlingResource.setIdleState(false);
        try {
            pluginLoader.unbindService(serviceConnection);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopService() {
        idlingResource.setIdleState(false);
        try {
            //随便发什么过去都表示杀进程
            service.transact(0, Parcel.obtain(), Parcel.obtain(), 0);
        } catch (RemoteException ignored) {
        }
    }

    final private PluginServiceConnection serviceConnection = new PluginServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, IBinder service) {
            PluginServiceConnectionTestCase.this.service = service;
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    UiUtil.setItemValue(viewGroup, STATUS_VIEW_TAG, "onServiceConnected");
                    UiUtil.setItemValue(viewGroup, PACKAGE_VIEW_TAG, name.getPackageName());
                    UiUtil.setItemValue(viewGroup, CLASS_VIEW_TAG, name.getClassName());
                    idlingResource.setIdleState(true);
                }
            });
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    UiUtil.setItemValue(viewGroup, STATUS_VIEW_TAG, "onServiceDisconnected");
                    UiUtil.setItemValue(viewGroup, PACKAGE_VIEW_TAG, name.getPackageName());
                    UiUtil.setItemValue(viewGroup, CLASS_VIEW_TAG, name.getClassName());
                    long actualMagicNumber = readSystemExitServiceOnUnbindWriteOutMagicNumber();
                    UiUtil.setItemValue(viewGroup,
                            MAGIC_NUMBER_MATCH_TAG,
                            Boolean.toString(bindServiceMagicNumber == actualMagicNumber));
                    idlingResource.setIdleState(true);
                }

                private long readSystemExitServiceOnUnbindWriteOutMagicNumber() {
                    File pluginFilesDir = new File(viewGroup.getContext().getFilesDir(),
                            "ShadowPlugin_plugin-service-for-host");
                    File magicNumberOutputFile = new File(pluginFilesDir,
                            "SystemExitService.onUnbind");

                    try (BufferedReader br = new BufferedReader(new FileReader(magicNumberOutputFile))) {
                        String s = br.readLine();
                        return Long.parseLong(s);
                    } catch (Exception ignored) {
                        return -1;//与bindServiceMagicNumber默认值0不同。
                    }
                }
            });
        }
    };

}
