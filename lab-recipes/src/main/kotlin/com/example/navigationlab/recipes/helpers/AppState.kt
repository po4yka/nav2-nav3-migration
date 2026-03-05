package com.example.navigationlab.recipes.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.recipes.helpers.TopLevelDestinationBehavior.HIDE
import com.example.navigationlab.recipes.helpers.TopLevelDestinationBehavior.SAME_AS_PARENT
import com.example.navigationlab.recipes.keys.TabAlphaDetail
import com.example.navigationlab.recipes.keys.TabBetaDetail

/**
 * Centralized app state coordinating tab switching, navigation, back handling,
 * and bottom bar visibility. Ported from screentransitionsample's AppState.
 */
@Stable
internal class AppState(
    private val coreData: AppStateCoreData,
    val navigationState: NavigationState,
) {
    companion object {
        private const val HOST = "AppState"
        private val DEFAULT_DESTINATION = TopLevelDestination.ALPHA

        internal val TOP_LEVEL_NAVIGATION_BEHAVIOR_MAP: Map<kotlin.reflect.KClass<out NavKey>, TopLevelDestinationBehavior> = mapOf(
            TabAlphaDetail::class to HIDE,
            TabBetaDetail::class to SAME_AS_PARENT,
        )
    }

    val topLevelDestinations: List<TopLevelDestination> =
        TopLevelDestination.getAvailableDestinations()

    val currentTopLevelDestination get() = coreData.currentTopLevelDestination

    var shouldShowNavigation by mutableStateOf(true)
        private set

    init {
        if (coreData.currentTopLevelDestination == TopLevelDestination.UNKNOWN) {
            coreData.currentTopLevelDestination = DEFAULT_DESTINATION
        }
        coreData.topLevelDestinationBackQueue.add(currentTopLevelDestination)
    }

    fun onSelectTopLevelDestination(destination: TopLevelDestination) {
        val from = coreData.currentTopLevelDestination.name
        navigationState.topLevelRoute = destination.startRoute
        coreData.topLevelDestinationBackQueue.add(destination)
        coreData.currentTopLevelDestination = destination
        NavLogger.tabSwitch(HOST, from, destination.name)
        showNavigation()
    }

    fun navigate(route: NavKey) {
        navigationState.backStacks[navigationState.topLevelRoute]?.add(route)
        val depth = navigationState.backStacks[navigationState.topLevelRoute]?.size ?: 0
        NavLogger.push(HOST, route::class.simpleName ?: "?", depth)
        updateNavigationVisibility(route)
    }

    fun onBack(finishActivity: () -> Unit) {
        val currentStack = navigationState.backStacks[navigationState.topLevelRoute]
        val isAtRoot = (currentStack?.size ?: 0) <= 1

        if (isAtRoot) {
            coreData.topLevelDestinationBackQueue.remove()
            coreData.topLevelDestinationBackQueue.element()?.let { dest ->
                NavLogger.back(HOST, "switched tab to ${dest.name}", 1)
                navigationState.topLevelRoute = dest.startRoute
                coreData.currentTopLevelDestination = dest
                showNavigation()
            } ?: run {
                NavLogger.back(HOST, "finish activity", 0)
                finishActivity()
            }
        } else {
            val from = currentStack?.lastOrNull()?.let { it::class.simpleName } ?: "?"
            currentStack?.removeLastOrNull()
            NavLogger.back(HOST, from, currentStack?.size ?: 0)
            val currentRoute = currentStack?.lastOrNull()
            if (currentRoute != null) updateNavigationVisibility(currentRoute)
            else showNavigation()
        }
    }

    private fun updateNavigationVisibility(route: NavKey) {
        val visible = when (TOP_LEVEL_NAVIGATION_BEHAVIOR_MAP[route::class]) {
            HIDE -> { hideNavigation(); false }
            SAME_AS_PARENT -> { return }
            else -> { showNavigation(); true }
        }
        NavLogger.visibility(HOST, "bottomBar", visible)
    }

    private fun showNavigation() {
        shouldShowNavigation = true
    }

    private fun hideNavigation() {
        shouldShowNavigation = false
    }
}

@Stable
internal class AppStateCoreData(
    currentTopLevelDestination: TopLevelDestination = TopLevelDestination.UNKNOWN,
    val topLevelDestinationBackQueue: LifoUniqueQueue<TopLevelDestination> = LifoUniqueQueue(),
) {
    var currentTopLevelDestination: TopLevelDestination by mutableStateOf(currentTopLevelDestination)

    companion object {
        private const val KEY_CURRENT = "current_top_level_destination"
        private const val KEY_QUEUE = "top_level_destination_back_queue"

        @Suppress("UNCHECKED_CAST")
        val Saver = mapSaver(
            save = {
                mapOf(
                    KEY_CURRENT to it.currentTopLevelDestination,
                    KEY_QUEUE to it.topLevelDestinationBackQueue.toSet(),
                )
            },
            restore = {
                AppStateCoreData(
                    currentTopLevelDestination = it[KEY_CURRENT] as TopLevelDestination,
                    topLevelDestinationBackQueue = LifoUniqueQueue(
                        it[KEY_QUEUE] as Set<TopLevelDestination>,
                    ),
                )
            },
        )
    }
}

@Composable
internal fun rememberAppState(): AppState {
    val coreData = rememberSaveable(saver = AppStateCoreData.Saver) {
        AppStateCoreData()
    }
    val topLevelRoutes = setOf<NavKey>(
        TopLevelDestination.ALPHA.startRoute,
        TopLevelDestination.BETA.startRoute,
        TopLevelDestination.GAMMA.startRoute,
    )
    val navigationState = rememberNavigationState(
        startRoute = coreData.currentTopLevelDestination.startRoute,
        topLevelRoutes = topLevelRoutes,
    )
    return remember(coreData, navigationState) {
        AppState(coreData = coreData, navigationState = navigationState)
    }
}
