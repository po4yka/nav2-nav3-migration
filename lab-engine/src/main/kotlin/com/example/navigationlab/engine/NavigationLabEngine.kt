package com.example.navigationlab.engine

import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabResult
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.engine.orchestrator.DefaultLabOrchestrator
import com.example.navigationlab.engine.orchestrator.StepExecutor

/**
 * Main facade for the navigation interop lab.
 * Holds the scenario registry, trace store, and drives execution.
 */
class NavigationLabEngine(
    val traceStore: LabTraceStore = LabTraceStore(),
) {
    private val _scenarios = mutableListOf<LabScenario>()

    /** All registered scenarios, for display in the case browser. */
    val scenarios: List<LabScenario> get() = _scenarios.toList()

    /** Register a scenario. Called by host topology modules during initialization. */
    fun register(scenario: LabScenario) {
        _scenarios.add(scenario)
    }

    /** Register multiple scenarios at once. */
    fun registerAll(scenarios: List<LabScenario>) {
        _scenarios.addAll(scenarios)
    }

    /** Look up a scenario by case ID. */
    fun findScenario(caseId: LabCaseId): LabScenario? =
        _scenarios.find { it.id == caseId }

    /**
     * Execute a scenario with the given step executor and run mode.
     * The [stepExecutor] is provided by the host topology that owns the scenario.
     */
    suspend fun execute(
        caseId: LabCaseId,
        mode: RunMode,
        stepExecutor: StepExecutor,
    ): LabResult {
        val scenario = findScenario(caseId)
            ?: return LabResult(
                caseId = caseId,
                status = com.example.navigationlab.contracts.ResultStatus.ERROR,
                errorMessage = "Scenario not found: ${caseId.code}",
            )

        val orchestrator = DefaultLabOrchestrator(
            traceStore = traceStore,
            stepExecutor = stepExecutor,
        )

        return orchestrator.run(scenario, mode)
    }
}
