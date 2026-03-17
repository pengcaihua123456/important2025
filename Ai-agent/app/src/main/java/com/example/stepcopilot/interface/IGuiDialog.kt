package com.example.stepcopilot.`interface`

import com.example.stepcopilot.core.GuiFactory

/**
 * GUI 对话框控制接口，用于向用户反馈执行状态。
 */
interface IGuiDialog {
    fun onTaskStart()
    fun onTaskComplete(reason: String)
    fun onTaskFail(reason: String)
    fun onStepHint(hint: String)
    fun onListenUserBack(reasoning: String, text: String?)
    fun setState(state: GuiFactory.State)
}