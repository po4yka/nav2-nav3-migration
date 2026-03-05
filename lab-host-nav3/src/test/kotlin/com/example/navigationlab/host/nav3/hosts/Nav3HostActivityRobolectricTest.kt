package com.example.navigationlab.host.nav3.hosts

import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.host.nav3.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Nav3HostActivityRobolectricTest {

    @Test
    fun createIntent_populatesTopologyHeaderWithCaseAndMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.D, 13),
            runMode = "scripted",
        )

        val activity = Robolectric.buildActivity(Nav3HostActivity::class.java, intent).setup().get()
        val topologyLabel = activity.findViewById<TextView>(R.id.tvTopologyLabel).text.toString()
        val expectedLabel = activity.getString(
            R.string.topology_label_with_case_mode,
            activity.getString(R.string.topology_t3),
            "D13",
            RunMode.SCRIPTED,
        )

        assertEquals(expectedLabel, topologyLabel)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun missingCaseId_finishesImmediately() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, Nav3HostActivity::class.java)

        val activity = Robolectric.buildActivity(Nav3HostActivity::class.java, intent).setup().get()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun modalApi_updatesBackStackAndVisibility() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav3HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.D, 14),
            runMode = "scripted",
        )

        val activity = Robolectric.buildActivity(Nav3HostActivity::class.java, intent).setup().get()

        assertEquals(1, activity.backStackDepth)
        assertFalse(activity.isDialogModalVisible)
        assertFalse(activity.isSheetModalVisible)
        assertFalse(activity.isPopupVisible)

        activity.openDialogModal()
        assertEquals(2, activity.backStackDepth)
        assertTrue(activity.isDialogModalVisible)
        assertTrue(activity.dismissModalOrPopup())
        assertEquals(1, activity.backStackDepth)
        assertFalse(activity.isDialogModalVisible)

        activity.openSheetModal()
        assertEquals(2, activity.backStackDepth)
        assertTrue(activity.isSheetModalVisible)
        assertTrue(activity.dismissModalOrPopup())
        assertEquals(1, activity.backStackDepth)
        assertFalse(activity.isSheetModalVisible)

        assertFalse(activity.dismissModalOrPopup())
    }
}
