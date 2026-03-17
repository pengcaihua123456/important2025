package com.example.stepcopilot.core

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.stepcopilot.`interface`.GuiEnvironment
import com.example.stepcopilot.`interface`.IGuiDialog
import com.example.stepcopilot.util.GsonUtils
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask


/**
 * 阶跃 GUI Agent 通用客户端。
 * @param context Android Context
 * @param environment 环境抽象实现
 * @param apiKey 阶跃 API Key
 * @param guiDialog 对话框控制（可选）
 * @param modelName 模型名称，
 * @param taskType 任务类型，
 * @param maxRetryStepCount 单步最大重试次数
 * @param maxRetrySceneCount 场景最大重试次数
 * @param stepExceptionCount 视为异常的步数阈值
 * @param interruptPackages 需要拦截的包名列表（例如支付宝支付页）
 * @param customAppNameToPackage 自定义应用名称到包名的映射（用于唤醒指令）
 * @param onCommandIntercept 指令拦截回调，返回 true 表示拦截并由外部处理
 * @param coroutineScope 协程作用域，默认 MainScope
 */
class AgentClient(
    private val context: Context,
    private val environment: GuiEnvironment,
    private val apiKey: String,
    private val guiDialog: IGuiDialog? = null,
    private val modelName: String = "gwm-step-copilot",
    private val taskType: String = "general_9_action",
    private val maxRetryStepCount: Int = 8,
    private val maxRetrySceneCount: Int = 1,
    private val stepExceptionCount: Int = 3,
    private val interruptPackages: Set<String> = setOf("com.alipay.sdk.app.H5PayActivity"),
    private val customAppNameToPackage: Map<String, String> = emptyMap(),
    private val onCommandIntercept: ((String) -> Boolean)? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : IGuiAgent {

    private val TAG = "AgentClient"

    // 动作类型常量（与 API 定义一致）
    companion object {
        const val ACTION_TYPE_AWAKE = "Awake"
        const val ACTION_TYPE_CLICK = "Click"
        const val ACTION_TYPE_TYPE = "Type"
        const val ACTION_TYPE_POP = "Pop"
        const val ACTION_TYPE_LONGPRESS = "LongPress"
        const val ACTION_TYPE_SCROLL = "Scroll"
        const val ACTION_TYPE_WAIT = "Wait"
        const val ACTION_TYPE_COMPLETE = "Complete"
        const val ACTION_TYPE_ABORT = "Abort"
    }

    // HTTP 客户端（无日志拦截器）
    private val client: OkHttpClient = OkHttpClient.Builder()
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType: MediaType = "application/json".toMediaTypeOrNull()!!

    // 状态变量
    private var timer: Timer? = null
    private var speakerJob: Job? = null
    private var lastTaskFinish: Boolean = true
    private var mTaskName: String? = null
    private var taskAppPackage: String? = null
    private var mRunId: String? = null
    private var sessionId: String? = null
    private var isNeedFeedback: Boolean? = false
    private var mFeedbackText: String? = ""
    private var mLastReasoning: String? = ""
    private var startTimeMillis: Long = 0

    // 调试日志
    private var logStringBuffer: StringBuffer? = null
    private var costTimeStringBuffer: StringBuffer? = null
    private var executeIndex: Int = 0
    private var messages = mutableListOf<ArrayMap<String, Any?>>()
    private var isCancel: Boolean = false

    // 重试计数器
    private var retrySceneCount: Int = 0
    private var retryStepCount: Int = 0

    // 记录上一次动作，用于检测重复
    private var lastAction: String? = null

    override fun getRunTaskPackage(): String? = taskAppPackage

    override fun processAsrCommand(text: String) {
        // 外部拦截
        if (onCommandIntercept?.invoke(text) == true) {
            return
        }

        // 处理自定义应用名称唤醒
        customAppNameToPackage.entries.find { text.contains(it.key) }?.let { (name, pkg) ->
            environment.launchApp(pkg)
            environment.speak("好的")
            return
        }

        if (isNeedFeedback == true && GuiFactory.isProcessing) {
            coroutineScope.launch {
                askServer(feedbackText = text)
            }
        } else if (!GuiFactory.isProcessing) {
            cancelRequest()
            executeScene(text)
            // 如果需要通知 UI 状态变化，可以通过 guiDialog 或外部回调
        }
    }

    override fun executeScene(text: String) {
        // 检查是否启用（可由外部通过环境或配置判断，此处假设总是启用）
        // 超时计时
        timer?.cancel()
        timer = Timer()
        timer!!.schedule(timerTask {
            Log.d(TAG, "executeScene timeout")
            clearState()
            speakerJob?.cancel()
            speakerJob = null
            timer = null
        }, 120_000)

        clearState()
        GuiFactory.isProcessing = true
        isCancel = false
        startTimeMillis = System.currentTimeMillis()
        mTaskName = text

        // 构造创建会话的请求体
        val screenWidth = environment.getScreenWidth()
        val screenHeight = environment.getScreenHeight()
        val gson = Gson()
        val bodyContent = gson.toJson(hashMapOf<String, Any?>().apply {
            put("device", hashMapOf<String, Any>().apply {
                put("device_id", environment.getDeviceId())
                put("resolution", arrayOf(screenWidth, screenHeight))
                put("device_model", environment.getDeviceModel())
                put("os_version", environment.getOsVersion())
                put("platform", "Android")
                put("timezone", environment.getTimeZoneId())
            })
            put("applications", environment.getInstalledApps())
            put("user", "default_user") // 可由环境提供
            put("task", text)
            put("task_type", taskType)
            put("model_name", modelName)
            put("prompt_rewrite", "false")
        })

        val request = Request.Builder()
            .url("https://kapi.stepfun.com/v1/copilot/sessions")
            .post(RequestBody.create(mediaType, bodyContent))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        speakerJob = coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        Log.e(TAG, "create session failed: ${response.code}")
                        null
                    } else {
                        response.body?.string()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "create session exception", e)
                    null
                }
            }

            guiDialog?.onTaskStart()
            // 回到主屏幕
            environment.click(0, 0) // 简单返回 home，可能需要更精确的方法
            delay(1000)

            withContext(Dispatchers.Default) {
                if (result.isNullOrEmpty()) {
                    isNeedFeedback = false
                } else {
                    sessionId = JsonParser.parseString(result).asJsonObject.get("session_id")?.asString
                    logStringBuffer?.append("sessionId:$sessionId\n")
                    lastTaskFinish = true
                }
                executeStep(text)
            }
        }
    }

    private fun clearState() {
        GuiFactory.isProcessing = false
        messages.clear()
        taskAppPackage = null
        mFeedbackText = null
        isNeedFeedback = false
        retryStepCount = 0
        lastAction = null
        mLastReasoning = null
        speakerJob?.cancel()
        logStringBuffer = StringBuffer()
        executeIndex = 0
        mRunId = null
        timer?.cancel()
        timer = null
    }

    private var stepTimeMillis = 0L

    private suspend fun executeStep(taskName: String?) {
        if (isCancel) return

        costTimeStringBuffer = StringBuffer()
        stepTimeMillis = System.currentTimeMillis()
        executeIndex++
        costTimeStringBuffer?.append("execute step $executeIndex cost time\n")

        // 检查是否需要中断（例如支付页面）
        val topActivity = environment.getTopActivity()
        if (topActivity in interruptPackages) {
            Log.d(TAG, "Interrupt package detected: $topActivity")
            finishWithMessage("场景执行完成，请手动完成支付")
            return
        }

        logStringBuffer?.append("step: $executeIndex\n")

        // 截图
        val imageBase64 = environment.takeScreenshotBase64()

        Log.d(TAG, "executeStep imageBase64的值是多少:$imageBase64")


        costTimeStringBuffer?.append("screen shot: ${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000}  sleep 2s\n")

        guiDialog?.onStepHint(mLastReasoning ?: "")

        // 准备请求体
        val gson = Gson()
        val bodyContent = gson.toJson(hashMapOf<String, Any?>().apply {
            put("session_id", sessionId)
            if (!mRunId.isNullOrEmpty()) {
                put("previous_run", ArrayMap<String, Any>().apply {
                    put("run_id", mRunId)
                    put("action_results", messages)
                })
            }
            put("regenerate", false)
            put("task_finish", lastTaskFinish)
            if (lastTaskFinish) {
                lastTaskFinish = false
            }

            val observation = hashMapOf<String, Any>()
            observation["package_name"] = environment.getTopPackage() ?: ""
            if (mFeedbackText?.isNotEmpty() == true) {
                observation["query"] = mFeedbackText!!
                mFeedbackText = null
            }
            put("observation", observation.apply {
                put("screenshot", hashMapOf<String, Any>().apply {
                    put("type", "image_url")
                    put("image_url", ArrayMap<String, Any>().apply {
                        put("url", "data:image/jpeg;base64,$imageBase64")
                    })
                })
            })
        })

        val request = Request.Builder()
            .url("https://kapi.stepfun.com/v1/copilot/automate")
            .post(RequestBody.create(mediaType, bodyContent))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        val responseStr = withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e(TAG, "automate request failed: ${response.code}")
                    delay(2000)
                    executeStep(taskName)
                    null
                } else {
                    response.body?.string()
                }
            } catch (e: Exception) {
                Log.e(TAG, "automate exception", e)
                null
            }
        }

        Log.d(TAG, "speak enter success. $responseStr")

        parseAndExecuteActions(taskName, responseStr)
    }

    private suspend fun parseAndExecuteActions(taskName: String?, responseStr: String?) {




        withContext(Dispatchers.Default) {
            costTimeStringBuffer?.append("parser data: ${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000}\n")
            isNeedFeedback = false

            if (responseStr.isNullOrEmpty()) {
                Log.e(TAG, "response is null or empty")
                return@withContext
            }

            val gson = Gson()
            val bean = try {
                gson.fromJson(responseStr, AgentAutomateBean::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "parse json error", e)
                return@withContext
            }

            bean.run_id?.let { mRunId = it }
            logStringBuffer?.append("\trunid:${mRunId ?: ""}\n")

            val action = bean.actions?.firstOrNull() ?: run {
                Log.e(TAG, "no action in response")
                return@withContext
            }

            val resultMap = ArrayMap<String, Any?>()
            resultMap["content"] = GsonUtils.toString(bean.actions)
            resultMap["is_error"] = retryStepCount >= stepExceptionCount
            messages.add(resultMap)

            withContext(Dispatchers.Main) {
                val state = when (action.action_type) {
                    ACTION_TYPE_POP -> GuiFactory.State.GUI_ACTION_TYPE_LISTENER
                    ACTION_TYPE_COMPLETE -> GuiFactory.State.GUI_ACTION_TYPE_COMPLETE
                    else -> GuiFactory.State.GUI_ACTION_TYPE_EXECUTE
                }
                guiDialog?.setState(state)
                guiDialog?.onStepHint(action.reasoning ?: "")
                costTimeStringBuffer?.append("show next mean: ${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000} sleep 0.5s\n")
            }

            mLastReasoning = action.reasoning

            // 执行动作
            when (action.action_type) {
                ACTION_TYPE_AWAKE -> handleAwake(taskName, action)
                ACTION_TYPE_POP -> handlePop(action)
                ACTION_TYPE_TYPE -> handleType(taskName, action)
                ACTION_TYPE_WAIT -> handleWait(taskName, action)
                ACTION_TYPE_CLICK -> handleClick(taskName, action)
                ACTION_TYPE_SCROLL -> handleScroll(taskName, action)
                ACTION_TYPE_LONGPRESS -> handleLongPress(taskName, action)
                ACTION_TYPE_ABORT -> handleAbort(action)
                ACTION_TYPE_COMPLETE -> handleComplete(action)
                else -> {
                    Log.w(TAG, "Unknown action type: ${action.action_type}")
                    executeStep(taskName)
                }
            }
        }
    }

    // ---------- 动作处理函数 ----------
    private suspend fun handleAwake(taskName: String?, action: AgentAutomateBean.ActionBean) {
        val pkg = action.args?.package_name ?: return
        logAction("awake", "package_name:$pkg", action.reasoning)
        if (!checkRetry("awake-$pkg-${action.reasoning}", taskName)) return

        taskAppPackage = pkg
        if (!environment.isAppInstalled(pkg)) {
            guiDialog?.onTaskComplete("未找到目标应用")
            environment.speak("请先安装该应用")
            delay(3000)
            clearState()
            return
        }

        // 强制停止并启动应用
        environment.forceStopApp(pkg)
        environment.launchApp(pkg)
        delay(1000)

        costTimeStringBuffer?.append("awake: ${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000} sleep 1s\n")
        executeStep(taskName)
    }

    private suspend fun handlePop(action: AgentAutomateBean.ActionBean) {
        val text = action.args?.text
        logAction("pop", "text:$text", action.reasoning)
        if (!checkRetry("pop-${action.reasoning}", null)) return

        // 退出虚拟屏（如果有）
        exitVirtualIfNeeded()

        isNeedFeedback = true
        guiDialog?.onListenUserBack(action.reasoning ?: "", text)
        delay(1000)
        costTimeStringBuffer?.append("pop: ${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000} sleep 1s\n")
        delay(1500)
        // 等待用户输入，不自动执行下一步
    }

    private suspend fun handleType(taskName: String?, action: AgentAutomateBean.ActionBean) {
        val text = action.args?.text ?: return
        val point = action.args?.point
        logAction("type", "text:$text point:${point?.toList()}", action.reasoning)
        if (!checkRetry("type-$text-${action.reasoning}", taskName)) return

        delay(500)
        costTimeStringBuffer?.append("type: ")

        // 如果在虚拟屏且不是前台应用，先切回主屏？根据需求决定，这里简单判断
        if (environment.isVirtualDisplayRunning() && !environment.isAppInForeground(taskAppPackage ?: "")) {
            exitVirtualIfNeeded()
            executeStep(taskName)
            return
        }

        // 尝试通过无障碍输入
        val root = environment.getRootAccessibilityNode()
        if (root != null) {
            // 查找输入框并输入
            val editNode = findEditText(root)
            if (editNode != null) {
                // 使用无障碍设置文本
                setTextWithAccessibility(editNode, text)
                delay(500)
            } else {
                // 如果找不到，降级为点击坐标后输入
                val x = point?.getOrNull(0) ?: -1
                val y = point?.getOrNull(1) ?: -1
                if (x != -1 && y != -1) {
                    environment.click(x, y)
                    delay(300)
                }
                environment.inputText(text)
                delay(500)
            }
        } else {
            // 无障碍不可用，直接点击坐标后输入
            val x = point?.getOrNull(0) ?: -1
            val y = point?.getOrNull(1) ?: -1
            if (x != -1 && y != -1) {
                environment.click(x, y)
                delay(300)
            }
            environment.inputText(text)
            delay(500)
        }

        costTimeStringBuffer?.append("end sleep 0.5s. ${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000}\n")
        executeStep(taskName)
    }

    private suspend fun handleWait(taskName: String?, action: AgentAutomateBean.ActionBean) {
        val duration = action.args?.duration ?: 5
        logAction("wait", "duration:$duration", action.reasoning)
        if (!checkRetry("wait-${action.reasoning}", taskName)) return

        delay((duration * 1000).toLong())
        costTimeStringBuffer?.append("wait: ${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000} sleep ${duration * 1000}ms\n")
        executeStep(taskName)
    }

    private suspend fun handleClick(taskName: String?, action: AgentAutomateBean.ActionBean) {
        val point = action.args?.point ?: return
        if (point.size < 2) return
        val x = point[0]
        val y = point[1]
        logAction("click", "point:($x,$y)", action.reasoning)
        if (!checkRetry("click-${action.reasoning}", taskName)) return

        costTimeStringBuffer?.append("click: ")

        // 检查推理中是否包含支付等关键词，可根据需要处理
        if (action.reasoning?.contains("付款") == true || action.reasoning?.contains("支付") == true) {
            finishWithMessage("场景执行完成，请手动完成支付")
            return
        }

        // 状态栏补偿（可由环境内部处理，这里简单做）
        val adjustedY = if (y <= 116) 116 else y
        environment.click(x, adjustedY)
        delay(1000)

        // 再次检查是否进入中断页面
        if (environment.getTopActivity() in interruptPackages) {
            finishWithMessage("场景执行完成，请手动完成支付")
            return
        }

        costTimeStringBuffer?.append("${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000} \n")
        executeStep(taskName)
    }

    private suspend fun handleScroll(taskName: String?, action: AgentAutomateBean.ActionBean) {
        val coords = action.args?.coordinates ?: return
        if (coords.size < 4) return
        logAction("scroll", "coords:${coords.toList()}", action.reasoning)
        // 滑动不检查重试，以免误判
        environment.swipe(coords[0], coords[1], coords[2], coords[3])
        delay(1000)
        costTimeStringBuffer?.append("scroll: ${(System.currentTimeMillis() - stepTimeMillis) * 1.0f / 1000} sleep 1s \n")
        executeStep(taskName)
    }

    private suspend fun handleLongPress(taskName: String?, action: AgentAutomateBean.ActionBean) {
        val point = action.args?.point ?: return
        if (point.size < 2) return
        logAction("longPress", "point:(${point[0]},${point[1]})", action.reasoning)
        // 长按实现（环境需要支持）
        environment.longClick(point[0], point[1])
        delay(1000)
        executeStep(taskName)
    }

    private suspend fun handleAbort(action: AgentAutomateBean.ActionBean) {
        logAction("abort", "", action.reasoning)
        if (action.reasoning == "登录隐私（敏感弹窗）") {
            exitVirtualIfNeeded()
            environment.speak("请确认隐私权限")
            delay(5000)
            // 可能继续执行，但这里简单处理
        }
        // 一般终止
        guiDialog?.onTaskFail(action.reasoning ?: "任务异常终止")
        clearState()
    }

    private suspend fun handleComplete(action: AgentAutomateBean.ActionBean) {
        logAction("complete", "", action.reasoning)
        exitVirtualIfNeeded()
        val message = if (action.reasoning?.contains("支付") == true || action.reasoning?.contains("付款") == true) {
            "场景执行完成，请手动完成支付"
        } else {
            "当前任务已经执行完成"
        }
        environment.speak(message)
        guiDialog?.onTaskComplete(mLastReasoning ?: "")
        delay(3000)
        clearState()
    }

    // ---------- 辅助方法 ----------
    private fun logAction(type: String, detail: String, reasoning: String?) {
        logStringBuffer?.append("\t$type\n")
        logStringBuffer?.append("\t$detail\n")
        logStringBuffer?.append("\treasoning:${reasoning}\n")
    }

    private suspend fun checkRetry(actionKey: String, taskName: String?): Boolean {
        if (actionKey == lastAction) {
            retryStepCount++
        } else {
            retryStepCount = 0
        }
        lastAction = actionKey

        if (retryStepCount >= maxRetryStepCount) {
            retrySceneCount++
            if (retrySceneCount >= maxRetrySceneCount) {
                environment.speak("我好像迷路了，请稍后再试")
                clearState()
                guiDialog?.onTaskFail("任务执行失败")
                return false
            } else {
                // 重试整个场景
                mTaskName?.let { executeScene(it) } ?: taskName?.let { executeScene(it) }
                return false
            }
        }
        return true
    }

    private suspend fun exitVirtualIfNeeded() {
        if (environment.isVirtualDisplayRunning() && !environment.isAppInForeground(taskAppPackage ?: "")) {
            environment.exitVirtualDisplay()
            delay(1500)
        }
    }

    private suspend fun finishWithMessage(message: String) {
        environment.speak(message)
        guiDialog?.onTaskComplete(mLastReasoning ?: "")
        delay(3000)
        clearState()
    }

    private fun findEditText(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // 简单查找，可根据需要扩展
        return if (root.className == "android.widget.EditText" && root.isVisibleToUser) {
            root
        } else {
            for (i in 0 until root.childCount) {
                val child = root.getChild(i) ?: continue
                val found = findEditText(child)
                if (found != null) return found
            }
            null
        }
    }

    private fun setTextWithAccessibility(node: AccessibilityNodeInfo, text: String) {
        val arguments = android.os.Bundle()
        arguments.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    override suspend fun askServer(taskName: String?, feedbackText: String?, like: String?) {
        Log.d(TAG, "askServer: taskName=$taskName, feedbackText=$feedbackText, session_id=$sessionId")
        mFeedbackText = feedbackText

        val gson = Gson()
        val bodyContent = gson.toJson(hashMapOf<String, Any?>().apply {
            put("session_id", sessionId)
            put("rating", like ?: "")
            put("comment", feedbackText ?: "")
        })

        val request = Request.Builder()
            .url("https://kapi.stepfun.com/v1/copilot/feedbacks")
            .post(RequestBody.create(mediaType, bodyContent))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        val result = withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) response.body?.string() else null
            } catch (e: Exception) {
                null
            }
        }

        if (result == null) {
            Log.e(TAG, "askServer error")
            return
        }

        delay(2000)
        executeStep(taskName)
    }

    override fun cancelRequest() {
        Log.d(TAG, "cancelRequest")
        isCancel = true
        speakerJob?.cancel()
        clearState()
    }
}