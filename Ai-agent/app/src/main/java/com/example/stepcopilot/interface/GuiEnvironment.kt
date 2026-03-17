package com.example.stepcopilot.`interface`

import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo

/**
 * GUI 自动化执行环境抽象接口。
 * 所有与设备、应用交互的操作都需要通过此接口执行，以便在不同平台上适配。
 */
interface GuiEnvironment {
    /** 获取当前顶部 Activity 的包名 */
    fun getTopPackage(): String?

    /** 获取当前顶部 Activity 的类名 */
    fun getTopActivity(): String?

    /** 获取当前窗口的根 AccessibilityNodeInfo（可能为 null） */
    fun getRootAccessibilityNode(): AccessibilityNodeInfo?

    /** 获取指定包名的窗口根节点（用于虚拟屏等场景） */
    fun getWindowRootForPackage(packageName: String): AccessibilityNodeInfo?

    /** 在指定坐标点击 */
    fun click(x: Int, y: Int)

    /** 长按指定坐标 */
    fun longClick(x: Int, y: Int)

    /** 输入文本，如果提供了坐标，则先点击该位置 */
    fun inputText(text: String, x: Int? = null, y: Int? = null)

    /** 滑动操作 */
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int)

    /** 截图返回 Bitmap */
    fun takeScreenshot(): Bitmap?

    /** 截屏并返回 Base64 编码的 JPEG 字符串 */
    fun takeScreenshotBase64(): String

    /** 获取屏幕宽度 */
    fun getScreenWidth(): Int

    /** 获取屏幕高度 */
    fun getScreenHeight(): Int

    /** 语音播报 */
    fun speak(text: String)

    /** 显示 Toast */
    fun showToast(text: String)

    /** 检查应用是否已安装 */
    fun isAppInstalled(packageName: String): Boolean

    /** 启动应用（通过包名） */
    fun launchApp(packageName: String)

    /** 强制停止应用 */
    fun forceStopApp(packageName: String)

    /** 判断当前是否在虚拟屏运行 */
    fun isVirtualDisplayRunning(): Boolean

    /** 获取虚拟屏 ID（如果没有则返回 null） */
    fun getVirtualDisplayId(): Int?

    /** 切换到虚拟屏（将指定应用放入虚拟屏） */
    fun switchToVirtualDisplay(packageName: String)

    /** 退出虚拟屏，回到主屏幕 */
    fun exitVirtualDisplay()

    /** 判断指定包名的应用是否正在前台显示（主屏或虚拟屏） */
    fun isAppInForeground(packageName: String): Boolean

    /** 获取设备唯一标识（如 Android ID） */
    fun getDeviceId(): String

    /** 获取设备型号 */
    fun getDeviceModel(): String

    /** 获取操作系统版本（具体构建号） */
    fun getOsVersion(): String

    /** 获取时区 ID */
    fun getTimeZoneId(): String

    /** 获取已安装应用列表（用于初始化会话） */
    fun getInstalledApps(): List<Map<String, String>> // 每个 map 包含 app_name 和 package_name
}