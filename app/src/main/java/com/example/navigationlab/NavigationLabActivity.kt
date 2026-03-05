package com.example.navigationlab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.navigationlab.contracts.LabResult
import com.example.navigationlab.contracts.ResultStatus
import com.example.navigationlab.engine.NavigationLabEngine
import com.example.navigationlab.engine.casebrowser.CaseBrowserScreen
import com.example.navigationlab.execution.LabRuntimeStepExecutor
import com.example.navigationlab.launch.CaseHostLauncher
import com.example.navigationlab.results.ResultSummaryPanel
import com.example.navigationlab.results.TraceTimelinePanel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class NavigationLabActivity : ComponentActivity() {

    private val engine: NavigationLabEngine by inject()
    private val caseHostLauncher: CaseHostLauncher by inject()
    private var executionJob: Job? = null
    private val _latestResult = MutableStateFlow<LabResult?>(null)
    private val latestResult = _latestResult.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val traceEvents by engine.traceStore.events.collectAsState()
            val lastResult by latestResult.collectAsState()
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CaseBrowserScreen(
                            scenarios = engine.scenarios,
                            onCaseSelected = { caseId, runMode ->
                                Log.i(TAG, "Case selected: ${caseId.code}, mode: $runMode")
                                executionJob?.cancel()
                                executionJob = lifecycleScope.launch {
                                    val result = engine.execute(caseId, runMode, LabRuntimeStepExecutor())
                                    _latestResult.value = result
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

                                val launched = caseHostLauncher.launch(this@NavigationLabActivity, caseId, runMode)
                                if (!launched) {
                                    Log.e(TAG, "No host launcher registered for case ${caseId.code}")
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )

                        lastResult?.let { result ->
                            ResultSummaryPanel(
                                result = result,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }

                        TraceTimelinePanel(
                            events = traceEvents,
                            modifier = Modifier
                                .height(220.dp)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "NavigationLab"
    }
}
