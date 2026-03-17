package com.example.stepcopilot.util

import com.google.gson.Gson


// GsonUtils 简单实现（如果需要）
object GsonUtils {
    fun toString(obj: Any?): String = Gson().toJson(obj)
}