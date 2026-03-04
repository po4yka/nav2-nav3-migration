package com.example.navigationlab.recipes.helpers

import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.receiveAsFlow

class ResultEventBus {

    val channelMap = mutableStateMapOf<String, Channel<Any?>>()

    inline fun <reified T> getResultFlow(resultKey: String = T::class.toString()) =
        channelMap[resultKey]?.receiveAsFlow()

    inline fun <reified T> sendResult(resultKey: String = T::class.toString(), result: T) {
        if (!channelMap.contains(resultKey)) {
            channelMap[resultKey] = Channel(
                capacity = BUFFERED,
                onBufferOverflow = BufferOverflow.SUSPEND,
            )
        }
        channelMap[resultKey]?.trySend(result)
    }

    inline fun <reified T> removeResult(resultKey: String = T::class.toString()) {
        channelMap.remove(resultKey)
    }
}
