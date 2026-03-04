package com.example.navigationlab.deeplink

import com.example.navigationlab.contracts.DeeplinkOutcome

/**
 * Deterministic deeplink chain simulator used by F-family lab scenarios.
 *
 * Resolution order:
 * 1) host/channel preconditions
 * 2) manager chain in registration order
 * 3) fallback route
 */
class DeeplinkSimulator(
    private val managers: List<DeeplinkManager>,
    private val fallbackRoute: String = DEFAULT_FALLBACK_ROUTE,
) {

    fun dispatch(request: DeeplinkRequest): DeeplinkDispatchResult {
        if (!request.hostReady) {
            return fallbackResult(
                request = request,
                chainOutcome = DeeplinkOutcome.IGNORED,
                consumedBy = null,
                reason = FallbackReason.HOST_NOT_READY,
                visitedManagers = emptyList(),
            )
        }
        if (request.sendToChannelActive) {
            return fallbackResult(
                request = request,
                chainOutcome = DeeplinkOutcome.BLOCKED,
                consumedBy = null,
                reason = FallbackReason.CHANNEL_ACTIVE,
                visitedManagers = emptyList(),
            )
        }

        val visitedManagers = mutableListOf<String>()
        for (manager in managers) {
            visitedManagers += manager.name
            when (val decision = manager.handle(request)) {
                is ManagerDecision.Handled -> {
                    val destination = decision.route
                    if (destination != null) {
                        return DeeplinkDispatchResult(
                            outcome = DeeplinkOutcome.HANDLED,
                            chainOutcome = DeeplinkOutcome.HANDLED,
                            consumedBy = manager.name,
                            route = destination,
                            fallbackReason = null,
                            source = request.source,
                            restoredAfterProcessDeath = request.restoredAfterProcessDeath,
                            visitedManagers = visitedManagers.toList(),
                        )
                    }
                    return fallbackResult(
                        request = request,
                        chainOutcome = DeeplinkOutcome.HANDLED,
                        consumedBy = manager.name,
                        reason = FallbackReason.SUPPRESSED_NO_NAVIGATION,
                        visitedManagers = visitedManagers,
                    )
                }

                ManagerDecision.Blocked -> {
                    return fallbackResult(
                        request = request,
                        chainOutcome = DeeplinkOutcome.BLOCKED,
                        consumedBy = manager.name,
                        reason = FallbackReason.BLOCKED,
                        visitedManagers = visitedManagers,
                    )
                }

                is ManagerDecision.Fallback -> {
                    return fallbackResult(
                        request = request,
                        chainOutcome = DeeplinkOutcome.FALLBACK,
                        consumedBy = manager.name,
                        reason = FallbackReason.MANAGER_REQUESTED,
                        visitedManagers = visitedManagers,
                        routeOverride = decision.route,
                    )
                }

                ManagerDecision.Ignored -> Unit
            }
        }

        return fallbackResult(
            request = request,
            chainOutcome = DeeplinkOutcome.IGNORED,
            consumedBy = null,
            reason = FallbackReason.UNKNOWN_PATH,
            visitedManagers = visitedManagers,
        )
    }

    private fun fallbackResult(
        request: DeeplinkRequest,
        chainOutcome: DeeplinkOutcome,
        consumedBy: String?,
        reason: FallbackReason,
        visitedManagers: List<String>,
        routeOverride: String? = null,
    ): DeeplinkDispatchResult = DeeplinkDispatchResult(
        outcome = DeeplinkOutcome.FALLBACK,
        chainOutcome = chainOutcome,
        consumedBy = consumedBy,
        route = routeOverride ?: fallbackRoute,
        fallbackReason = reason,
        source = request.source,
        restoredAfterProcessDeath = request.restoredAfterProcessDeath,
        visitedManagers = visitedManagers.toList(),
    )

    companion object {
        const val DEFAULT_FALLBACK_ROUTE: String = "menu/home"

        /** Default manager ordering used by F-family baseline scenarios. */
        fun defaultManagers(): List<DeeplinkManager> = listOf(
            PrefixDeeplinkManager(
                name = "SpaceManager",
                prefix = "/space/",
                routeRoot = "space",
            ),
            PrefixDeeplinkManager(
                name = "ProfileManager",
                prefix = "/profile/",
                routeRoot = "profile",
            ),
        )

        fun default(
            managers: List<DeeplinkManager> = defaultManagers(),
            fallbackRoute: String = DEFAULT_FALLBACK_ROUTE,
        ): DeeplinkSimulator = DeeplinkSimulator(
            managers = managers,
            fallbackRoute = fallbackRoute,
        )
    }
}

/** Input payload for deterministic deeplink simulation. */
data class DeeplinkRequest(
    val path: String,
    val source: DeeplinkSource = DeeplinkSource.INTENT,
    val hostReady: Boolean = true,
    val sendToChannelActive: Boolean = false,
    val restoredAfterProcessDeath: Boolean = false,
)

/** Origin metadata for source-attribution scenarios (F08). */
enum class DeeplinkSource {
    INTENT,
    INTERNAL,
}

/** Normalized simulation output consumed by host instrumentation and tests. */
data class DeeplinkDispatchResult(
    val outcome: DeeplinkOutcome,
    val chainOutcome: DeeplinkOutcome,
    val consumedBy: String?,
    val route: String?,
    val fallbackReason: FallbackReason?,
    val source: DeeplinkSource,
    val restoredAfterProcessDeath: Boolean,
    val visitedManagers: List<String>,
)

/** Why the simulator resolved to fallback route. */
enum class FallbackReason {
    HOST_NOT_READY,
    CHANNEL_ACTIVE,
    SUPPRESSED_NO_NAVIGATION,
    BLOCKED,
    UNKNOWN_PATH,
    MANAGER_REQUESTED,
}

/** Chain handler contract evaluated in deterministic order. */
interface DeeplinkManager {
    val name: String
    fun handle(request: DeeplinkRequest): ManagerDecision
}

/** Decision returned by a [DeeplinkManager]. */
sealed interface ManagerDecision {
    data class Handled(val route: String?) : ManagerDecision
    data object Blocked : ManagerDecision
    data object Ignored : ManagerDecision
    data class Fallback(val route: String? = null) : ManagerDecision
}

/** Prefix-based manager useful for synthetic route inventory scenarios. */
class PrefixDeeplinkManager(
    override val name: String,
    private val prefix: String,
    private val routeRoot: String,
) : DeeplinkManager {
    override fun handle(request: DeeplinkRequest): ManagerDecision {
        if (!request.path.startsWith(prefix)) return ManagerDecision.Ignored
        val leaf = request.path.removePrefix(prefix).trim('/')
        val route = if (leaf.isEmpty()) routeRoot else "$routeRoot/$leaf"
        return ManagerDecision.Handled(route = route)
    }
}
