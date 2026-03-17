package com

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.example.stepcopilot.core.AgentDemo

class MainApplication : Application() {
    companion object {
        @JvmStatic
        private lateinit var instance: MainApplication

        @JvmStatic
        fun getApplication(): MainApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化 com.example.stepcopilot.core.JieyueGuiDemo（传入自身 Application 实例）
        AgentDemo.init(this, null)

        // 延迟10秒后启动测试任务（可根据需要调整）
        Handler(Looper.getMainLooper()).postDelayed({
            AgentDemo.startTask("帮我用微博刷视频")
        }, 10000)
    }
}