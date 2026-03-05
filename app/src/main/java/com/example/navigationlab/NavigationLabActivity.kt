package com.example.navigationlab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.navigationlab.contracts.ResultStatus
import com.example.navigationlab.engine.NavigationLabEngine
import com.example.navigationlab.engine.casebrowser.CaseBrowserScreen
import com.example.navigationlab.engine.orchestrator.HeuristicStepExecutor
import com.example.navigationlab.launch.CaseHostLauncher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class NavigationLabActivity : ComponentActivity() {

    private val engine: NavigationLabEngine by inject()
    private val caseHostLauncher: CaseHostLauncher by inject()
    private val stepExecutor = HeuristicStepExecutor()
    private var executionJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CaseBrowserScreen(
                        scenarios = engine.scenarios,
                        onCaseSelected = { caseId, runMode ->
                            Log.i(TAG, "Case selected: ${caseId.code}, mode: $runMode")
                            executionJob?.cancel()
                            executionJob = lifecycleScope.launch {
                                val result = engine.execute(caseId, runMode, stepExecutor)
                                val failedInvariants = result.invariantResults.count { !it.passed }
                                when (result.status) {
                                    ResultStatus.PASS -> {
                                        Log.i(
                                            TAG,
                                            "Case ${caseId.code} finished: PASS (${result.trace.size} trace events)",
                                        )
                                    }

                                    else -> {
                                        Log.w(
                                            TAG,
                                            "Case ${caseId.code} finished: ${result.status} " +
                                                "(failed invariants: $failedInvariants, trace events: ${result.trace.size}) " +
                                                (result.errorMessage?.let { "| error=$it" } ?: ""),
                                        )
                                    }
                                }
                            }

                            val launched = caseHostLauncher.launch(this, caseId, runMode)
                            if (!launched) {
                                Log.e(TAG, "No host launcher registered for case ${caseId.code}")
                            }
                        },
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "NavigationLab"
    }
}
