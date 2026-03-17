package com.example.stepcopilot.core

import com.google.gson.Gson

class AgentAutomateBean {
    var session_id: String? = null
    val run_id: String? = null
    val actions: ArrayList<ActionBean>? = null
    val usage: UsageBean? = null
    val trace_id: String? = null

    class ActionBean() {
        var args: ArgsBean? = null
        var suggested_cooldown: Float? = null
        var reasoning: String? = null
        var action_type: String? = null

        class ArgsBean() {
            var point: ArrayList<Int>? = null
            var normalized_point: ArrayList<Float>? = null
            var coordinates: ArrayList<Int>? = null
            var text: String? = null
            var package_name: String? = null
            var duration: Int? = null
        }
    }

    class UsageBean() {
        var latency: Int? = null
        var download_elapsed: Int? = null

    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}