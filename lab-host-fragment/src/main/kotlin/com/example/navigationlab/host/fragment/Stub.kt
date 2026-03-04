package com.example.navigationlab.host.fragment

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.host.fragment.hosts.DualHostActivity
import com.example.navigationlab.host.fragment.hosts.FragmentHostActivity
import com.example.navigationlab.host.fragment.hosts.FragmentNav2HostActivity
import com.example.navigationlab.host.fragment.hosts.FragmentNav3HostActivity

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

/**
 * Entry point for the T6 topology (Fragment host -> ComposeView -> internal Nav2).
 * Provides B06/B07 plus D01/D02/D03/D04/D06/D07/D08/D09 scenarios and factory
 * for launching FragmentNav2HostActivity.
 */
object FragmentNav2HostProvider {

    /** All scenarios registered by this provider (T6 Nav2 + D-family overlays). */
    val scenarios: List<LabScenario> = T6_SCENARIOS + D_NAV2_SCENARIOS

    /** Create an Intent to launch the T6 host activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        FragmentNav2HostActivity.createIntent(context, caseId, runMode)
}

/**
 * Entry point for B08 and D05 scenarios (Fragment host -> ComposeView -> Nav3 NavDisplay).
 * Uses T6 topology pattern with Nav3 instead of Nav2.
 */
object FragmentNav3HostProvider {

    /** All scenarios registered by this provider (B08 + D05). */
    val scenarios: List<LabScenario> = B08_SCENARIOS + D_NAV3_SCENARIOS

    /** Create an Intent to launch the B08 host activity for a given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        FragmentNav3HostActivity.createIntent(context, caseId, runMode)
}

/**
 * C-family: Compose-to-XML bridge scenarios using T4 (DualHostActivity).
 * Covers C01 (compose opens fragment), C04 (compose opens DialogFragment),
 * C07 (compose args -> fragment args), C08 (activity recreate bridge).
 */
object ComposeToXmlBridgeProvider {

    /** C-family scenarios using T4 topology. */
    val scenarios: List<LabScenario> = C_T4_SCENARIOS

    /** Create an Intent to launch the T4 dual-container host activity. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        DualHostActivity.createIntent(context, caseId, runMode)
}

/**
 * C-family: XML-to-Compose bridge scenarios using T6 (FragmentNav2HostActivity).
 * Covers C03 (fragment hosts ComposeView), C05 (fragment opens Compose dialog),
 * C06 (XML args -> Compose args).
 */
object XmlToComposeBridgeProvider {

    /** C-family scenarios using T6 topology. */
    val scenarios: List<LabScenario> = C_T6_SCENARIOS

    /** Create an Intent to launch the T6 host activity. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
        FragmentNav2HostActivity.createIntent(context, caseId, runMode)
}
