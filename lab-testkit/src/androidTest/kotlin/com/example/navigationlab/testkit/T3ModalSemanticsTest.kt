package com.example.navigationlab.testkit

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class T3ModalSemanticsTest {

    @Test
    fun d13_dialogModalSemantics_noParentMutation() {
        val scenario = launchDCase(13)
        try {
            scenario.onActivity { activity ->
                val baseDepth = activity.backStackDepth
                activity.openDialogModal()
                assertTrue(activity.isDialogModalVisible)
                assertTrue(activity.dismissModalOrPopup())
                assertEquals(baseDepth, activity.backStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun d14_sheetModalSemantics_noParentMutation() {
        val scenario = launchDCase(14)
        try {
            scenario.onActivity { activity ->
                val baseDepth = activity.backStackDepth
                activity.openSheetModal()
                assertTrue(activity.isSheetModalVisible)
                assertTrue(activity.dismissModalOrPopup())
                assertEquals(baseDepth, activity.backStackDepth)
            }
        } finally {
            scenario.close()
        }
    }

    private fun launchDCase(number: Int): ActivityScenario<Nav3HostActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.D, number),
            runMode = "scripted",
        )
        return ActivityScenario.launch(intent)
    }
}
