package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.recipes.hosts.RecipeMigrationHostActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeMigrationTest {

    @Test
    fun r05_nav2MigrationBaseline_matchesScenarioFlow() {
        val scenario = launchRCase(5)
        try {
            assertTrue(
                waitUntil(scenario) {
                    it.isMigrationScenarioReady &&
                        it.currentMigBeginRoute == RecipeMigrationHostActivity.ROUTE_A
                },
            )

            scenario.onActivity { activity ->
                activity.navigateMigBeginToA1()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigBeginRoute == RecipeMigrationHostActivity.ROUTE_A1
                },
            )

            scenario.onActivity { activity ->
                activity.switchMigBeginTopLevelToB()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigBeginRoute == RecipeMigrationHostActivity.ROUTE_B
                },
            )

            scenario.onActivity { activity ->
                activity.navigateMigBeginToB1()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigBeginRoute == RecipeMigrationHostActivity.ROUTE_B1
                },
            )

            scenario.onActivity { activity ->
                activity.switchMigBeginTopLevelToA()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigBeginRoute == RecipeMigrationHostActivity.ROUTE_A
                },
            )

            scenario.onActivity { activity ->
                assertEquals(RecipeMigrationHostActivity.ROUTE_A, activity.currentMigBeginRoute)
                activity.openMigBeginDialog()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.isMigBeginDialogVisible
                },
            )

            scenario.onActivity { activity ->
                assertTrue(activity.dismissMigBeginDialog())
                assertEquals(RecipeMigrationHostActivity.ROUTE_A, activity.currentMigBeginRoute)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun r06_nav3MigrationEnd_matchesScenarioFlow() {
        val scenario = launchRCase(6)
        try {
            assertTrue(
                waitUntil(scenario) {
                    it.isMigrationScenarioReady &&
                        it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_A
                },
            )

            scenario.onActivity { activity ->
                activity.navigateMigEndToA1()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_A1
                },
            )

            scenario.onActivity { activity ->
                activity.switchMigEndTopLevelToB()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndTopLevelRoute == RecipeMigrationHostActivity.ROUTE_B &&
                        it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_B
                },
            )

            scenario.onActivity { activity ->
                activity.navigateMigEndToB1()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_B1
                },
            )

            scenario.onActivity { activity ->
                activity.switchMigEndTopLevelToA()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndTopLevelRoute == RecipeMigrationHostActivity.ROUTE_A &&
                        it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_A1
                },
            )

            scenario.onActivity { activity ->
                activity.switchMigEndTopLevelToB()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_B1
                },
            )

            scenario.onActivity { activity ->
                activity.backFromMigEnd()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndTopLevelRoute == RecipeMigrationHostActivity.ROUTE_B &&
                        it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_B
                },
            )

            scenario.onActivity { activity ->
                activity.backFromMigEnd()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndTopLevelRoute == RecipeMigrationHostActivity.ROUTE_A &&
                        it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_A1
                },
            )

            scenario.onActivity { activity ->
                activity.backFromMigEnd()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_A
                },
            )

            scenario.onActivity { activity ->
                activity.openMigEndDialog()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.isMigEndDialogVisible
                },
            )

            scenario.onActivity { activity ->
                activity.backFromMigEnd()
            }
            assertTrue(
                waitUntil(scenario) {
                    it.currentMigEndRoute == RecipeMigrationHostActivity.ROUTE_A &&
                        !it.isMigEndDialogVisible
                },
            )
        } finally {
            scenario.close()
        }
    }

    private fun launchRCase(number: Int): ActivityScenario<RecipeMigrationHostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = RecipeMigrationHostActivity.createIntent(
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
