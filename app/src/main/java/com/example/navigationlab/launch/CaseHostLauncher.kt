package com.example.navigationlab.launch

import android.app.Activity
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
        val intent = intentFactory(context, caseId, runMode.name).apply {
            // Defensive safety: non-activity callers require NEW_TASK.
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
        return true
    }
}
