package com.example.navigationlab.host.nav3

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.host.nav3.hosts.Nav3FragmentIslandActivity
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity
import com.example.navigationlab.host.nav3.hosts.Nav3ToNav2InteropActivity

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

/**
 * Entry point for the T8 interop topology (Nav3 root -> Nav2 leaf).
 * Provides B03 scenario and factory for launching the interop activity.
 */
object Nav3ToNav2InteropProvider {

    /** All scenarios registered by this provider (T8 topology). */
    val scenarios: List<LabScenario> = T8_SCENARIOS

    /** Create an Intent to launch the T8 interop activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav3ToNav2InteropActivity.createIntent(context, caseId, runMode)
}

/**
 * Entry point for the T5 topology (Nav3 root -> legacy fragment island).
 * Provides B05 scenario and factory for launching the fragment island activity.
 */
object Nav3FragmentIslandProvider {

    /** All scenarios registered by this provider (T5 topology). */
    val scenarios: List<LabScenario> = T5_SCENARIOS

    /** Create an Intent to launch the T5 fragment island activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav3FragmentIslandActivity.createIntent(context, caseId, runMode)
}
