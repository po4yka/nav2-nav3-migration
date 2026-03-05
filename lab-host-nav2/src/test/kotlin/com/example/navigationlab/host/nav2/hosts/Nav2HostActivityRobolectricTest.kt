package com.example.navigationlab.host.nav2.hosts

import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.host.nav2.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Nav2HostActivityRobolectricTest {

    @Test
    fun createIntent_populatesTopologyHeaderWithCaseAndMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Nav2HostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.D, 11),
            runMode = "scripted",
        )

        val activity = Robolectric.buildActivity(Nav2HostActivity::class.java, intent).setup().get()
        val topologyLabel = activity.findViewById<TextView>(R.id.tvTopologyLabel).text.toString()
        val expectedLabel = activity.getString(
            R.string.topology_label_with_case_mode,
            activity.getString(R.string.topology_t2),
            "D11",
            RunMode.SCRIPTED,
        )

        assertEquals(expectedLabel, topologyLabel)
        assertFalse(activity.isFinishing)
    }
}
