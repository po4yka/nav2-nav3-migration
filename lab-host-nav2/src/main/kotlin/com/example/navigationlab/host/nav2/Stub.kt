package com.example.navigationlab.host.nav2

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity

/**
 * Entry point for the Nav2 host module.
 * Provides T2 topology scenarios and factory for launching the host activity.
 */
object Nav2HostProvider {

    /** All scenarios registered by this module (T2 topology). */
    val scenarios: List<LabScenario> = T2_SCENARIOS

    /** Create an Intent to launch the T2 host activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav2HostActivity.createIntent(context, caseId, runMode)
}
