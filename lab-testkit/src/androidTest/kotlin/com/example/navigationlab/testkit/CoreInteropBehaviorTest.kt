package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.fragment.fragments.ComposeNav2Fragment
import com.example.navigationlab.host.fragment.fragments.LabStubFragment
import com.example.navigationlab.host.fragment.hosts.FragmentNav2HostActivity
import com.example.navigationlab.host.fragment.hosts.FragmentNav3HostActivity
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import com.example.navigationlab.host.nav2.hosts.Nav2ToNav3InteropActivity
import com.example.navigationlab.host.nav2.hosts.Nav3LeafKey
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.hosts.IslandStubFragment
import com.example.navigationlab.host.nav3.hosts.Nav2LeafKey
import com.example.navigationlab.host.nav3.hosts.Nav3FragmentIslandActivity
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity
import com.example.navigationlab.host.nav3.hosts.Nav3NestedChainActivity
import com.example.navigationlab.host.nav3.hosts.Nav3ToNav2InteropActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreInteropBehaviorTest {

    @Test
    fun b01_nav2GraphBaseline_preservesBackHistory() {
        val scenario = launchNav2Case(1)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A)
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_B)
                assertTrue(activity.popBack())
                assertEquals(Nav2HostActivity.ROUTE_SCREEN_A, activity.currentRoute)
                assertEquals(2, activity.backStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b02_nav3GraphBaseline_preservesBackHistory() {
        val scenario = launchNav3Case(2)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav3Key.ScreenB)
                assertTrue(activity.popBack())
                assertEquals(2, activity.backStackDepth)
                assertTrue(activity.backStack.lastOrNull() is Nav3Key.ScreenA)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b03_nav3ToNav2Leaf_unwindsWithoutCorruptingParent() {
        val scenario = launchT8Case(3)
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

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2LeafBack())
            }
            assertTrue(waitUntil(scenario) { it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME })

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
    fun b04_nav2ToNav3Leaf_unwindsWithoutCorruptingParent() {
        val scenario = launchT7Case(4)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2ToNav3InteropActivity.ROUTE_SCREEN_A)
                activity.navigateTo(Nav2ToNav3InteropActivity.ROUTE_NAV3_LEAF)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2Route == Nav2ToNav3InteropActivity.ROUTE_NAV3_LEAF &&
                        it.nav3LeafBackStackDepth == 1
                },
            )

            scenario.onActivity { activity ->
                activity.navigateNav3Leaf(Nav3LeafKey.LeafDetail)
            }
            assertTrue(waitUntil(scenario) { it.nav3LeafBackStackDepth == 2 })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav3LeafBack())
            }
            assertTrue(waitUntil(scenario) { it.nav3LeafBackStackDepth == 1 })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
                assertEquals(Nav2ToNav3InteropActivity.ROUTE_SCREEN_A, activity.currentNav2Route)
                assertEquals(2, activity.nav2BackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b05_nav3LegacyIsland_fragmentBackStackIndependentFromParent() {
        val scenario = launchT5Case(5)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.openLegacyIsland()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.isLegacyIslandVisible &&
                        it.nav3BackStackDepth == 3 &&
                        it.isLegacyIslandContainerReady
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Island One", Color.DKGRAY)))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.islandBackStackDepth >= 1 })

            scenario.onActivity { activity ->
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Island Two", Color.GRAY)))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.islandBackStackDepth >= 2 })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav3Back())
                assertEquals(2, activity.nav3BackStackDepth)
                assertFalse(activity.isLegacyIslandVisible)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b06_fragmentHostOverlay_doesNotMutateNav2Route() {
        val scenario = launchT6Nav2Case(6)
        try {
            assertTrue(waitUntil(scenario) { it.nav2BackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })

            scenario.onActivity { activity ->
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", Color.DKGRAY))
                activity.supportFragmentManager.executePendingTransactions()
            }
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
    fun b07_fragmentHostNav2Dialog_returnsResultAndRestoresRoute() {
        val scenario = launchT6Nav2Case(7)
        try {
            assertTrue(waitUntil(scenario) { it.nav2BackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })

            scenario.onActivity { activity ->
                activity.openDialog()
            }
            assertTrue(waitUntil(scenario) { it.isDialogVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.confirmDialogResult())
            }
            assertTrue(
                waitUntil(scenario) {
                    !it.isDialogVisible &&
                        it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A &&
                        it.lastDialogResult == "confirmed"
                },
            )
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b08_fragmentHostNav3Modal_returnsResultAndRestoresKey() {
        val scenario = launchT6Nav3Case(8)
        try {
            assertTrue(waitUntil(scenario) { it.nav3BackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.navigateToScreenA()
                activity.openModal()
            }
            assertTrue(waitUntil(scenario) { it.isModalVisible && it.nav3BackStackDepth >= 3 })

            scenario.onActivity { activity ->
                assertTrue(activity.confirmModalResult())
            }
            assertTrue(
                waitUntil(scenario) {
                    !it.isModalVisible &&
                        it.lastModalResult == "confirmed" &&
                        it.nav3BackStackDepth == 2
                },
            )
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b09_nestedChain_unwindsDialogFragmentNav2ChainNav3InOrder() {
        val scenario = launchB09Case()
        try {
            scenario.onActivity { activity ->
                activity.enterChain()
            }
            assertTrue(waitUntil(scenario) { it.nav3Depth == 2 && it.nav2ChainDepth > 0 })

            scenario.onActivity { activity ->
                activity.navigateNav2Chain(Nav3NestedChainActivity.CHAIN_ROUTE_FRAGMENT)
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2ChainRoute == Nav3NestedChainActivity.CHAIN_ROUTE_FRAGMENT &&
                        it.isFragmentLayerReady
                },
            )

            scenario.onActivity { activity ->
                activity.navigateFragmentNav2(Nav3NestedChainActivity.FRAG_ROUTE_DIALOG)
            }
            assertTrue(waitUntil(scenario) { it.currentFragmentNav2Route == Nav3NestedChainActivity.FRAG_ROUTE_DIALOG })

            scenario.onActivity { activity ->
                assertTrue(activity.confirmFragmentDialogResult())
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentFragmentNav2Route == Nav3NestedChainActivity.FRAG_ROUTE_HOME &&
                        it.lastDialogResult == "confirmed"
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2ChainBack())
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentNav2ChainRoute == Nav3NestedChainActivity.CHAIN_ROUTE_ROOT &&
                        !it.isFragmentLayerReady
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.popNav3Back())
                assertEquals(1, activity.nav3Depth)
                assertEquals(0, activity.nav2ChainDepth)
                assertEquals(0, activity.fragmentNav2Depth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b10_crossEngineChildPop_doesNotMutateParentStack() {
        val scenario = launchT8Case(10)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
            }
            assertTrue(waitUntil(scenario) { it.nav2LeafBackStackDepth > 0 })

            var parentDepthBefore = 0
            scenario.onActivity { activity ->
                parentDepthBefore = activity.nav3BackStackDepth
                activity.navigateNav2Leaf(Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL)
            }
            assertTrue(waitUntil(scenario) { it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2LeafBack())
            }
            assertTrue(waitUntil(scenario) { it.currentLeafRoute == Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME })

            scenario.onActivity { activity ->
                assertEquals(parentDepthBefore, activity.nav3BackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b11_nav2SingleTop_semanticsParity() {
        val scenario = launchNav2Case(11)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A)
                val depthAfterFirst = activity.backStackDepth

                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A, singleTop = true)
                val depthAfterSingleTop = activity.backStackDepth

                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A, singleTop = false)
                val depthAfterRegular = activity.backStackDepth

                assertEquals(depthAfterFirst, depthAfterSingleTop)
                assertEquals(depthAfterFirst + 1, depthAfterRegular)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b12_nav2ClearToRoot_semanticsParity() {
        val scenario = launchNav2Case(12)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A)
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_B)
                assertTrue(activity.backStackDepth >= 3)

                activity.navigateClearingTo(Nav2HostActivity.ROUTE_HOME)
                assertEquals(Nav2HostActivity.ROUTE_HOME, activity.currentRoute)
                assertEquals(1, activity.backStackDepth)
                assertFalse(activity.popBack())
            }
        } finally {
            scenario.close()
        }
    }

    private fun launchNav2Case(number: Int): ActivityScenario<Nav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.B, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchNav3Case(number: Int): ActivityScenario<Nav3HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.B, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT7Case(number: Int): ActivityScenario<Nav2ToNav3InteropActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2ToNav3InteropActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.B, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT8Case(number: Int): ActivityScenario<Nav3ToNav2InteropActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3ToNav2InteropActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.B, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT5Case(number: Int): ActivityScenario<Nav3FragmentIslandActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3FragmentIslandActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.B, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT6Nav2Case(number: Int): ActivityScenario<FragmentNav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentNav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.B, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT6Nav3Case(number: Int): ActivityScenario<FragmentNav3HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentNav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.B, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchB09Case(): ActivityScenario<Nav3NestedChainActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3NestedChainActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.B, 9),
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
