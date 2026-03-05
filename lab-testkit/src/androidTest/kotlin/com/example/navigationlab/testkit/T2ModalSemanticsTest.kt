package com.example.navigationlab.testkit

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class T2ModalSemanticsTest {

    @Test
    fun d10_bottomSheetSemantics_noParentMutation() {
        val scenario = launchDCase(10)
        try {
            scenario.onActivity { activity ->
                val baseDepth = activity.backStackDepth
                activity.openBottomSheet()
                assertTrue(activity.isBottomSheetVisible)
                assertTrue(activity.dismissModal())
                assertEquals(baseDepth, activity.backStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d11_dialogSemantics_noParentMutation() {
        val scenario = launchDCase(11)
        try {
            scenario.onActivity { activity ->
                val baseDepth = activity.backStackDepth
                activity.openDialog()
                assertTrue(activity.isDialogVisible)
                assertTrue(activity.dismissModal())
                assertEquals(baseDepth, activity.backStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d12_fullscreenDialogSemantics_noParentMutation() {
        val scenario = launchDCase(12)
        try {
            scenario.onActivity { activity ->
                val baseDepth = activity.backStackDepth
                activity.openFullScreenDialog()
                assertTrue(activity.isFullScreenDialogVisible)
                assertTrue(activity.dismissModal())
                assertEquals(baseDepth, activity.backStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    private fun launchDCase(number: Int): ActivityScenario<Nav2HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.D, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }
}
