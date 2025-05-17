package com.example.modulematrix.matrix;

import android.app.Application;

import java.io.File;

import com.tencent.matrix.Matrix;
import com.tencent.matrix.trace.TracePlugin;
import com.tencent.matrix.trace.config.TraceConfig;
import com.tencent.matrix.util.MatrixLog;



public class MatrixUtil {

    private static final String TAG = "Matrix.Application";

    public  void init(Application application){

        // Switch.
        DynamicConfigImplDemo dynamicConfig = new DynamicConfigImplDemo();
        MatrixLog.i(TAG, "Start Matrix configurations.");

        // Builder. Not necessary while some plugins can be configured separately.
        Matrix.Builder builder = new Matrix.Builder(application);
        // Reporter. Matrix will callback this listener when found issue then emitting it.
        builder.pluginListener(new TestPluginListener(application));


        // Configure trace canary.
        TracePlugin tracePlugin = configureTracePlugin(dynamicConfig, application);
        builder.plugin(tracePlugin);
        Matrix.init(builder.build());

        Matrix.with().startAllPlugins();

    }

    private TracePlugin configureTracePlugin(DynamicConfigImplDemo dynamicConfig,Application application) {

        boolean fpsEnable = dynamicConfig.isFPSEnable();
        boolean traceEnable = dynamicConfig.isTraceEnable();
        boolean signalAnrTraceEnable = dynamicConfig.isSignalAnrTraceEnable();

        File traceFileDir = new File(application.getFilesDir(), "matrix_trace");
        if (!traceFileDir.exists()) {
            if (traceFileDir.mkdirs()) {
                MatrixLog.e(TAG, "failed to create traceFileDir");
            }
        }

        File anrTraceFile = new File(traceFileDir, "anr_trace");    // path : /data/user/0/sample.tencent.matrix/files/matrix_trace/anr_trace
        File printTraceFile = new File(traceFileDir, "print_trace");    // path : /data/user/0/sample.tencent.matrix/files/matrix_trace/print_trace

        TraceConfig traceConfig = new TraceConfig.Builder()
                .dynamicConfig(dynamicConfig)
                .enableFPS(fpsEnable)
                .enableEvilMethodTrace(traceEnable)
                .enableAnrTrace(traceEnable)
                .enableStartup(traceEnable)
                .enableIdleHandlerTrace(traceEnable)                    // Introduced in Matrix 2.0
                .enableSignalAnrTrace(signalAnrTraceEnable)             // Introduced in Matrix 2.0
//                .anrTracePath(anrTraceFile.getAbsolutePath())
//                .printTracePath(printTraceFile.getAbsolutePath())
//                .splashActivities("sample.tencent.matrix.SplashActivity;")
                .isDebug(true)
                .isDevEnv(false)
                .build();

        GwEvilMethodTracer gwEvilMethodTracer = new GwEvilMethodTracer(traceConfig);
        gwEvilMethodTracer.onStartTrace();

        //Another way to use SignalAnrTracer separately
        //useSignalAnrTraceAlone(anrTraceFile.getAbsolutePath(), printTraceFile.getAbsolutePath());

        return new TracePlugin(traceConfig);
    }



}
