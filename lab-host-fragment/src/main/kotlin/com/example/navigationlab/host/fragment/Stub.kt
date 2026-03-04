package com.example.navigationlab.host.fragment

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.host.fragment.hosts.DualHostActivity
import com.example.navigationlab.host.fragment.hosts.FragmentHostActivity

/**
 * Entry point for the fragment host module.
 * Provides T1 topology scenarios and factory for launching the host activity.
 */
object FragmentHostProvider {

    /** All scenarios registered by this module (T1 topology). */
    val scenarios: List<LabScenario> = T1_SCENARIOS

    /** Create an Intent to launch the T1 host activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        FragmentHostActivity.createIntent(context, caseId, runMode)
}

/**
 * Entry point for the T4 dual-container topology.
 * Provides T4 scenarios and factory for launching DualHostActivity.
 */
object DualHostProvider {

    /** All scenarios registered by this module (T4 topology). */
    val scenarios: List<LabScenario> = T4_SCENARIOS

    /** Create an Intent to launch the T4 dual-container host activity. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        DualHostActivity.createIntent(context, caseId, runMode)
}
