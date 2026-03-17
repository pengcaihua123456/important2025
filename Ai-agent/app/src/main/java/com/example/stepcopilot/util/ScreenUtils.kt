package com.example.stepcopilot.util

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import android.view.Surface
import com.MainApplication

import java.io.ByteArrayOutputStream
import java.io.File

class ScreenUtils {
    companion object {
        private const val TAG = "ScreenUtils"

        fun getShotJpegBase64(): String {
            val startTime = System.currentTimeMillis()
            val screenWidth = DisplayUtils.getScreenWidth(MainApplication.getApplication())
            val screenHeight = DisplayUtils.getScreenHeight(MainApplication.getApplication())

            val bitmap= null
//            val bitmap = SurfaceControl.screenshot(
//                Rect(),
//                screenWidth,
//                screenHeight,
//                Surface.ROTATION_0
//            )

            Log.d(TAG, "getShotJpegBase64 $screenWidth $screenHeight"+bitmap)

            val time = System.currentTimeMillis() - startTime
            Log.d(TAG, "screen shot cost time:$time")
            return bt2JpegBase64(bitmap)
        }

        fun bt2JpegBase64(bitmap: Bitmap?):String {
            val startTime = System.currentTimeMillis()
            val outputStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            val encodeToString = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            val time = System.currentTimeMillis() - startTime
            Log.d(TAG, "bt to base64 cost time:$time ")
            return encodeToString
        }
    }

}
