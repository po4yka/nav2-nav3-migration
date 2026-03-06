package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.DeeplinkOutcome
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.deeplink.DeeplinkDispatchResult
import com.example.navigationlab.deeplink.DeeplinkManager
import com.example.navigationlab.deeplink.DeeplinkRequest
import com.example.navigationlab.deeplink.DeeplinkSimulator
import com.example.navigationlab.deeplink.DeeplinkSource
import com.example.navigationlab.deeplink.FallbackReason
import com.example.navigationlab.deeplink.ManagerDecision
import com.example.navigationlab.host.fragment.fragments.ComposeNav2Fragment
import com.example.navigationlab.host.fragment.hosts.FragmentNav2HostActivity
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.hosts.Nav2LeafKey
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity
import com.example.navigationlab.host.nav3.hosts.Nav3ToNav2InteropActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeeplinkFamiliesBehaviorParityTest {

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

        val scenario = launchNav2Case(1)
        try {
            assertTrue(waitUntil(scenario) { it.currentRoute == Nav2HostActivity.ROUTE_HOME && it.backStackDepth == 1 })

            scenario.onActivity { activity ->
                applyDeeplinkToNav2Host(activity, result)
            }

            assertTrue(
                waitUntil(scenario) {
                    it.currentRoute == Nav2HostActivity.ROUTE_SCREEN_A && it.backStackDepth == 2
                },
            )
        } finally {
            scenario.close()
        }
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

        val scenario = launchNav2Case(2)
        try {
            assertTrue(waitUntil(scenario) { it.currentRoute == Nav2HostActivity.ROUTE_HOME && it.backStackDepth == 1 })

            scenario.onActivity { activity ->
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentRoute == Nav2HostActivity.ROUTE_SCREEN_A && it.backStackDepth == 2
                },
            )

            scenario.onActivity { activity ->
                applyDeeplinkToNav2Host(activity, result)
            }
            assertTrue(waitUntil(scenario) { it.currentRoute == Nav2HostActivity.ROUTE_HOME && it.backStackDepth == 1 })

            scenario.onActivity { activity ->
                assertFalse(activity.popBack())
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun f03_blockedManager_routesToFallbackWithoutMutatingHostStack() {
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

        val scenario = launchT6Nav2Case(3)
        try {
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == ComposeNav2Fragment.ROUTE_HOME && it.nav2BackStackDepth == 1
                },
            )
            var baselineDepth = 0
            scenario.onActivity { activity ->
                baselineDepth = activity.nav2BackStackDepth
                applyDeeplinkToFragmentNav2Host(activity, result)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == ComposeNav2Fragment.ROUTE_HOME &&
                        it.nav2BackStackDepth == baselineDepth
                },
            )
        } finally {
            scenario.close()
        }
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

        val scenario = launchNav3Case(4)
        try {
            assertTrue(waitUntil(scenario) { isTopNav3Key(it, Nav3Key.Home) && it.backStackDepth == 1 })

            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
            }
            assertTrue(waitUntil(scenario) { isTopNav3Key(it, Nav3Key.ScreenA) && it.backStackDepth == 2 })

            scenario.onActivity { activity ->
                applyDeeplinkToNav3Host(activity, result)
            }
            assertTrue(waitUntil(scenario) { isTopNav3Key(it, Nav3Key.Home) && it.backStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun f05_hostNotReady_shortCircuits_thenReplayedWhenReady() {
        val simulator = DeeplinkSimulator.default()
        val notReady = simulator.dispatch(
            DeeplinkRequest(
                path = "/space/project-alpha",
                hostReady = false,
            ),
        )
        val ready = simulator.dispatch(
            DeeplinkRequest(
                path = "/space/project-alpha",
                hostReady = true,
            ),
        )

        assertFallback(
            result = notReady,
            chainOutcome = DeeplinkOutcome.IGNORED,
            reason = FallbackReason.HOST_NOT_READY,
            consumedBy = null,
            visitedManagers = emptyList(),
        )
        assertHandled(
            result = ready,
            route = "space/project-alpha",
            consumedBy = "SpaceManager",
            visitedManagers = listOf("SpaceManager"),
        )

        val scenario = launchT6Nav2Case(5)
        try {
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == ComposeNav2Fragment.ROUTE_HOME && it.nav2BackStackDepth == 1
                },
            )
            var baselineDepth = 0
            scenario.onActivity { activity ->
                baselineDepth = activity.nav2BackStackDepth
                applyDeeplinkToFragmentNav2Host(activity, notReady)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == ComposeNav2Fragment.ROUTE_HOME &&
                        it.nav2BackStackDepth == baselineDepth
                },
            )

            scenario.onActivity { activity ->
                applyDeeplinkToFragmentNav2Host(activity, ready)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A &&
                        it.nav2BackStackDepth == baselineDepth + 1
                },
            )
        } finally {
            scenario.close()
        }
    }

    @Test
    fun f06_channelActive_shortCircuits_thenRecoversDeterministically() {
        val simulator = DeeplinkSimulator.default()
        val channelActive = simulator.dispatch(
            DeeplinkRequest(
                path = "/space/project-alpha",
                sendToChannelActive = true,
            ),
        )
        val channelDisabled = simulator.dispatch(
            DeeplinkRequest(
                path = "/space/project-alpha",
                sendToChannelActive = false,
            ),
        )

        assertFallback(
            result = channelActive,
            chainOutcome = DeeplinkOutcome.BLOCKED,
            reason = FallbackReason.CHANNEL_ACTIVE,
            consumedBy = null,
            visitedManagers = emptyList(),
        )
        assertHandled(
            result = channelDisabled,
            route = "space/project-alpha",
            consumedBy = "SpaceManager",
            visitedManagers = listOf("SpaceManager"),
        )

        val scenario = launchT6Nav2Case(6)
        try {
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == ComposeNav2Fragment.ROUTE_HOME && it.nav2BackStackDepth == 1
                },
            )
            var baselineDepth = 0
            scenario.onActivity { activity ->
                baselineDepth = activity.nav2BackStackDepth
                applyDeeplinkToFragmentNav2Host(activity, channelActive)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == ComposeNav2Fragment.ROUTE_HOME &&
                        it.nav2BackStackDepth == baselineDepth
                },
            )

            scenario.onActivity { activity ->
                applyDeeplinkToFragmentNav2Host(activity, channelDisabled)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A &&
                        it.nav2BackStackDepth == baselineDepth + 1
                },
            )
        } finally {
            scenario.close()
        }
    }

    @Test
    fun f07_restoreReplay_preservesDeterministicDestinationAndNoDuplicateLeafPush() {
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
        assertHandled(
            result = second,
            route = "profile/user-42",
            consumedBy = "ProfileManager",
            visitedManagers = listOf("SpaceManager", "ProfileManager"),
        )
        assertTrue(first.restoredAfterProcessDeath)
        assertTrue(second.restoredAfterProcessDeath)

        val scenario = launchT8Case(7)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.nav3BackStackDepth == 3 &&
                        it.nav2LeafBackStackDepth == 1 &&
                        it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME
                },
            )

            scenario.onActivity { activity ->
                applyDeeplinkToT8Leaf(activity, first, dedupe = true)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL &&
                        it.nav2LeafBackStackDepth == 2
                },
            )

            scenario.recreate()
            assertTrue(
                waitUntil(scenario) {
                    it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL &&
                        it.nav2LeafBackStackDepth >= 1 &&
                        it.nav3BackStackDepth == 3
                },
            )

            var depthBeforeReplay = 0
            scenario.onActivity { activity ->
                depthBeforeReplay = activity.nav2LeafBackStackDepth
                applyDeeplinkToT8Leaf(activity, second, dedupe = true)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL &&
                        it.nav2LeafBackStackDepth == depthBeforeReplay
                },
            )
        } finally {
            scenario.close()
        }
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

        assertHandled(
            result = intentResult,
            route = "space/project-alpha",
            consumedBy = "SpaceManager",
            visitedManagers = listOf("SpaceManager"),
        )
        assertHandled(
            result = internalResult,
            route = "space/project-alpha",
            consumedBy = "SpaceManager",
            visitedManagers = listOf("SpaceManager"),
        )
        assertEquals(DeeplinkSource.INTENT, intentResult.source)
        assertEquals(DeeplinkSource.INTERNAL, internalResult.source)
        assertEquals(intentResult.route, internalResult.route)

        val scenario = launchNav3Case(8)
        try {
            assertTrue(waitUntil(scenario) { isTopNav3Key(it, Nav3Key.Home) && it.backStackDepth == 1 })

            scenario.onActivity { activity ->
                applyDeeplinkToNav3Host(activity, intentResult, dedupe = true)
            }
            assertTrue(waitUntil(scenario) { isTopNav3Key(it, Nav3Key.ScreenA) && it.backStackDepth == 2 })

            var depthAfterFirst = 0
            scenario.onActivity { activity ->
                depthAfterFirst = activity.backStackDepth
                applyDeeplinkToNav3Host(activity, internalResult, dedupe = true)
            }
            assertTrue(
                waitUntil(scenario) {
                    isTopNav3Key(it, Nav3Key.ScreenA) && it.backStackDepth == depthAfterFirst
                },
            )
        } finally {
            scenario.close()
        }
    }

    private fun applyDeeplinkToNav2Host(activity: Nav2HostActivity, result: DeeplinkDispatchResult) {
        when (result.outcome) {
            DeeplinkOutcome.HANDLED -> {
                activity.navigateTo(mapToNav2Route(result.route))
            }
            DeeplinkOutcome.FALLBACK -> {
                activity.navigateClearingTo(Nav2HostActivity.ROUTE_HOME)
            }
            else -> Unit
        }
    }

    private fun applyDeeplinkToFragmentNav2Host(
        activity: FragmentNav2HostActivity,
        result: DeeplinkDispatchResult,
    ) {
        when (result.outcome) {
            DeeplinkOutcome.HANDLED -> {
                val targetRoute = mapToFragmentNav2Route(result.route)
                if (activity.currentNav2Route != targetRoute) {
                    activity.navigateNav2(targetRoute)
                }
            }
            DeeplinkOutcome.FALLBACK -> {
                if (activity.currentNav2Route != ComposeNav2Fragment.ROUTE_HOME) {
                    activity.navigateNav2(ComposeNav2Fragment.ROUTE_HOME)
                }
            }
            else -> Unit
        }
    }

    private fun applyDeeplinkToNav3Host(
        activity: Nav3HostActivity,
        result: DeeplinkDispatchResult,
        dedupe: Boolean = false,
    ) {
        when (result.outcome) {
            DeeplinkOutcome.HANDLED -> {
                val target = mapToNav3Key(result.route)
                if (dedupe && activity.backStack.lastOrNull() == target) return
                activity.navigateTo(target)
            }
            DeeplinkOutcome.FALLBACK -> {
                while (activity.backStackDepth > 1) {
                    activity.popBack()
                }
            }
            else -> Unit
        }
    }

    private fun applyDeeplinkToT8Leaf(
        activity: Nav3ToNav2InteropActivity,
        result: DeeplinkDispatchResult,
        dedupe: Boolean,
    ) {
        when (result.outcome) {
            DeeplinkOutcome.HANDLED -> {
                val target = mapToLeafRoute(result.route)
                if (dedupe && activity.currentLeafRoute == target) return
                activity.navigateNav2Leaf(target)
            }
            DeeplinkOutcome.FALLBACK -> {
                while (
                    activity.currentLeafRoute != Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME &&
                    activity.nav2LeafBackStackDepth > 1
                ) {
                    activity.popNav2LeafBack()
                }
            }
            else -> Unit
        }
    }

    private fun mapToNav2Route(deeplinkRoute: String?): String = when {
        deeplinkRoute?.startsWith("space/") == true -> Nav2HostActivity.ROUTE_SCREEN_A
        deeplinkRoute?.startsWith("profile/") == true -> Nav2HostActivity.ROUTE_SCREEN_B
        else -> Nav2HostActivity.ROUTE_SCREEN_C
    }

    private fun mapToFragmentNav2Route(deeplinkRoute: String?): String = when {
        deeplinkRoute?.startsWith("space/") == true -> ComposeNav2Fragment.ROUTE_SCREEN_A
        deeplinkRoute?.startsWith("profile/") == true -> ComposeNav2Fragment.ROUTE_SCREEN_B
        else -> ComposeNav2Fragment.ROUTE_HOME
    }

    private fun mapToNav3Key(deeplinkRoute: String?): Nav3Key = when {
        deeplinkRoute?.startsWith("space/") == true -> Nav3Key.ScreenA
        deeplinkRoute?.startsWith("profile/") == true -> Nav3Key.ScreenB
        else -> Nav3Key.Home
    }

    private fun mapToLeafRoute(deeplinkRoute: String?): String = when {
        deeplinkRoute?.startsWith("space/") == true -> Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL
        deeplinkRoute?.startsWith("profile/") == true -> Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL
        else -> Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME
    }

    private fun isTopNav3Key(activity: Nav3HostActivity, key: Nav3Key): Boolean =
        activity.backStack.lastOrNull() == key

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

    private fun launchNav2Case(number: Int): ActivityScenario<Nav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.F, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT6Nav2Case(number: Int): ActivityScenario<FragmentNav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentNav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.F, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchNav3Case(number: Int): ActivityScenario<Nav3HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.F, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT8Case(number: Int): ActivityScenario<Nav3ToNav2InteropActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3ToNav2InteropActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.F, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun <T : Activity> waitUntil(
        scenario: ActivityScenario<T>,
        timeoutMs: Long = WAIT_TIMEOUT_MS,
        predicate: (T) -> Boolean,
    ): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            var matched = false
            scenario.onActivity { activity ->
                matched = predicate(activity)
            }
            if (matched) return true
            SystemClock.sleep(WAIT_POLL_MS)
        }
        return false
    }

    companion object {
        private const val WAIT_TIMEOUT_MS = 5_000L
        private const val WAIT_POLL_MS = 25L
    }
}
