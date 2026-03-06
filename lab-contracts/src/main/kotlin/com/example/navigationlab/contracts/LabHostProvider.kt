package com.example.navigationlab.contracts

import android.content.Context
import android.content.Intent

/**
 * Contract for host modules that provide lab scenarios and can launch
 * their host activity for a given case.
 */
interface LabHostProvider {
    /** All scenarios registered by this provider. */
    val scenarios: List<LabScenario>

    /** Create an Intent to launch the host activity for the given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent
}
