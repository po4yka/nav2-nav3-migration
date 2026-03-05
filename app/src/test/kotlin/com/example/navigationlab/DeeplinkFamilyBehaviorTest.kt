package com.example.navigationlab

import com.example.navigationlab.contracts.DeeplinkOutcome
import com.example.navigationlab.deeplink.DeeplinkDispatchResult
import com.example.navigationlab.deeplink.DeeplinkManager
import com.example.navigationlab.deeplink.DeeplinkRequest
import com.example.navigationlab.deeplink.DeeplinkSimulator
import com.example.navigationlab.deeplink.DeeplinkSource
import com.example.navigationlab.deeplink.FallbackReason
import com.example.navigationlab.deeplink.ManagerDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeeplinkFamilyBehaviorTest {

    @Test
    fun f01_firstMatchingManager_handlesAndNavigates() {
        val result = DeeplinkSimulator.default()
            .dispatch(DeeplinkRequest(path = "/space/project-alpha"))

        assertHandled(
            result = result,
            route = "space/project-alpha",
            consumedBy = "SpaceManager",
            visitedManagers = listOf("SpaceManager"),
        )
    }

    @Test
    fun f02_handledWithoutRoute_isRewrittenToSuppressionFallback() {
        val simulator = DeeplinkSimulator(
            managers = listOf(
                object : DeeplinkManager {
                    override val name: String = "SuppressionManager"
                    override fun handle(request: DeeplinkRequest): ManagerDecision =
                        ManagerDecision.Handled(route = null)
                },
            ),
        )

        val result = simulator.dispatch(DeeplinkRequest(path = "/space/suppressed"))

        assertFallback(
            result = result,
            chainOutcome = DeeplinkOutcome.HANDLED,
            reason = FallbackReason.SUPPRESSED_NO_NAVIGATION,
            consumedBy = "SuppressionManager",
            visitedManagers = listOf("SuppressionManager"),
        )
    }

    @Test
    fun f03_blockedManager_routesToFallbackWithBlockedOutcome() {
        val simulator = DeeplinkSimulator(
            managers = listOf(
                object : DeeplinkManager {
                    override val name: String = "GateManager"
                    override fun handle(request: DeeplinkRequest): ManagerDecision =
                        if (request.path.startsWith("/space/private")) {
                            ManagerDecision.Blocked
                        } else {
                            ManagerDecision.Ignored
                        }
                },
                object : DeeplinkManager {
                    override val name: String = "SpaceManager"
                    override fun handle(request: DeeplinkRequest): ManagerDecision =
                        ManagerDecision.Handled(route = "space/fallback")
                },
            ),
        )

        val result = simulator.dispatch(DeeplinkRequest(path = "/space/private-room"))

        assertFallback(
            result = result,
            chainOutcome = DeeplinkOutcome.BLOCKED,
            reason = FallbackReason.BLOCKED,
            consumedBy = "GateManager",
            visitedManagers = listOf("GateManager"),
        )
    }

    @Test
    fun f04_unknownPath_fallsBackWithUnknownPathReason() {
        val result = DeeplinkSimulator.default()
            .dispatch(DeeplinkRequest(path = "/unknown/feature-entry"))

        assertFallback(
            result = result,
            chainOutcome = DeeplinkOutcome.IGNORED,
            reason = FallbackReason.UNKNOWN_PATH,
            consumedBy = null,
            visitedManagers = listOf("SpaceManager", "ProfileManager"),
        )
    }

    @Test
    fun f05_hostNotReady_shortCircuitsToFallback() {
        val result = DeeplinkSimulator.default().dispatch(
            DeeplinkRequest(
                path = "/space/project-alpha",
                hostReady = false,
            ),
        )

        assertFallback(
            result = result,
            chainOutcome = DeeplinkOutcome.IGNORED,
            reason = FallbackReason.HOST_NOT_READY,
            consumedBy = null,
            visitedManagers = emptyList(),
        )
    }

    @Test
    fun f06_channelActive_shortCircuitsToFallback() {
        val result = DeeplinkSimulator.default().dispatch(
            DeeplinkRequest(
                path = "/space/project-alpha",
                sendToChannelActive = true,
            ),
        )

        assertFallback(
            result = result,
            chainOutcome = DeeplinkOutcome.BLOCKED,
            reason = FallbackReason.CHANNEL_ACTIVE,
            consumedBy = null,
            visitedManagers = emptyList(),
        )
    }

    @Test
    fun f07_restoreReplay_preservesDeterministicDestinationAndMetadata() {
        val simulator = DeeplinkSimulator.default()
        val request = DeeplinkRequest(
            path = "/profile/user-42",
            restoredAfterProcessDeath = true,
        )

        val first = simulator.dispatch(request)
        val second = simulator.dispatch(request)

        assertHandled(
            result = first,
            route = "profile/user-42",
            consumedBy = "ProfileManager",
            visitedManagers = listOf("SpaceManager", "ProfileManager"),
        )
        assertEquals(first.route, second.route)
        assertEquals(first.outcome, second.outcome)
        assertEquals(first.chainOutcome, second.chainOutcome)
        assertEquals(true, first.restoredAfterProcessDeath)
        assertEquals(true, second.restoredAfterProcessDeath)
    }

    @Test
    fun f08_sourceAttribution_preservesOriginWithEquivalentDestination() {
        val simulator = DeeplinkSimulator.default()
        val intentResult = simulator.dispatch(
            DeeplinkRequest(
                path = "/space/project-alpha",
                source = DeeplinkSource.INTENT,
            ),
        )
        val internalResult = simulator.dispatch(
            DeeplinkRequest(
                path = "/space/project-alpha",
                source = DeeplinkSource.INTERNAL,
            ),
        )

        assertEquals(intentResult.route, internalResult.route)
        assertEquals(intentResult.outcome, internalResult.outcome)
        assertEquals(intentResult.chainOutcome, internalResult.chainOutcome)
        assertEquals(DeeplinkSource.INTENT, intentResult.source)
        assertEquals(DeeplinkSource.INTERNAL, internalResult.source)
    }

    private fun assertHandled(
        result: DeeplinkDispatchResult,
        route: String,
        consumedBy: String,
        visitedManagers: List<String>,
    ) {
        assertEquals(DeeplinkOutcome.HANDLED, result.outcome)
        assertEquals(DeeplinkOutcome.HANDLED, result.chainOutcome)
        assertEquals(route, result.route)
        assertEquals(consumedBy, result.consumedBy)
        assertNull(result.fallbackReason)
        assertEquals(visitedManagers, result.visitedManagers)
    }

    private fun assertFallback(
        result: DeeplinkDispatchResult,
        chainOutcome: DeeplinkOutcome,
        reason: FallbackReason,
        consumedBy: String?,
        visitedManagers: List<String>,
    ) {
        assertEquals(DeeplinkOutcome.FALLBACK, result.outcome)
        assertEquals(chainOutcome, result.chainOutcome)
        assertEquals(DeeplinkSimulator.DEFAULT_FALLBACK_ROUTE, result.route)
        assertEquals(reason, result.fallbackReason)
        assertEquals(consumedBy, result.consumedBy)
        assertEquals(visitedManagers, result.visitedManagers)
    }
}
