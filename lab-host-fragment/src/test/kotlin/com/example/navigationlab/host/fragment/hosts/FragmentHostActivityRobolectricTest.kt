package com.example.navigationlab.host.fragment.hosts

import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.host.fragment.R
import com.example.navigationlab.host.fragment.fragments.LabStubFragment
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
class FragmentHostActivityRobolectricTest {

    @Test
    fun createIntent_populatesTopologyHeaderWithCaseAndMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.A, 1),
            runMode = "scripted",
        )

        val activity = Robolectric.buildActivity(FragmentHostActivity::class.java, intent).setup().get()
        val topologyLabel = activity.findViewById<TextView>(R.id.tvTopologyLabel).text.toString()
        val expectedLabel = activity.getString(
            R.string.topology_label_with_case_mode,
            activity.getString(R.string.topology_t1),
            "A01",
            RunMode.SCRIPTED,
        )

        assertEquals(expectedLabel, topologyLabel)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun missingCaseId_finishesImmediately() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, FragmentHostActivity::class.java)

        val activity = Robolectric.buildActivity(FragmentHostActivity::class.java, intent).setup().get()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun fragmentApi_updatesBackStackDepth() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = FragmentHostActivity.createIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.A, 1),
            runMode = "scripted",
        )
        val activity = Robolectric.buildActivity(FragmentHostActivity::class.java, intent).setup().get()

        activity.supportFragmentManager.executePendingTransactions()
        assertEquals(0, activity.backStackDepth)

        activity.showFragment(
            LabStubFragment.newInstance("Screen A", FragmentHostActivity.COLORS[1]),
            addToBackStack = true,
        )
        activity.supportFragmentManager.executePendingTransactions()
        assertEquals(1, activity.backStackDepth)

        activity.addOverlayFragment(
            LabStubFragment.newInstance("Overlay", FragmentHostActivity.COLORS[2]),
        )
        activity.supportFragmentManager.executePendingTransactions()
        assertEquals(2, activity.backStackDepth)
    }
}
