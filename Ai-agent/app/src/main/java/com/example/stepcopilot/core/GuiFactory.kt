package com.example.stepcopilot.core

/**
 * GUI 执行状态（可根据需要自定义）
 */
object GuiFactory {
    enum class State {
        GUI_ACTION_TYPE_LISTENER,
        GUI_ACTION_TYPE_COMPLETE,
        GUI_ACTION_TYPE_EXECUTE
    }
    var isProcessing: Boolean = false
}