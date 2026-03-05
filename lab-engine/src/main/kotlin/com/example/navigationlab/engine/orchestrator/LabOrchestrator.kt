package com.example.navigationlab.engine.orchestrator

import com.example.navigationlab.contracts.LabResult
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.contracts.TraceEventType

/**
 * Orchestrates execution of a [LabScenario].
 * Host topology modules provide concrete step executors;
 * the orchestrator drives the step sequence and collects results.
 */
interface LabOrchestrator {

    /** Execute the scenario and return the aggregated result. */
    suspend fun run(scenario: LabScenario, mode: RunMode): LabResult
}

/**
 * Executes a single scenario step. Provided by host topology modules.
 */
fun interface StepExecutor {
    /**
     * Execute the given [step].
     * Implementations should record trace events via LabTraceStore.
     */
    suspend fun execute(step: LabStep): StepExecutionResult
}

/**
 * Output of a single step execution.
 *
 * [observedEvents] should reflect what the executor actually observed while
 * performing the step, so the orchestrator can validate [LabStep.expectedEvents].
 */
data class StepExecutionResult(
    val observedEvents: List<TraceEventType> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
