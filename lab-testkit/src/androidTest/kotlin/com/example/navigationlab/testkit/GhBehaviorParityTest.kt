package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.fragment.fragments.ComposeNav2Fragment
import com.example.navigationlab.host.fragment.fragments.LabStubFragment
import com.example.navigationlab.host.fragment.hosts.DualHostActivity
import com.example.navigationlab.host.fragment.hosts.FragmentHostActivity
import com.example.navigationlab.host.fragment.hosts.FragmentNav2HostActivity
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import com.example.navigationlab.host.nav2.hosts.Nav2ToNav3InteropActivity
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.hosts.IslandStubFragment
import com.example.navigationlab.host.nav3.hosts.Nav2LeafKey
import com.example.navigationlab.host.nav3.hosts.Nav3FragmentIslandActivity
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity
import com.example.navigationlab.host.nav3.hosts.Nav3ToNav2InteropActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GhBehaviorParityTest {

    @Test
    fun g01_rotationWithNav2AndOverlay_preservesRouteAndOverlayOrdering() {
        val scenario = launchT6Nav2Case(1)
        try {
            assertTrue(waitUntil(scenario) { it.nav2BackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })

            scenario.onActivity { activity ->
                activity.showOverlayFragment(LabStubFragment.newInstance("G01 Overlay", Color.DKGRAY))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.overlayBackStackDepth > 0 })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.overlayBackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
                assertEquals(ComposeNav2Fragment.ROUTE_SCREEN_A, activity.currentNav2Route)
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g02_rotationWithParentAndLeafStacks_restoresIndependentUnwind() {
        val scenario = launchT8Case(CaseFamily.G, 2)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
            }
            assertTrue(waitUntil(scenario) { it.nav2LeafBackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.navigateNav2Leaf(Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL)
            }
            assertTrue(waitUntil(scenario) { it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL })

            var parentDepthBefore = 0
            scenario.onActivity { activity ->
                parentDepthBefore = activity.nav3BackStackDepth
                assertEquals(3, parentDepthBefore)
            }

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.nav3BackStackDepth == parentDepthBefore })
            assertTrue(waitUntil(scenario) { it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2LeafBack())
            }
            assertTrue(waitUntil(scenario) { it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME })

            scenario.onActivity { activity ->
                assertEquals(parentDepthBefore, activity.nav3BackStackDepth)
                assertTrue(activity.popNav3Back())
                assertEquals(2, activity.nav3BackStackDepth)
                assertEquals(0, activity.nav2LeafBackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g03_recreateRestoresNav3KeyOrderingAndDepth() {
        val scenario = launchNav3Case(3)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav3Key.ScreenB)
                assertEquals(3, activity.backStackDepth)
            }

            scenario.recreate()
            assertTrue(
                waitUntil(scenario) {
                    it.backStackDepth == 3 && it.backStack.lastOrNull() is Nav3Key.ScreenB
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.popBack())
                assertEquals(2, activity.backStackDepth)
                assertTrue(activity.backStack.lastOrNull() is Nav3Key.ScreenA)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g04_recreateRestoresLegacyIslandStackAndKeepsChildFirstUnwind() {
        val scenario = launchT5Case(CaseFamily.G, 4)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.openLegacyIsland()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.isLegacyIslandVisible && it.nav3BackStackDepth == 3 && it.isLegacyIslandContainerReady
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Island One", Color.DKGRAY)))
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Island Two", Color.GRAY)))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.islandBackStackDepth >= 2 && it.currentIslandFragmentLabel == "Island Two"
                },
            )

            scenario.recreate()
            assertTrue(
                waitUntil(scenario) {
                    it.isLegacyIslandVisible &&
                        it.isLegacyIslandContainerReady &&
                        it.islandBackStackDepth >= 2 &&
                        it.nav3BackStackDepth == 3
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.popIslandFragmentBack())
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.islandBackStackDepth >= 1 && it.isLegacyIslandVisible })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g05_nonSaveableArgumentPath_isDetectedAndFallbackIsDeterministic() {
        val scenario = launchNav2Case(CaseFamily.G, 5)
        try {
            assertTrue(waitUntil(scenario) { it.backStackDepth > 0 })

            var resolvedBefore = ""
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A)
                val accepted = activity.injectNonSaveableArgumentSurrogate()
                assertFalse(accepted)
                assertTrue(activity.nonSaveableArgumentDetected)
                resolvedBefore = activity.resolvedArgumentValue
                assertEquals(Nav2HostActivity.DEFAULT_ARGUMENT_FALLBACK, resolvedBefore)
            }

            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.recreate()
            scenario.onActivity { activity ->
                assertTrue(activity.nonSaveableArgumentDetected)
                assertEquals(resolvedBefore, activity.resolvedArgumentValue)

                val accepted = activity.injectNonSaveableArgumentSurrogate()
                assertFalse(accepted)
                assertEquals(resolvedBefore, activity.resolvedArgumentValue)
                assertTrue(activity.backStackDepth >= 1)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g06_defaultArgumentResolution_isStableAcrossReentryAfterRecreate() {
        val scenario = launchT7Case(6)
        try {
            var resolvedBefore = ""
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2ToNav3InteropActivity.ROUTE_SCREEN_A)
                resolvedBefore = activity.enterNav3LeafWithDeferredDefault()
                assertEquals(Nav2ToNav3InteropActivity.DEFAULT_LEAF_ARGUMENT, resolvedBefore)
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == Nav2ToNav3InteropActivity.ROUTE_NAV3_LEAF })

            scenario.recreate()
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2ToNav3InteropActivity.ROUTE_SCREEN_A)
                val resolvedAfter = activity.enterNav3LeafWithDeferredDefault()
                assertEquals(resolvedBefore, resolvedAfter)
                assertEquals(resolvedBefore, activity.resolvedLeafDefaultArgument)
                assertEquals(Nav2ToNav3InteropActivity.ROUTE_NAV3_LEAF, activity.currentNav2Route)
                assertEquals(1, activity.nav3LeafBackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g07_recreateWithTopMostModal_restoresAndDismissesSingleLayer() {
        val scenario = launchT6Nav2Case(7)
        try {
            assertTrue(waitUntil(scenario) { it.nav2BackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.openDialog()
            }
            assertTrue(waitUntil(scenario) { it.isDialogVisible })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.isDialogVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
                assertEquals(ComposeNav2Fragment.ROUTE_SCREEN_A, activity.currentNav2Route)
                assertTrue(activity.popNav2Back())
                assertEquals(ComposeNav2Fragment.ROUTE_HOME, activity.currentNav2Route)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g08_recreateWithChildModalTop_restoresChildThenParentOrder() {
        val scenario = launchT8Case(CaseFamily.G, 8)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
            }
            assertTrue(waitUntil(scenario) { it.nav2LeafBackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.openLeafDialog()
            }
            assertTrue(waitUntil(scenario) { it.isLeafDialogVisible && it.nav3BackStackDepth == 3 })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.isLeafDialogVisible && it.nav3BackStackDepth == 3 })

            scenario.onActivity { activity ->
                assertTrue(activity.dismissLeafModal())
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME &&
                        it.nav3BackStackDepth == 3
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.popNav3Back())
                assertEquals(2, activity.nav3BackStackDepth)
                assertEquals(0, activity.nav2LeafBackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun h01_stateLossCommitDuringOnStop_keepsDeterministicStackOnResume() {
        val scenario = launchT1Case(1)
        try {
            scenario.onActivity { activity ->
                activity.showFragment(
                    LabStubFragment.newInstance("Screen A", FragmentHostActivity.COLORS[1]),
                    addToBackStack = true,
                )
                activity.scheduleStateLossOverlayCommit(
                    LabStubFragment.newInstance("StateLoss Overlay", FragmentHostActivity.COLORS[2]),
                )
            }

            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)

            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.didApplyStateLossCommit && it.backStackDepth >= 2 })

            scenario.onActivity { activity ->
                activity.supportFragmentManager.popBackStack()
                activity.supportFragmentManager.executePendingTransactions()
                assertTrue(activity.backStackDepth >= 1)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun h02_rapidInterleaving_convergesToDeterministicFinalRoute() {
        val scenario = launchNav2Case(CaseFamily.H, 2, runMode = "stress")
        try {
            assertTrue(waitUntil(scenario) { it.backStackDepth > 0 })

            scenario.onActivity { activity ->
                repeat(40) {
                    activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A, singleTop = true)
                    activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_B)
                    assertTrue(activity.popBack())
                    activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_C, singleTop = true)
                    assertTrue(activity.popBack())
                }

                assertEquals(Nav2HostActivity.ROUTE_SCREEN_A, activity.currentRoute)
                assertEquals(2, activity.backStackDepth)
                assertTrue(activity.popBack())
                assertEquals(Nav2HostActivity.ROUTE_HOME, activity.currentRoute)
                assertFalse(activity.popBack())
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun h03_executePendingTransactions_preservesIslandOrderingAfterDrain() {
        val scenario = launchT5Case(CaseFamily.H, 3)
        try {
            scenario.onActivity { activity ->
                activity.openLegacyIsland()
            }
            assertTrue(waitUntil(scenario) { it.isLegacyIslandVisible && it.isLegacyIslandContainerReady })

            scenario.onActivity { activity ->
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("First", Color.DKGRAY)))
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Second", Color.GRAY)))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.islandBackStackDepth >= 2 && it.currentIslandFragmentLabel == "Second"
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.popIslandFragmentBack())
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.islandBackStackDepth >= 1 && it.currentIslandFragmentLabel == "First"
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.popIslandFragmentBack())
                activity.supportFragmentManager.executePendingTransactions()
                assertEquals(0, activity.islandBackStackDepth)
                assertEquals(2, activity.nav3BackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun h04_concurrentUiAndDeeplinkRequests_resolveDeterministicallyWithAttribution() {
        val scenario = launchT8Case(CaseFamily.H, 4)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
            }
            assertTrue(waitUntil(scenario) { it.nav2LeafBackStackDepth > 0 })

            scenario.onActivity { activity ->
                val resolvedRoute = activity.resolveConcurrentLeafNavigation(
                    uiRoute = Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL,
                    deeplinkRoute = Nav3ToNav2InteropActivity.LEAF_ROUTE_DIALOG,
                )
                assertEquals(Nav3ToNav2InteropActivity.LEAF_ROUTE_DIALOG, resolvedRoute)
                assertEquals(Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL, activity.lastUiLeafNavigationRoute)
                assertEquals(Nav3ToNav2InteropActivity.LEAF_ROUTE_DIALOG, activity.lastDeeplinkLeafNavigationRoute)
            }
            assertTrue(waitUntil(scenario) { it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_DIALOG })

            scenario.onActivity { activity ->
                assertTrue(activity.dismissLeafModal())
            }
            assertTrue(waitUntil(scenario) { it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2LeafBack())
                assertEquals(Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME, activity.currentLeafRoute)
                assertEquals(3, activity.nav3BackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun h05_overlayVisibilityRace_convergesWithoutBaseRegression() {
        val scenario = launchT4Case(5)
        try {
            scenario.onActivity { activity ->
                activity.setBaseContent("Base Stable", 1)
                activity.showOverlayContainer()
                activity.showOverlayFragment(
                    LabStubFragment.newInstance("Race Overlay", FragmentHostActivity.COLORS[2]),
                )
                activity.hideOverlayContainer()
                activity.showOverlayContainer()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.isOverlayVisible && it.overlayBackStackDepth > 0 && it.hasOverlayFragment && it.isBaseVisible
                },
            )

            scenario.onActivity { activity ->
                assertEquals("Base Stable", activity.baseLabel)
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.isBaseVisible })
            scenario.onActivity { activity ->
                assertEquals("Base Stable", activity.baseLabel)
            }
        } finally {
            scenario.close()
        }
    }

    private fun launchT1Case(number: Int): ActivityScenario<FragmentHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.H, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT4Case(number: Int): ActivityScenario<DualHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = DualHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.H, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchNav2Case(
        family: CaseFamily,
        number: Int,
        runMode: String = "scripted",
    ): ActivityScenario<Nav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
            runMode = runMode,
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchNav3Case(number: Int): ActivityScenario<Nav3HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.G, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT6Nav2Case(number: Int): ActivityScenario<FragmentNav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentNav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.G, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT7Case(number: Int): ActivityScenario<Nav2ToNav3InteropActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2ToNav3InteropActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.G, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT8Case(family: CaseFamily, number: Int): ActivityScenario<Nav3ToNav2InteropActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3ToNav2InteropActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT5Case(family: CaseFamily, number: Int): ActivityScenario<Nav3FragmentIslandActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3FragmentIslandActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun <T : Activity> waitUntil(
        scenario: ActivityScenario<T>,
        timeoutMs: Long = 6_000,
        condition: (T) -> Boolean,
    ): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            var satisfied = false
            scenario.onActivity { activity ->
                satisfied = condition(activity)
            }
            if (satisfied) return true
            SystemClock.sleep(50)
        }
        return false
    }
}
