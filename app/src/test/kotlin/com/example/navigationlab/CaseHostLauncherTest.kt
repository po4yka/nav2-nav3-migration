package com.example.navigationlab

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.launch.CaseHostLauncher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class CaseHostLauncherTest {

    @Test
    fun launch_addsNewTaskFlag_whenUsingApplicationContext() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val launcher = launcherWithSingleCase()

        val launched = launcher.launch(
            context = appContext,
            caseId = LabCaseId(CaseFamily.R, 1),
            runMode = RunMode.MANUAL,
        )

        assertTrue(launched)
        val startedIntent = shadowOf(appContext as Application).nextStartedActivity
        assertNotNull(startedIntent)
        assertTrue(startedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun launch_doesNotForceNewTaskFlag_whenUsingActivityContext() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val launcher = launcherWithSingleCase()

        val launched = launcher.launch(
            context = activity,
            caseId = LabCaseId(CaseFamily.R, 1),
            runMode = RunMode.SCRIPTED,
        )

        assertTrue(launched)
        val startedIntent = shadowOf(activity).nextStartedActivity
        assertNotNull(startedIntent)
        assertFalse(startedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun launch_returnsFalse_whenCaseHasNoMapping() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val launcher = launcherWithSingleCase()

        val launched = launcher.launch(
            context = appContext,
            caseId = LabCaseId(CaseFamily.R, 2),
            runMode = RunMode.STRESS,
        )

        assertFalse(launched)
    }

    private fun launcherWithSingleCase(): CaseHostLauncher =
        CaseHostLauncher(
            launchByCaseCode = mapOf(
                "R01" to { context, _, _ -> Intent(context, NavigationLabActivity::class.java) },
            ),
        )
}
