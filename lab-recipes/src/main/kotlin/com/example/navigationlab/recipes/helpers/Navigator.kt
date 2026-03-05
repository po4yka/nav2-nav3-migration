package com.example.navigationlab.recipes.helpers

import androidx.navigation3.runtime.NavKey
import com.example.navigationlab.contracts.NavLogger

class Navigator(val state: NavigationState) {

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            val from = state.topLevelRoute::class.simpleName ?: "?"
            state.topLevelRoute = route
            NavLogger.tabSwitch(HOST, from, route::class.simpleName ?: "?")
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
            val depth = state.backStacks[state.topLevelRoute]?.size ?: 0
            NavLogger.push(HOST, route::class.simpleName ?: "?", depth)
        }
    }

    fun goBack(onAtRoot: () -> Unit = {}) {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            if (state.topLevelRoute != state.startRoute) {
                NavLogger.tabSwitch(HOST, state.topLevelRoute::class.simpleName ?: "?", state.startRoute::class.simpleName ?: "?")
                state.topLevelRoute = state.startRoute
            } else {
                NavLogger.back(HOST, "root-exit", currentStack.size)
                onAtRoot()
            }
        } else {
            NavLogger.back(HOST, currentRoute::class.simpleName ?: "?", currentStack.size - 1)
            currentStack.removeLastOrNull()
        }
    }

    companion object {
        private const val HOST = "Navigator"
    }
}
