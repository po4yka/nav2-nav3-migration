package com.example.navigationlab.recipes.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
inline fun <reified T> ResultEffect(
    resultEventBus: ResultEventBus,
    resultKey: String = T::class.toString(),
    crossinline onResult: suspend (T) -> Unit,
) {
    LaunchedEffect(resultKey, resultEventBus.channelMap[resultKey]) {
        resultEventBus.getResultFlow<T>(resultKey)?.collect { result ->
            @Suppress("UNCHECKED_CAST")
            onResult.invoke(result as T)
        }
    }
}
