package com.example.stepcopilot.core

import MyGuiDialog
import MyGuiEnvironment
import android.accessibilityservice.AccessibilityService
import android.content.Context

object AgentDemo {
    private lateinit var client: AgentClient
    private const val API_KEY = "1AZdcYfKtT7sOu6ZVWACIhBdpfFNInlDYp8iGKATBG6cIWOnBJqrkXjTe6J73IpLm"  // 替换为真实 Key

    fun init(context: Context, accessibilityService: AccessibilityService? = null) {
        val environment = MyGuiEnvironment(context, accessibilityService)
        client = AgentClient(
            context = context,
            environment = environment,
            apiKey = API_KEY,
            guiDialog = MyGuiDialog(), // 可选，实现 IGuiDialog 接口
            // 其他参数按需配置
        )
    }

    fun startTask(task: String) {
        client.executeScene(task)
    }
}