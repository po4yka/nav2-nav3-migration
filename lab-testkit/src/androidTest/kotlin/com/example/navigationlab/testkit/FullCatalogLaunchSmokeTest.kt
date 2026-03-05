package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import com.example.navigationlab.catalog.LabScenarioCatalog
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullCatalogLaunchSmokeTest {

    @Test
    fun allCatalogCases_launchHostsWithoutCrashing() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scenarios = LabScenarioCatalog.scenarios

        assertTrue("Catalog should contain at least the documented 101 scenarios", scenarios.size >= 101)
        val duplicates = scenarios.groupBy { it.id.code }.filterValues { it.size > 1 }
        assertTrue("Duplicate case codes found: ${duplicates.keys}", duplicates.isEmpty())
        scenarios
            .sortedWith(compareBy({ it.id.family.ordinal }, { it.id.number }))
            .forEach { scenario ->
                val intentFactory = requireNotNull(LabScenarioCatalog.launchByCaseCode[scenario.id.code]) {
                    "Missing host launcher for case ${scenario.id.code}"
                }
                val intent = intentFactory(context, scenario.id, "scripted")
                val activityScenario = ActivityScenario.launch<Activity>(intent)
                try {
                    var launched = false
                    activityScenario.onActivity {
                        launched = true
                    }
                    assertTrue("Failed to launch case ${scenario.id.code}", launched)
                    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                } finally {
                    activityScenario.close()
                }
            }
    }
}
