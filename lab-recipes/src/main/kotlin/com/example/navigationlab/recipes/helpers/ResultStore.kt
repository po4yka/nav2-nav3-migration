package com.example.navigationlab.recipes.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberResultStore(): ResultStore {
    return rememberSaveable(saver = ResultStoreSaver()) {
        ResultStore()
    }
}

class ResultStore {

    val resultStateMap = mutableStateMapOf<String, MutableState<Any?>>()

    inline fun <reified T> getResultState(resultKey: String = T::class.toString()) =
        resultStateMap[resultKey]?.value as T

    inline fun <reified T> setResult(resultKey: String = T::class.toString(), result: T) {
        resultStateMap[resultKey] = mutableStateOf(result)
    }

    inline fun <reified T> removeResult(resultKey: String = T::class.toString()) {
        resultStateMap.remove(resultKey)
    }
}

private fun ResultStoreSaver(): Saver<ResultStore, Map<String, Any?>> =
    Saver(
        save = { store ->
            store.resultStateMap.mapValues { (_, state) -> state.value }
        },
        restore = { savedValues ->
            ResultStore().apply {
                savedValues.forEach { (key, value) ->
                    resultStateMap[key] = mutableStateOf(value)
                }
            }
        },
    )
