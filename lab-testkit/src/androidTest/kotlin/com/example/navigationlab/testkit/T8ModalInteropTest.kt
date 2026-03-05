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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class T8ModalInteropTest {

    @Test
    fun b15_parentOverlays_doNotMutateChildLeafState() {
        val scenario = launchCase(CaseFamily.B, 15)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
                activity.navigateNav2Leaf(Nav3ToNav2InteropActivity.LEAF_ROUTE_DETAIL)
                val childRouteBefore = activity.currentLeafRoute
                val parentDepthBefore = activity.nav3BackStackDepth

                activity.openParentDialog()
                activity.openParentPopup()
                assertTrue(activity.isParentPopupVisible)

                assertTrue(activity.dismissParentModalOrPopup())
                assertTrue(activity.dismissParentModalOrPopup())

                assertEquals(parentDepthBefore, activity.nav3BackStackDepth)
                assertEquals(childRouteBefore, activity.currentLeafRoute)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b16_childModalDismiss_doesNotMutateParentStack() {
        val scenario = launchCase(CaseFamily.B, 16)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
            }
            assertTrue(waitUntil(scenario) { it.nav2LeafBackStackDepth > 0 })

            var parentDepthBefore = 0
            scenario.onActivity { activity ->
                parentDepthBefore = activity.nav3BackStackDepth
                activity.openLeafDialog()
            }
            assertTrue(waitUntil(scenario) { it.isLeafDialogVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.dismissLeafModal())

                activity.openLeafSheet()
            }
            assertTrue(waitUntil(scenario) { it.isLeafSheetVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.dismissLeafModal())

                activity.openLeafFullScreenDialog()
            }
            assertTrue(waitUntil(scenario) { it.isLeafFullScreenDialogVisible })

            scenario.onActivity { activity ->
                assertTrue(activity.dismissLeafModal())

                assertEquals(parentDepthBefore, activity.nav3BackStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g08_recreateWithChildModalTop_restoresAndUnwinds() {
        val scenario = launchCase(CaseFamily.G, 8)
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
                assertTrue(activity.dismissLeafModal())
                assertTrue(activity.nav3BackStackDepth >= 3)
            }
        } finally {
            scenario.close()
        }
    }

    private fun launchCase(family: CaseFamily, number: Int): ActivityScenario<Nav3ToNav2InteropActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3ToNav2InteropActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
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
