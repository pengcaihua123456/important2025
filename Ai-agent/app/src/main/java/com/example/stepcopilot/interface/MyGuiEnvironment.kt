import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.stepcopilot.`interface`.GuiEnvironment
import com.example.stepcopilot.util.ScreenUtils
import com.example.stepcopilot.util.SendKeyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MyGuiEnvironment(
    private val context: Context,
    private val accessibilityService: AccessibilityService? // 传入你的 AccessibilityService 实例
) : GuiEnvironment {

    // 备选：Instrumentation（普通应用无权限，可能无效）
    private val instrumentation = android.app.Instrumentation()

    override fun getTopPackage(): String? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val tasks = am.getRunningTasks(1)
        return if (tasks.isNotEmpty()) tasks[0].topActivity?.packageName else null
    }

    override fun getTopActivity(): String? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val tasks = am.getRunningTasks(1)
        return if (tasks.isNotEmpty()) tasks[0].topActivity?.className else null
    }

    override fun getRootAccessibilityNode(): AccessibilityNodeInfo? {
        return accessibilityService?.rootInActiveWindow
    }

    override fun getWindowRootForPackage(packageName: String): AccessibilityNodeInfo? {
        // 如果需要跨窗口查找，可以遍历所有窗口；这里简单返回 null
        return null
    }

    override fun click(x: Int, y: Int) {
        clickByCoordinate(x,y)
    }

    fun clickByCoordinate(x: Int, y: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val timeout = 1000L
                //先把对话窗设置成不可点击，以便能点击到对话窗下层的控件
                withContext(Dispatchers.IO) {
                    val builder = ProcessBuilder()
                    val order = listOf("input", "tap", x.toString(), y.toString())
                    val startTime = System.currentTimeMillis()
                    builder.command(order).start().waitFor(timeout, TimeUnit.MILLISECONDS).apply {
                        Log.d(
                            "MyGuiEnvironment",
                            "clickByCoordinate: input tap, x= $x, y= $y result:$this time:${System.currentTimeMillis() - startTime}"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d("MyGuiEnvironment", "clickByCoordinate: input tap, x= $x, y= $y")
        }
    }

    override fun longClick(x: Int, y: Int) {
        if (accessibilityService != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = GestureDescription.Builder()
            val path = Path()
            path.moveTo(x.toFloat(), y.toFloat())
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, 500)) // 500ms 长按
            accessibilityService.dispatchGesture(builder.build(), null, null)
        }
        // 备选略
    }

    override fun inputText(text: String, x: Int?, y: Int?) {
        x?.let { y?.let { click(it, y) } }
        // 通过 AccessibilityNodeInfo 设置文本
        val root = accessibilityService?.rootInActiveWindow
        if (root != null) {
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            if (focused != null) {
                val args = android.os.Bundle()
                args.putCharSequence(
                    android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    text
                )
                focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                return
            }
        }
        // 备选：使用剪贴板粘贴
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        // 尝试粘贴（需要无障碍焦点）
        root?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)?.performAction(AccessibilityNodeInfo.ACTION_PASTE)
    }

    override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int) {
        SendKeyUtils.simulateSwipeEvent(
            arrayOf(
                "${x1}",
                "${y1}",
                "${x2}",
                "${y2}",
            )
        )
    }

    override fun takeScreenshot(): Bitmap? {
        // 必须使用 MediaProjection 或系统 API，此处返回 null 表示未实现
        return null
    }

    override fun takeScreenshotBase64(): String {
        // 必须实现截图，否则任务无法进行
        // 请参考 MediaProjection 方案实现
        return  ScreenUtils.getShotJpegBase64()
    }

    override fun getScreenWidth(): Int = context.resources.displayMetrics.widthPixels
    override fun getScreenHeight(): Int = context.resources.displayMetrics.heightPixels

    override fun speak(text: String) {
        // 使用 TTS 或 Toast
//        android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun showToast(text: String) {
        android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        }
    }

    override fun forceStopApp(packageName: String) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
//        am.forceStopPackage(packageName)
    }

    override fun isVirtualDisplayRunning(): Boolean = false
    override fun getVirtualDisplayId(): Int? = null
    override fun switchToVirtualDisplay(packageName: String) {}
    override fun exitVirtualDisplay() {}

    override fun isAppInForeground(packageName: String): Boolean = getTopPackage() == packageName

    override fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    override fun getDeviceModel(): String = Build.MODEL
    override fun getOsVersion(): String = Build.VERSION.INCREMENTAL
    override fun getTimeZoneId(): String = java.util.TimeZone.getDefault().id

    override fun getInstalledApps(): List<Map<String, String>> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.map { app ->
            mapOf(
                "app_name" to pm.getApplicationLabel(app).toString(),
                "package_name" to app.packageName
            )
        }
    }
}