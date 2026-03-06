package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentContainerView
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
import com.example.navigationlab.host.fragment.hosts.FragmentNav3HostActivity
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.hosts.IslandStubFragment
import com.example.navigationlab.host.nav3.hosts.LegacyIslandKey
import com.example.navigationlab.host.nav3.hosts.Nav2LeafKey
import com.example.navigationlab.host.nav3.hosts.Nav3FragmentIslandActivity
import com.example.navigationlab.host.nav3.hosts.Nav3ToNav2InteropActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoundationalFamiliesBehaviorTest {

    @Test
    fun a01_singleDeterministicContainer_existsBeforeFirstNavigation() {
        val scenario = launchT1Case(CaseFamily.A, 1)
        try {
            scenario.onActivity { activity ->
                val container = activity.findViewById<FragmentContainerView>(com.example.navigationlab.host.fragment.R.id.fragmentContainer)
                assertTrue(container.isShown)
                assertEquals(0, activity.backStackDepth)
                activity.showFragment(LabStubFragment.newInstance("Screen A", FragmentHostActivity.COLORS[1]), addToBackStack = true)
            }
            assertTrue(waitUntil(scenario) { it.backStackDepth == 1 && currentT1Label(it) == "Screen A" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun a02_lateContainerInflation_fallbackShowsOverlay_withoutMutatingBase() {
        val scenario = launchT4Case(CaseFamily.A, 2)
        try {
            scenario.onActivity { activity ->
                assertFalse(activity.isOverlayVisible)
                assertTrue(activity.isBaseVisible)
                activity.setBaseContent("Base", 1)
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[2]))
            }
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.hasOverlayFragment && it.baseLabel == "Base" })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
                assertEquals("Base", activity.baseLabel)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun a03_dualContainerVisibilityRace_keepsBaseAndOverlayIndependent() {
        val scenario = launchT4Case(CaseFamily.A, 3)
        try {
            scenario.onActivity { activity ->
                activity.setBaseContent("Base A", 1)
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[3]))
            }
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.isBaseVisible && it.hasOverlayFragment })

            scenario.onActivity { activity ->
                activity.setBaseContent("Base B", 2)
                assertTrue(activity.hasOverlayFragment)
                assertEquals("Base B", activity.baseLabel)
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.baseLabel == "Base B" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun a04_popupOverlay_doesNotReplaceBaseFragment() {
        val scenario = launchT1Case(CaseFamily.A, 4)
        try {
            scenario.onActivity { activity ->
                activity.showFragment(LabStubFragment.newInstance("Base A", FragmentHostActivity.COLORS[1]), addToBackStack = true)
            }
            assertTrue(waitUntil(scenario) { it.backStackDepth == 1 && currentT1Label(it) == "Base A" })

            scenario.onActivity { activity ->
                activity.addOverlayFragment(LabStubFragment.newInstance("Popup", FragmentHostActivity.COLORS[2]))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.backStackDepth == 2 && currentT1Label(it) == "Popup" })

            scenario.onActivity { activity ->
                activity.supportFragmentManager.popBackStack()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.backStackDepth == 1 && currentT1Label(it) == "Base A" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun a05_overlayRemoval_restoresPreviousBaseContentAndBackStack() {
        val scenario = launchT1Case(CaseFamily.A, 5)
        try {
            scenario.onActivity { activity ->
                activity.showFragment(LabStubFragment.newInstance("Screen A", FragmentHostActivity.COLORS[1]), addToBackStack = true)
                activity.showFragment(LabStubFragment.newInstance("Screen B", FragmentHostActivity.COLORS[2]), addToBackStack = true)
            }
            assertTrue(waitUntil(scenario) { it.backStackDepth == 2 && currentT1Label(it) == "Screen B" })

            var baseDepthBeforeOverlay = 0
            scenario.onActivity { activity ->
                baseDepthBeforeOverlay = activity.backStackDepth
                activity.addOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[3]))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.backStackDepth == baseDepthBeforeOverlay + 1 })

            scenario.onActivity { activity ->
                activity.supportFragmentManager.popBackStack()
                activity.supportFragmentManager.executePendingTransactions()
                assertEquals(baseDepthBeforeOverlay, activity.backStackDepth)
            }
            assertTrue(waitUntil(scenario) { currentT1Label(it) == "Screen B" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun a06_navigationBeforeInflation_isQueuedAndAppliedOnceHostIsReady() {
        val scenario = launchT4Case(CaseFamily.A, 6, deferInflation = true)
        try {
            scenario.onActivity { activity ->
                assertFalse(activity.isHostInflated)
                assertEquals(0, activity.pendingNavigationCount)
                activity.requestNavigation("Deferred Target", 2)
                assertEquals(1, activity.pendingNavigationCount)
                activity.completeHostInflation()
            }
            assertTrue(waitUntil(scenario) { it.isHostInflated && it.pendingNavigationCount == 0 && it.baseLabel == "Deferred Target" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun a07_rotationWhileOverlayVisible_preservesContainerOwnership() {
        val scenario = launchT4Case(CaseFamily.A, 7)
        try {
            scenario.onActivity { activity ->
                activity.setBaseContent("A07 Base", 3)
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[4]))
            }
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.overlayBackStackDepth > 0 })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.overlayVisibleBeforeConfigChange || it.isOverlayVisible })
            assertTrue(waitUntil(scenario) { it.baseLabel == "A07 Base" && it.baseColorIndex == 3 })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.baseLabel == "A07 Base" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun c01_composeBase_canOpenFragmentOverlay_andDismissWithoutStateLoss() {
        val scenario = launchT4Case(CaseFamily.C, 1)
        try {
            scenario.onActivity { activity ->
                activity.setBaseContent("Screen A", 1)
                activity.showOverlayFragment(LabStubFragment.newInstance("C01 Overlay", FragmentHostActivity.COLORS[2]))
            }
            assertTrue(waitUntil(scenario) { it.baseLabel == "Screen A" && it.isOverlayVisible && it.hasOverlayFragment })
            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.baseLabel == "Screen A" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun c02_androidViewBindingIsland_reentryKeepsContainerOperational() {
        val scenario = launchT5Case(CaseFamily.C, 2)
        try {
            scenario.onActivity { activity ->
                activity.openLegacyIsland()
            }
            assertTrue(waitUntil(scenario) { it.isLegacyIslandVisible && it.isLegacyIslandContainerReady })

            scenario.onActivity { activity ->
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Island A", Color.DKGRAY)))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.currentIslandFragmentLabel == "Island A" })

            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
            }
            assertTrue(waitUntil(scenario) { it.nav3BackStackDepth >= 3 && !it.isLegacyIslandVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav3Back())
            }
            assertTrue(waitUntil(scenario) { it.isLegacyIslandVisible && it.isLegacyIslandContainerReady })

            scenario.onActivity { activity ->
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Island B", Color.GRAY)))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.currentIslandFragmentLabel == "Island B" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun c03_fragmentHostedComposeNav2_preservesScopedBackStack() {
        val scenario = launchT6Nav2Case(CaseFamily.C, 3)
        try {
            assertTrue(waitUntil(scenario) { it.nav2BackStackDepth > 0 })
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_B)
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A && it.nav2BackStackDepth >= 2 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun c04_composeCanOpenXmlDialog_andReceiveFragmentResult() {
        val scenario = launchT4Case(CaseFamily.C, 4)
        try {
            var dialogResult: String? = null
            scenario.onActivity { activity ->
                activity.setBaseContent("Compose Base", 1)
                activity.supportFragmentManager.setFragmentResultListener(
                    ResultDialogFragment.REQUEST_KEY,
                    activity,
                ) { _, bundle ->
                    dialogResult = bundle.getString(ResultDialogFragment.RESULT_KEY)
                }
                ResultDialogFragment().show(activity.supportFragmentManager, "c04_dialog")
            }
            assertTrue(
                waitUntil(scenario) {
                    val fragment = it.supportFragmentManager.findFragmentByTag("c04_dialog") as? ResultDialogFragment
                    fragment?.dialog?.isShowing == true
                },
            )
            scenario.onActivity { activity ->
                val dialog = activity.supportFragmentManager.findFragmentByTag("c04_dialog") as? ResultDialogFragment
                assertNotNull(dialog)
                dialog?.confirmAndDismiss()
            }
            assertTrue(waitUntil(scenario) { dialogResult == "confirmed" && it.baseLabel == "Compose Base" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun c05_fragmentComposeDialog_returnsResult_andRestoresRoute() {
        val scenario = launchT6Nav2Case(CaseFamily.C, 5)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
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
    fun c06_xmlContextInputs_remainStableAcrossComposeNavTransitions() {
        val scenario = launchT6Nav2Case(CaseFamily.C, 6)
        try {
            scenario.onActivity { activity ->
                assertEquals("C06", activity.intent.getStringExtra(FragmentNav2HostActivity.EXTRA_CASE_ID))
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_B)
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun c07_composeArgs_mapToFragmentBundleArgumentsCorrectly() {
        val scenario = launchT4Case(CaseFamily.C, 7)
        try {
            scenario.onActivity { activity ->
                activity.setBaseContent("ProductDetail", 2)
                activity.showOverlayFragment(
                    LabStubFragment.newInstance("ProductDetail", FragmentHostActivity.COLORS[2]),
                )
            }
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.hasOverlayFragment })

            scenario.onActivity { activity ->
                val overlayFragment =
                    activity.supportFragmentManager.findFragmentById(com.example.navigationlab.host.fragment.R.id.overlayContainer)
                        as? LabStubFragment
                val args = overlayFragment?.arguments
                assertEquals("ProductDetail", args?.getString("label"))
                assertEquals(FragmentHostActivity.COLORS[2], args?.getInt("color"))
                assertEquals("ProductDetail", activity.baseLabel)
                activity.dismissOverlay()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.baseLabel == "ProductDetail" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun c08_recreate_rebuildsXmlComposeBridge_withoutDuplicateContainers() {
        val scenario = launchT4Case(CaseFamily.C, 8)
        try {
            scenario.onActivity { activity ->
                activity.setBaseContent("ScreenB", 3)
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[4]))
            }
            assertTrue(waitUntil(scenario) { it.baseLabel == "ScreenB" && it.isOverlayVisible && it.hasOverlayFragment })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.baseLabel == "ScreenB" && it.baseColorIndex == 3 })
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.hasOverlayFragment && it.overlayBackStackDepth > 0 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d01_nav2BottomSheet_baselineBehavior() {
        val scenario = launchT6Nav2Case(CaseFamily.D, 1)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.openBottomSheet()
            }
            assertTrue(waitUntil(scenario) { it.isBottomSheetVisible })
            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d02_nav2Dialog_baselineBehavior() {
        val scenario = launchT6Nav2Case(CaseFamily.D, 2)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.openDialog()
            }
            assertTrue(waitUntil(scenario) { it.isDialogVisible })
            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d03_nav2FullScreenDialog_baselineBehavior() {
        val scenario = launchT6Nav2Case(CaseFamily.D, 3)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_B)
                activity.openFullScreenDialog()
            }
            assertTrue(waitUntil(scenario) { it.isFullScreenDialogVisible })
            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_B })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d04_overlayAboveNav2Sheet_unwindsOverlayFirst() {
        val scenario = launchT6Nav2Case(CaseFamily.D, 4)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.openBottomSheet()
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[2]))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.isBottomSheetVisible && it.isOverlayVisible })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.isBottomSheetVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d05_overlayAboveNav3Modal_unwindsOverlayFirst() {
        val scenario = launchT6Nav3Case(CaseFamily.D, 5)
        try {
            scenario.onActivity { activity ->
                activity.navigateToScreenA()
                activity.openModal()
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[3]))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.isModalVisible && it.isOverlayVisible })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.isModalVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav3Back())
            }
            assertTrue(waitUntil(scenario) { !it.isModalVisible && it.nav3BackStackDepth == 2 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d06_simultaneousOverlayLayers_canBeDismissedIndependently() {
        val scenario = launchT6Nav2Case(CaseFamily.D, 6)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_B)
                activity.openFullScreenDialog()
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[4]))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.isFullScreenDialogVisible && it.isOverlayVisible })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.isFullScreenDialogVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_B })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d07_sheetDismiss_doesNotPopParentRoute() {
        val scenario = launchT6Nav2Case(CaseFamily.D, 7)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_B)
                activity.openBottomSheet()
            }
            assertTrue(waitUntil(scenario) { it.isBottomSheetVisible })
            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_B })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d08_fullScreenDialogDismiss_preservesUnderlyingRoute() {
        val scenario = launchT6Nav2Case(CaseFamily.D, 8)
        try {
            var depthBeforeDialog = 0
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                depthBeforeDialog = activity.nav2BackStackDepth
                activity.openFullScreenDialog()
            }
            assertTrue(waitUntil(scenario) { it.isFullScreenDialogVisible })
            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
                assertEquals(ComposeNav2Fragment.ROUTE_SCREEN_A, activity.currentNav2Route)
                assertEquals(depthBeforeDialog, activity.nav2BackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d09_overlayToSheetTransition_restoresSheetStateAfterOverlayPop() {
        val scenario = launchT6Nav2Case(CaseFamily.D, 9)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.openBottomSheet()
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[5]))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.isBottomSheetVisible })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.isBottomSheetVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e01_composeBackHandlerPopsNav2BeforeRootExit() {
        val scenario = launchNav2Case(CaseFamily.E, 1)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A)
                assertTrue(activity.popBack())
                assertEquals(Nav2HostActivity.ROUTE_HOME, activity.currentRoute)
                assertFalse(activity.popBack())
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e02_fragmentBackLayer_popsDetailBeforeRoot() {
        val scenario = launchT1Case(CaseFamily.E, 2)
        try {
            scenario.onActivity { activity ->
                activity.showFragment(LabStubFragment.newInstance("Detail", FragmentHostActivity.COLORS[1]), addToBackStack = true)
            }
            assertTrue(waitUntil(scenario) { it.backStackDepth == 1 && currentT1Label(it) == "Detail" })
            scenario.onActivity { activity ->
                activity.supportFragmentManager.popBackStack()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.backStackDepth == 0 && currentT1Label(it) == "Home" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e03_twoTierBack_unwindsOverlayThenComposeNavLayer() {
        val scenario = launchT6Nav2Case(CaseFamily.E, 3)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[2]))
            }
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })
            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.currentNav2Route == ComposeNav2Fragment.ROUTE_SCREEN_A })
            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_HOME })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e04_wizardLikeChildFragmentStack_unwindsBeforeParentNav3() {
        val scenario = launchT5Case(CaseFamily.E, 4)
        try {
            scenario.onActivity { activity ->
                activity.openLegacyIsland()
            }
            assertTrue(waitUntil(scenario) { it.isLegacyIslandVisible && it.isLegacyIslandContainerReady })

            scenario.onActivity { activity ->
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Step 1", Color.DKGRAY)))
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Step 2", Color.GRAY)))
                assertTrue(activity.showIslandFragment(IslandStubFragment.newInstance("Step 3", Color.LTGRAY)))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.islandBackStackDepth >= 3 })

            var parentDepth = 0
            scenario.onActivity { activity ->
                parentDepth = activity.nav3BackStackDepth
                activity.popIslandFragmentBack()
                activity.popIslandFragmentBack()
            }
            assertTrue(waitUntil(scenario) { it.nav3BackStackDepth == parentDepth && it.islandBackStackDepth >= 1 })

            scenario.onActivity { activity ->
                activity.popIslandFragmentBack()
                activity.popNav3Back()
            }
            assertTrue(waitUntil(scenario) { it.nav3BackStackDepth == 1 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e05_mixedStackBack_unwindsOverlaySheetThenBaseRoute() {
        val scenario = launchT6Nav2Case(CaseFamily.E, 5)
        try {
            scenario.onActivity { activity ->
                activity.navigateNav2(ComposeNav2Fragment.ROUTE_SCREEN_A)
                activity.openBottomSheet()
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[3]))
            }
            assertTrue(waitUntil(scenario) { it.isOverlayVisible && it.isBottomSheetVisible })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.isBottomSheetVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav2Back())
                assertTrue(activity.popNav2Back())
            }
            assertTrue(waitUntil(scenario) { it.currentNav2Route == ComposeNav2Fragment.ROUTE_HOME })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e06_rootBackSingleShot_gateFiresOnceUntilReset() {
        val scenario = launchT4Case(CaseFamily.E, 6)
        try {
            scenario.onActivity { activity ->
                assertEquals(0, activity.rootExitEventCount)
                assertTrue(activity.dispatchRootBackSingleShot())
                assertEquals(1, activity.rootExitEventCount)
                assertFalse(activity.dispatchRootBackSingleShot())
                assertEquals(1, activity.rootExitEventCount)
                activity.resetRootExitGate()
                assertTrue(activity.dispatchRootBackSingleShot())
                assertEquals(2, activity.rootExitEventCount)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e07_fallbackPopChain_unwindsChildBeforeParentAfterDeeplinkPath() {
        val scenario = launchT8Case(CaseFamily.E, 7)
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
                assertEquals(Nav3ToNav2InteropActivity.LEAF_ROUTE_HOME, activity.currentLeafRoute)
                assertTrue(activity.popNav3Back())
            }
            assertTrue(waitUntil(scenario) { it.nav3BackStackDepth == 2 && it.nav2LeafBackStackDepth == 0 })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun e08_pendingTransactions_stillUnwindOverlayBeforeNav3Modal() {
        val scenario = launchT6Nav3Case(CaseFamily.E, 8)
        try {
            scenario.onActivity { activity ->
                activity.navigateToScreenA()
                activity.openModal()
                activity.showOverlayFragment(LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[4]))
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { it.isModalVisible && it.isOverlayVisible })

            scenario.onActivity { activity ->
                activity.dismissOverlay()
                activity.supportFragmentManager.executePendingTransactions()
            }
            assertTrue(waitUntil(scenario) { !it.isOverlayVisible && it.isModalVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.popNav3Back())
            }
            assertTrue(waitUntil(scenario) { !it.isModalVisible && it.nav3BackStackDepth == 2 })
        } finally {
            scenario.close()
        }
    }

    private fun launchT1Case(family: CaseFamily, number: Int): ActivityScenario<FragmentHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT4Case(
        family: CaseFamily,
        number: Int,
        deferInflation: Boolean = false,
    ): ActivityScenario<DualHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = DualHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
            runMode = "scripted",
            deferInflation = deferInflation,
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT6Nav2Case(family: CaseFamily, number: Int): ActivityScenario<FragmentNav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentNav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchT6Nav3Case(family: CaseFamily, number: Int): ActivityScenario<FragmentNav3HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentNav3HostActivity.createIntent(
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

    private fun launchNav2Case(family: CaseFamily, number: Int): ActivityScenario<Nav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
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

    private fun currentT1Label(activity: FragmentHostActivity): String? {
        val fragment = activity.supportFragmentManager
            .findFragmentById(com.example.navigationlab.host.fragment.R.id.fragmentContainer) as? LabStubFragment
        return fragment?.arguments?.getString("label")
    }

    private fun <T : Activity> waitUntil(
        scenario: ActivityScenario<T>,
        timeoutMs: Long = 6_000,
        condition: (T) -> Boolean,
    ): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            var matched = false
            scenario.onActivity { activity ->
                matched = condition(activity)
            }
            if (matched) return true
            SystemClock.sleep(50)
        }
        return false
    }

    class ResultDialogFragment : DialogFragment() {

        fun confirmAndDismiss() {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                bundleOf(RESULT_KEY to "confirmed"),
            )
            dismiss()
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, androidx.appcompat.R.style.Theme_AppCompat_Dialog)
        }

        companion object {
            const val REQUEST_KEY = "c04_request"
            const val RESULT_KEY = "dialog_result"
        }
    }
}
