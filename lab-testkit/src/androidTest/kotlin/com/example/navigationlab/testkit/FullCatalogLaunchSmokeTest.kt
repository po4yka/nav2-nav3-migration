package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import com.example.navigationlab.catalog.LabScenarioCatalog
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullCatalogLaunchSmokeTest {

    @Test
    fun allCatalogCases_launchHostsWithoutCrashing() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scenarios = LabScenarioCatalog.scenarios

        assertTrue("Catalog should contain at least the documented 86 scenarios", scenarios.size >= 86)
        val duplicates = scenarios.groupBy { it.id.code }.filterValues { it.size > 1 }
        assertTrue("Duplicate case codes found: ${duplicates.keys}", duplicates.isEmpty())
        assertTrue(
            "Expected unsupported harness cases must exist in catalog",
            unsupportedHarnessCases.all { code -> scenarios.any { it.id.code == code } },
        )

        val executableScenarios = scenarios.filterNot { scenario ->
            scenario.id.family == CaseFamily.R && scenario.id.number == 17
        }
        assertTrue(
            "R17 must be excluded from harness launch pass",
            executableScenarios.none { it.id.family == CaseFamily.R && it.id.number == 17 },
        )

        executableScenarios
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

    private companion object {
        /**
         * R17 relies on adaptive-scene runtime behavior that is incompatible with the
         * current instrumentation harness classpath and crashes before assertions can run.
         */
        val unsupportedHarnessCases: Set<String> = setOf("R17")
    }
}
