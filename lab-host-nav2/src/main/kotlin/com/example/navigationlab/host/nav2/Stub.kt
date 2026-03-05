package com.example.navigationlab.host.nav2

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.host.nav2.hosts.Nav2HostActivity
import com.example.navigationlab.host.nav2.hosts.Nav2ToNav3InteropActivity

/**
 * Entry point for the Nav2 host module.
 * Provides T2 topology scenarios and factory for launching the host activity.
 */
object Nav2HostProvider {

    /** All scenarios registered by this module (T2 + E/F/G/H cases). */
    val scenarios: List<LabScenario> =
        T2_SCENARIOS + D_T2_SCENARIOS + E_T2_SCENARIOS + F_T2_SCENARIOS + G_T2_SCENARIOS + H_T2_SCENARIOS

    /** Create an Intent to launch the T2 host activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav2HostActivity.createIntent(context, caseId, runMode)
}

/**
 * Entry point for the T7 interop topology (Nav2 root -> Nav3 leaf).
 * Provides B04 scenario and factory for launching the interop activity.
 */
object Nav2ToNav3InteropProvider {

    /** All scenarios registered by this provider (T7 + E09 + G06 cases). */
    val scenarios: List<LabScenario> = T7_SCENARIOS + E_T7_SCENARIOS + G_T7_SCENARIOS

    /** Create an Intent to launch the T7 interop activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav2ToNav3InteropActivity.createIntent(context, caseId, runMode)
}
