package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HTransactionStressSmokeTest {

    @Test
    fun h02_rapidNavAndPopLoop_doesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.H, 2),
            runMode = "stress",
        )

        val scenario: ActivityScenario<Nav2HostActivity> = ActivityScenario.launch(intent)
        try {
            val hostReady = waitUntil(scenario) { activity ->
                runCatching { activity.backStackDepth; true }.getOrDefault(false)
            }
            assertTrue(hostReady)

            scenario.onActivity { activity ->
                repeat(40) {
                    activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A, singleTop = true)
                    activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_B)
                    activity.popBack()
                    activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_C, singleTop = true)
                    activity.popBack()
                }
            }

            var isAlive = false
            var depth = 0
            scenario.onActivity { activity ->
                isAlive = !activity.isFinishing
                depth = activity.backStackDepth
            }

            assertTrue(isAlive)
            assertTrue(depth >= 1)
        } finally {
            scenario.close()
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
