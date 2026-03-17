package com.example.stepcopilot.util;


import android.util.Log;


import java.io.IOException;

public class SendKeyUtils {
    private static final String TAG = "SendKeyUtils";
    /**
     * 模拟滑动翻页事件
     *
     * @param points eg： input swipe 960 540 960 180
     */
    public static void simulateSwipeEvent(String[] points) {
        if (points == null || points.length != 4) {
            Log.w(TAG, "simulateSwipeEvent,parameter erro ---");
            return;
        }
        String[] cmdArrary = new String[6];
        cmdArrary[0] = "input";
        cmdArrary[1] = "swipe";
        cmdArrary[2] = points[0];
        cmdArrary[3] = points[1];
        cmdArrary[4] = points[2];
        cmdArrary[5] = points[3];
        Log.d(TAG, "simulateSwipeEvent conmmand: " + cmdArrary[0] + " " + cmdArrary[1] + " " + cmdArrary[2] + " " + cmdArrary[3] + " " + cmdArrary[4] + " " + cmdArrary[5]);
        ProcessBuilder processBuilder = new ProcessBuilder();
        try {
            processBuilder.command(cmdArrary).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}