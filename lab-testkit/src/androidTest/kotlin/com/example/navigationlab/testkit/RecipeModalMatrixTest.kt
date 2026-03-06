package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.recipes.hosts.RecipeModalMatrixHostActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeModalMatrixTest {

    @Test
    fun r20_nav2ModalReference_sequenceIsStable() {
        val scenario = launchRCase(20)
        try {
            scenario.onActivity { activity ->
                val baseDepth = activity.nav2ParentDepth
                activity.openDialogModal()
                assertTrue(activity.isDialogVisible)
                assertTrue(activity.dismissTopLayer())

                activity.openSheetModal()
                assertTrue(activity.isSheetVisible)
                assertTrue(activity.dismissTopLayer())

                activity.openFullScreenDialogModal()
                assertTrue(activity.isFullScreenDialogVisible)
                assertTrue(activity.dismissTopLayer())

                assertEquals(baseDepth, activity.nav2ParentDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r21_nav3ModalReference_sequenceIsStable() {
        val scenario = launchRCase(21)
        try {
            scenario.onActivity { activity ->
                val baseDepth = activity.nav3ParentDepth
                activity.openDialogModal()
                assertTrue(activity.isDialogVisible)
                assertTrue(activity.dismissTopLayer())

                activity.openSheetModal()
                assertTrue(activity.isSheetVisible)
                assertTrue(activity.dismissTopLayer())

                activity.openParentPopupOverlay()
                assertTrue(activity.isParentPopupVisible)
                assertTrue(activity.dismissTopLayer())

                assertEquals(baseDepth, activity.nav3ParentDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r22_nav2ToNav3Interop_modalChainUnwindsCorrectly() {
        val scenario = launchRCase(22)
        try {
            scenario.onActivity { activity ->
                activity.openInteropLeaf()
                activity.openDialogModal() // child Nav3 dialog
                activity.openParentDialogModal() // parent Nav2 dialog

                assertTrue(activity.isParentDialogVisible)
                assertTrue(activity.dismissTopLayer()) // parent first
                assertTrue(activity.dismissTopLayer()) // child second
                assertEquals(1, activity.nav3LeafDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r23_nav3ToNav2Interop_modalChainUnwindsCorrectly() {
        val scenario = launchRCase(23)
        try {
            scenario.onActivity { activity ->
                activity.openParentDetail()
                activity.openInteropLeaf()
            }
            assertTrue(waitUntil(scenario) { it.nav2LeafDepth > 0 })

            scenario.onActivity { activity ->
                activity.openSheetModal() // child Nav2 sheet
                activity.openParentPopupOverlay() // parent Nav3 popup
            }
            assertTrue(waitUntil(scenario) { it.isParentPopupVisible })
            assertTrue(waitUntil(scenario) { it.isSheetVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.isParentPopupVisible)
                assertTrue(activity.dismissTopLayer()) // parent popup
                assertTrue(activity.dismissTopLayer()) // child sheet
                assertEquals(1, activity.nav2LeafDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r24_legacyIslandPopupDialog_referenceFlowStable() {
        val scenario = launchRCase(24)
        try {
            scenario.onActivity { activity ->
                activity.openInteropLeaf() // enter island
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                activity.attachIslandFragment()
                activity.openIslandDialogModal()
            }
            assertTrue(waitUntil(scenario) { it.isIslandDialogVisible })

            scenario.onActivity { activity ->
                activity.openParentPopupOverlay()
                assertTrue(activity.isParentPopupVisible)

                assertTrue(activity.dismissTopLayer()) // parent popup
                assertTrue(activity.dismissTopLayer()) // island dialog
                assertTrue(activity.isLegacyIslandVisible)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r25_restoreAndUnwind_stackedInteropModalsRemainDeterministic() {
        val scenario = launchRCase(25)
        try {
            scenario.onActivity { activity ->
                activity.openParentDetail()
                activity.openInteropLeaf()
            }
            assertTrue(waitUntil(scenario) { it.nav2LeafDepth > 0 })

            scenario.onActivity { activity ->
                activity.openDialogModal() // child dialog
                activity.openParentDialogModal() // parent dialog
            }
            assertTrue(waitUntil(scenario) { it.isDialogVisible })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.isParentDialogVisible || it.isDialogVisible })

            var hadParentModal = false
            scenario.onActivity { activity ->
                hadParentModal = activity.isParentDialogVisible
                assertTrue(activity.dismissTopLayer())
            }
            if (hadParentModal) {
                assertTrue(waitUntil(scenario) { it.nav2LeafDepth > 0 })
                scenario.onActivity { activity ->
                    assertTrue(activity.dismissTopLayer())
                }
            }
            scenario.onActivity { activity ->
                assertTrue(activity.nav3ParentDepth >= 3)
            }
        } finally {
            scenario.close()
        }
    }

    private fun launchRCase(number: Int): ActivityScenario<RecipeModalMatrixHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeModalMatrixHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun <T : Activity> waitUntil(
        scenario: ActivityScenario<T>,
        timeoutMs: Long = 5_000,
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
