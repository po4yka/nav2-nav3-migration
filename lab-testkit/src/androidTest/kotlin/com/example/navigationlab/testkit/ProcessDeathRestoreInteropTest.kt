package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.hosts.Nav2LeafKey
import com.example.navigationlab.host.nav3.hosts.Nav3ToNav2InteropActivity
import com.example.navigationlab.recipes.hosts.RecipeModalMatrixHostActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProcessDeathRestoreInteropTest {

    @Test
    fun g08_recreateAndColdRelaunch_restoresThenResetsCleanly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3ToNav2InteropActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.G, 8),
            runMode = "scripted",
        )

        val scenario = ActivityScenario.launch<Nav3ToNav2InteropActivity>(intent)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
            }
            assertTrue(waitUntil(scenario) { it.nav2LeafBackStackDepth > 0 })

            scenario.onActivity { activity ->
                activity.openLeafDialog()
            }
            assertTrue(waitUntil(scenario) { it.isLeafDialogVisible })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.nav2LeafBackStackDepth > 0 })
            assertTrue(waitUntil(scenario) { it.isLeafDialogVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.nav3BackStackDepth >= 3)
                assertTrue(activity.dismissLeafModal())
            }
            assertTrue(waitUntil(scenario) { !it.isLeafDialogVisible || it.nav2LeafBackStackDepth <= 1 })
        } finally {
            scenario.close()
        }

        // Cold relaunch after scenario close acts as a process-death-like restart boundary.
        val coldScenario = ActivityScenario.launch<Nav3ToNav2InteropActivity>(intent)
        try {
            coldScenario.onActivity { activity ->
                assertEquals(1, activity.nav3BackStackDepth)
                assertEquals(0, activity.nav2LeafBackStackDepth)
                assertFalse(activity.isLeafDialogVisible)
                assertFalse(activity.isLeafSheetVisible)
                assertFalse(activity.isLeafFullScreenDialogVisible)
            }
        } finally {
            coldScenario.close()
        }
    }

    @Test
    fun r25_recreateAndColdRelaunch_restoresInteropModalChainThenResets() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeModalMatrixHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, 25),
            runMode = "scripted",
        )

        val scenario = ActivityScenario.launch<RecipeModalMatrixHostActivity>(intent)
        try {
            scenario.onActivity { activity ->
                activity.openParentDetail()
                activity.openInteropLeaf()
            }
            assertTrue(waitUntil(scenario) { it.nav3ParentDepth >= 3 && it.nav2LeafDepth > 0 })

            scenario.onActivity { activity ->
                activity.openDialogModal()
                activity.openParentDialogModal()
            }
            assertTrue(waitUntil(scenario) { it.isParentDialogVisible })

            scenario.recreate()
            assertTrue(waitUntil(scenario) { it.isParentDialogVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.dismissTopLayer())
            }
            assertTrue(waitUntil(scenario) { it.isDialogVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.dismissTopLayer())
            }
            assertTrue(
                waitUntil(scenario) {
                    !it.isParentDialogVisible && (!it.isDialogVisible || it.nav2LeafDepth <= 1)
                },
            )
        } finally {
            scenario.close()
        }

        // Cold relaunch should start from clean root state.
        val coldScenario = ActivityScenario.launch<RecipeModalMatrixHostActivity>(intent)
        try {
            coldScenario.onActivity { activity ->
                assertEquals(1, activity.nav3ParentDepth)
                assertEquals(0, activity.nav2LeafDepth)
                assertFalse(activity.isParentDialogVisible)
                assertFalse(activity.isDialogVisible)
                assertFalse(activity.isSheetVisible)
            }
        } finally {
            coldScenario.close()
        }
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
