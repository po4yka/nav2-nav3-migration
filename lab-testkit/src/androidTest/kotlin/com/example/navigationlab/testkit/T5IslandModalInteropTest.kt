package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav3.hosts.IslandStubFragment
import com.example.navigationlab.host.nav3.hosts.Nav3FragmentIslandActivity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class T5IslandModalInteropTest {

    @Test
    fun d15_islandDialogAndParentPopup_layerAndDismissSafely() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3FragmentIslandActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.D, 15),
            runMode = "scripted",
        )

        val scenario: ActivityScenario<Nav3FragmentIslandActivity> = ActivityScenario.launch(intent)
        try {
            scenario.onActivity { activity ->
                activity.openLegacyIsland()
            }
            assertTrue(waitUntil(scenario) { it.isLegacyIslandVisible })

            scenario.onActivity { activity ->
                activity.showIslandFragment(
                    IslandStubFragment.newInstance(
                        label = "Island Base",
                        color = Color.DKGRAY,
                    ),
                )
                activity.showIslandModal()
            }
            assertTrue(waitUntil(scenario) { it.isIslandModalVisible })

            scenario.onActivity { activity ->
                activity.openParentPopup()
                assertTrue(activity.isParentPopupVisible)

                assertTrue(activity.dismissParentModalOrPopup())
                assertTrue(activity.dismissIslandModal())
                assertTrue(activity.isLegacyIslandVisible)
            }
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
