package com.example.navigationlab.host.nav3

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabHostProvider
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.host.nav3.hosts.Nav3FragmentIslandActivity
import com.example.navigationlab.host.nav3.hosts.Nav3NestedChainActivity
import com.example.navigationlab.host.nav3.hosts.Nav3HostActivity
import com.example.navigationlab.host.nav3.hosts.Nav3ToNav2InteropActivity

/**
 * Entry point for the Nav3 host module.
 * Provides T3 topology scenarios and factory for launching the host activity.
 */
object Nav3HostProvider : LabHostProvider {

    /** All scenarios registered by this module (T3 + D/F/G cases). */
    override val scenarios: List<LabScenario> = T3_SCENARIOS + D_T3_SCENARIOS + F_T3_SCENARIOS + G_T3_SCENARIOS

    /** Create an Intent to launch the T3 host activity for a given case. */
    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav3HostActivity.createIntent(context, caseId, runMode)
}

/**
 * Entry point for the T8 interop topology (Nav3 root -> Nav2 leaf).
 * Provides B03 scenario and factory for launching the interop activity.
 */
object Nav3ToNav2InteropProvider : LabHostProvider {

    /** All scenarios registered by this provider (T8 + E/F/G/H interop cases). */
    override val scenarios: List<LabScenario> =
        T8_SCENARIOS + E_T8_SCENARIOS + F_T8_SCENARIOS + G_T8_SCENARIOS + H_T8_SCENARIOS

    /** Create an Intent to launch the T8 interop activity for a given case. */
    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav3ToNav2InteropActivity.createIntent(context, caseId, runMode)
}

/**
 * Entry point for the T5 topology (Nav3 root -> legacy fragment island).
 * Provides B05 scenario and factory for launching the fragment island activity.
 */
object Nav3FragmentIslandProvider : LabHostProvider {

    /** All scenarios registered by this provider (T5 + D/E/G/H island cases). */
    override val scenarios: List<LabScenario> = T5_SCENARIOS + D_T5_SCENARIOS + E_T5_SCENARIOS + G_T5_SCENARIOS + H_T5_SCENARIOS

    /** Create an Intent to launch the T5 fragment island activity for a given case. */
    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav3FragmentIslandActivity.createIntent(context, caseId, runMode)
}

/**
 * Entry point for B09 nested chain stress test (Nav3 -> Nav2 -> Fragment -> Nav2 dialog).
 * Provides B09 scenario and factory for launching the nested chain activity.
 */
object Nav3NestedChainProvider : LabHostProvider {

    /** All scenarios registered by this provider (B09). */
    override val scenarios: List<LabScenario> = B09_SCENARIOS

    /** Create an Intent to launch the B09 nested chain activity for a given case. */
    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav3NestedChainActivity.createIntent(context, caseId, runMode)
}

/**
 * C-family: XML-in-Compose bridge scenario using T5 (Nav3FragmentIslandActivity).
 * Covers C02 (Compose route hosts XML via AndroidViewBinding and keeps state).
 */
object XmlInComposeBridgeProvider : LabHostProvider {

    /** C-family scenario using T5 topology. */
    override val scenarios: List<LabScenario> = C_NAV3_SCENARIOS

    /** Create an Intent to launch the T5 fragment island activity. */
    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        Nav3FragmentIslandActivity.createIntent(context, caseId, runMode)
}
