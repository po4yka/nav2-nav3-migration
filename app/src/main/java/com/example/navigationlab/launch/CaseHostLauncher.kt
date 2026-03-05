package com.example.navigationlab.launch

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.RunMode

/** Starts host activities for selected lab cases. */
class CaseHostLauncher(
    private val launchByCaseCode: Map<String, (Context, LabCaseId, String) -> Intent>,
) {
    fun launch(context: Context, caseId: LabCaseId, runMode: RunMode): Boolean {
        val intentFactory = launchByCaseCode[caseId.code] ?: return false
        context.startActivity(intentFactory(context, caseId, runMode.name))
        return true
    }
}
