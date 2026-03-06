package com.example.navigationlab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    private val _isExecuting = MutableStateFlow(false)
    private val isExecuting = _isExecuting.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val traceVersion by engine.traceStore.eventVersion.collectAsState()
            val traceEvents = remember(traceVersion) {
                engine.traceStore.snapshot()
            }
            val lastResult by latestResult.collectAsState()
            val executing by isExecuting.collectAsState()
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var resultsExpanded by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                        CaseBrowserScreen(
                            scenarios = engine.scenarios,
                            onCaseSelected = { caseId, runMode ->
                                Log.i(TAG, "Case selected: ${caseId.code}, mode: $runMode")
                                val launched = caseHostLauncher.launch(this@NavigationLabActivity, caseId, runMode)
                                if (!launched) {
                                    val error = "No host launcher registered for case ${caseId.code}"
                                    Log.e(TAG, error)
                                    _latestResult.value = LabResult(
                                        caseId = caseId,
                                        status = ResultStatus.ERROR,
                                        errorMessage = error,
                                    )
                                    return@CaseBrowserScreen
                                }

                                resultsExpanded = true
                                executionJob?.cancel()
                                executionJob = lifecycleScope.launch {
                                    _isExecuting.value = true
                                    try {
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
                                    } finally {
                                        _isExecuting.value = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )

                        if (executing) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                            )
                        }

                        AnimatedVisibility(
                            visible = resultsExpanded && lastResult != null,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 360.dp)
                                    .clickable { resultsExpanded = false },
                            ) {
                                lastResult?.let { result ->
                                    ResultSummaryPanel(
                                        result = result,
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                    )
                                }

                                TraceTimelinePanel(
                                    events = traceEvents,
                                    modifier = Modifier
                                        .heightIn(max = 160.dp)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "NavigationLab"
    }
}
