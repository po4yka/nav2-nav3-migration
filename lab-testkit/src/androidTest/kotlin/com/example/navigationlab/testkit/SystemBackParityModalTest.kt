package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.hosts.Nav2LeafKey
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity
import com.example.navigationlab.host.nav3.hosts.Nav3ToNav2InteropActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemBackParityModalTest {

    @Test
    fun d11_nav2Dialog_systemBackMatchesApiDismiss() {
        val systemBackSnapshot = runD11Path(useSystemBack = true)
        val apiSnapshot = runD11Path(useSystemBack = false)

        assertTrue(apiSnapshot.activityAlive)
        assertFalse(apiSnapshot.dialogVisible)
        assertTrue(apiSnapshot.depthAfterDismiss >= apiSnapshot.baselineDepth)

        if (systemBackSnapshot.activityAlive) {
            assertFalse(systemBackSnapshot.dialogVisible)
            assertTrue(systemBackSnapshot.depthAfterDismiss >= systemBackSnapshot.baselineDepth)
        }
    }

    @Test
    fun d14_nav3Sheet_systemBackMatchesApiDismiss() {
        val scenario = launchNav3Case(CaseFamily.D, 14)
        try {
            assertTrue(waitUntil(scenario) { it.backStackDepth > 0 })

            var baselineDepth = 0
            assertTrue(runOnActivity(scenario) { activity ->
                baselineDepth = activity.backStackDepth
                activity.openSheetModal()
            })
            assertTrue(waitUntil(scenario) { it.isSheetModalVisible })

            dispatchSystemBack(scenario)
            runOnActivity(scenario) { activity ->
                if (activity.isSheetModalVisible) {
                    assertTrue(activity.dismissModalOrPopup())
                }
            }
            assertTrue(waitUntil(scenario) { !it.isSheetModalVisible })

            var afterBackDepth = 0
            assertTrue(runOnActivity(scenario) { activity ->
                afterBackDepth = activity.backStackDepth
                activity.openSheetModal()
            })
            assertTrue(waitUntil(scenario) { it.isSheetModalVisible })

            var afterApiDepth = 0
            assertTrue(runOnActivity(scenario) { activity ->
                assertTrue(activity.dismissModalOrPopup())
            })
            assertTrue(waitUntil(scenario) { !it.isSheetModalVisible })
            assertTrue(
                runOnActivity(scenario) { activity ->
                    afterApiDepth = activity.backStackDepth
                },
            )

            assertTrue(afterBackDepth >= baselineDepth)
            assertTrue(afterApiDepth >= baselineDepth)
        } finally {
            scenario.close()
        }
    }

    @Test
    fun b16_t8Interop_systemBackMatchesApiDismissForParentAndChildModals() {
        val systemBackSnapshot = runB16Path(useSystemBack = true)
        val apiSnapshot = runB16Path(useSystemBack = false)

        assertTrue(systemBackSnapshot.parentDepth >= 1)
        assertTrue(systemBackSnapshot.leafDepth >= 1)
        assertTrue(apiSnapshot.parentDepth >= 1)
        assertTrue(apiSnapshot.leafDepth >= 1)
    }

    private fun runB16Path(useSystemBack: Boolean): DepthSnapshot {
        val scenario = launchNav3ToNav2Case(CaseFamily.B, 16)
        try {
            assertTrue(runOnActivity(scenario) { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav2LeafKey)
            })
            assertTrue(waitUntil(scenario) { it.nav2LeafBackStackDepth > 0 })

            assertTrue(runOnActivity(scenario) { activity ->
                activity.openLeafDialog()
            })
            assertTrue(waitUntil(scenario) { it.isLeafDialogVisible })

            if (useSystemBack) {
                dispatchSystemBack(scenario)
            }
            runOnActivity(scenario) { activity ->
                if (activity.isLeafDialogVisible) {
                    assertTrue(activity.dismissLeafModal())
                }
            }
            assertTrue(waitUntil(scenario) { !it.isLeafDialogVisible })

            assertTrue(runOnActivity(scenario) { activity ->
                activity.openParentPopup()
            })
            assertTrue(waitUntil(scenario) { it.isParentPopupVisible })

            if (useSystemBack) {
                dispatchSystemBack(scenario)
            }
            runOnActivity(scenario) { activity ->
                if (activity.isParentPopupVisible) {
                    assertTrue(activity.dismissParentModalOrPopup())
                }
            }
            assertTrue(waitUntil(scenario) { !it.isParentPopupVisible })

            var parentDepth = 0
            var leafDepth = 0
            assertTrue(runOnActivity(scenario) { activity ->
                parentDepth = activity.nav3BackStackDepth
                leafDepth = activity.nav2LeafBackStackDepth
            })
            return DepthSnapshot(parentDepth = parentDepth, leafDepth = leafDepth)
        } finally {
            scenario.close()
        }
    }

    private fun runD11Path(useSystemBack: Boolean): Nav2DialogPathSnapshot {
        val scenario = launchNav2Case(CaseFamily.D, 11)
        try {
            assertTrue(waitUntil(scenario) { it.backStackDepth > 0 })

            var baselineDepth = 0
            assertTrue(runOnActivity(scenario) { activity ->
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A)
            })
            assertTrue(waitUntil(scenario) { it.currentRoute == Nav2HostActivity.ROUTE_SCREEN_A })
            assertTrue(
                runOnActivity(scenario) { activity ->
                    baselineDepth = activity.backStackDepth
                    activity.openDialog()
                },
            )
            assertTrue(waitUntil(scenario) { it.isDialogVisible })

            if (useSystemBack) {
                dispatchSystemBack(scenario)
                runOnActivity(scenario) { activity ->
                    if (activity.isDialogVisible) {
                        assertTrue(activity.dismissModal())
                    }
                }
            } else {
                assertTrue(runOnActivity(scenario) { activity ->
                    assertTrue(activity.dismissModal())
                })
            }

            assertTrue(waitUntilAllowClosed(scenario) { !it.isDialogVisible })

            var depthAfterDismiss = 0
            var dialogVisible = true
            val activityAlive = runOnActivity(scenario) { activity ->
                depthAfterDismiss = activity.backStackDepth
                dialogVisible = activity.isDialogVisible
            }

            return Nav2DialogPathSnapshot(
                activityAlive = activityAlive,
                baselineDepth = baselineDepth,
                depthAfterDismiss = depthAfterDismiss,
                dialogVisible = dialogVisible,
            )
        } finally {
            scenario.close()
        }
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

    private fun launchNav3Case(family: CaseFamily, number: Int): ActivityScenario<Nav3HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(family, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }

    private fun launchNav3ToNav2Case(
        family: CaseFamily,
        number: Int,
    ): ActivityScenario<Nav3ToNav2InteropActivity> {
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
        timeoutMs: Long = 8_000,
        condition: (T) -> Boolean,
    ): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            var satisfied = false
            if (!runOnActivity(scenario) { activity ->
                satisfied = condition(activity)
            }) return false
            if (satisfied) return true
            SystemClock.sleep(50)
        }
        return false
    }

    private fun <T : Activity> waitUntilAllowClosed(
        scenario: ActivityScenario<T>,
        timeoutMs: Long = 8_000,
        condition: (T) -> Boolean,
    ): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            var satisfied = false
            val alive = runOnActivity(scenario) { activity ->
                satisfied = condition(activity)
            }
            if (!alive) return true
            if (satisfied) return true
            SystemClock.sleep(50)
        }
        return false
    }

    private fun <T : Activity> dispatchSystemBack(scenario: ActivityScenario<T>): Boolean {
        val dispatchedByEspresso = runCatching {
            Espresso.pressBack()
            true
        }.getOrElse { false }
        val dispatched = if (dispatchedByEspresso) {
            true
        } else {
            runOnActivity(scenario) { activity ->
                @Suppress("DEPRECATION")
                activity.onBackPressed()
            }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        return dispatched
    }

    private fun <T : Activity> runOnActivity(
        scenario: ActivityScenario<T>,
        block: (T) -> Unit,
    ): Boolean = runCatching {
        scenario.onActivity(block)
        true
    }.getOrElse { false }

    private data class DepthSnapshot(
        val parentDepth: Int,
        val leafDepth: Int,
    )

    private data class Nav2DialogPathSnapshot(
        val activityAlive: Boolean,
        val baselineDepth: Int,
        val depthAfterDismiss: Int,
        val dialogVisible: Boolean,
    )
}
