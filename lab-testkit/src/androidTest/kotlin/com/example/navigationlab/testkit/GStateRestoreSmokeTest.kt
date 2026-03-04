package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GStateRestoreSmokeTest {

    @Test
    fun g05_nav2HostLaunchAndNavigate_doesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.G, 5),
            runMode = "scripted",
        )

        val scenario: ActivityScenario<Nav2HostActivity> = ActivityScenario.launch(intent)
        try {
            val hostReady = waitUntil(scenario) { activity ->
                runCatching { activity.backStackDepth; true }.getOrDefault(false)
            }
            assertTrue(hostReady)

            scenario.onActivity { activity ->
                activity.navigateTo(Nav2HostActivity.ROUTE_SCREEN_A)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            var alive = false
            var depth = 0
            scenario.onActivity { activity ->
                alive = !activity.isFinishing
                depth = activity.backStackDepth
            }

            assertTrue(alive)
            assertTrue(depth >= 1)
        } finally {
            scenario.close()
        }
    }

    @Test
    fun g03_nav3HostLaunchAndNavigate_doesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.G, 3),
            runMode = "scripted",
        )

        val scenario: ActivityScenario<Nav3HostActivity> = ActivityScenario.launch(intent)
        try {
            scenario.onActivity { activity ->
                activity.navigateTo(Nav3Key.ScreenA)
                activity.navigateTo(Nav3Key.ScreenB)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            var alive = false
            var depth = 0
            scenario.onActivity { activity ->
                alive = !activity.isFinishing
                depth = activity.backStackDepth
            }

            assertTrue(alive)
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
