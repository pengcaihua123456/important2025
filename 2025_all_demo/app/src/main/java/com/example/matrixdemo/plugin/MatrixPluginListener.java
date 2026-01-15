package com.example.matrixdemo.plugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.tencent.matrix.plugin.DefaultPluginListener;
import com.tencent.matrix.report.Issue;
import com.tencent.matrix.util.MatrixLog;

import java.lang.ref.SoftReference;

/**
 * @author: njb
 * @date: 2023/8/10 11:21
 * @desc:
 */
public class MatrixPluginListener extends DefaultPluginListener {
    public static final String TAG = "MatrixPluginListener";
    public SoftReference<Context> softReference;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public MatrixPluginListener(Context context) {
        super(context);
        softReference = new SoftReference<>(context);
    }

    @Override
    public void onReportIssue(Issue issue) {
        super.onReportIssue(issue);
        //todo 处理性能监控数据
        MatrixLog.e(TAG, issue.toString());
        LogUtils.e(TAG, issue.toString());
        // IssuesMap.put(IssueFilter.getCurrentFilter(), issue);

     /*   mHandler.post(new Runnable() {// 等ui线程阻塞结束后，才执行
            @Override
            public void run() {
                Context context = softReference.get();
                String message = String.format("666 Report an issue - [%s]. context - [%s]", issue.getTag(),context);
                LogUtils.e(TAG, message);
                if (context != null) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            }
        });*/
    }

}
