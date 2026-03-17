package com.example.stepcopilot.core

interface IGuiAgent {
    fun getRunTaskPackage():String?
    fun processAsrCommand(text:String)
    fun executeScene(text:String)
    suspend fun askServer(taskName: String? = null, feedbackText: String? = null, like:String? = null)
    fun cancelRequest()
}
