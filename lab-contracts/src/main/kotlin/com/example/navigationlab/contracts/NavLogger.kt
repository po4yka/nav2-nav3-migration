package com.example.navigationlab.contracts

import android.util.Log

object NavLogger {
    private const val TAG = "NavRecipe"

    fun push(host: String, route: String, stackDepth: Int) {
        Log.d(TAG, "[PUSH] host=$host | route=$route | depth=$stackDepth")
    }

    fun pop(host: String, route: String, stackDepth: Int) {
        Log.d(TAG, "[POP] host=$host | route=$route | depth=$stackDepth")
    }

    fun back(host: String, from: String, stackDepth: Int) {
        Log.d(TAG, "[BACK] host=$host | from=$from | depth=$stackDepth")
    }

    fun tabSwitch(host: String, from: String, to: String) {
        Log.d(TAG, "[TAB_SWITCH] host=$host | from=$from | to=$to")
    }

    fun deepLink(host: String, action: String, params: Map<String, String>) {
        val paramStr = params.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Log.d(TAG, "[DEEPLINK] host=$host | action=$action | params=[$paramStr]")
    }

    fun redirect(host: String, from: String, to: String, reason: String) {
        Log.d(TAG, "[REDIRECT] host=$host | from=$from | to=$to | reason=$reason")
    }

    fun result(host: String, type: String, key: String) {
        Log.d(TAG, "[RESULT] host=$host | type=$type | key=$key")
    }

    fun visibility(host: String, component: String, visible: Boolean) {
        Log.d(TAG, "[VISIBILITY] host=$host | component=$component | visible=$visible")
    }
}
