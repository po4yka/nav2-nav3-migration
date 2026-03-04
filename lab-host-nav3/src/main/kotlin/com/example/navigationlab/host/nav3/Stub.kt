package com.example.navigationlab.host.nav3

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity

/**
 * Entry point for the Nav3 host module.
 * Provides T3 topology scenarios and factory for launching the host activity.
 */
object Nav3HostProvider {

    /** All scenarios registered by this module (T3 topology). */
    val scenarios: List<LabScenario> = T3_SCENARIOS

    /** Create an Intent to launch the T3 host activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav3HostActivity.createIntent(context, caseId, runMode)
}
