package com.example.navigationlab.engine.orchestrator

import com.example.navigationlab.contracts.LabResult
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.RunMode

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
     * Execute the step described by [instruction].
     * Implementations should record trace events via LabTraceStore.
     */
    suspend fun execute(instruction: String)
}
