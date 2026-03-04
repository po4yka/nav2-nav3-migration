package com.example.navigationlab.engine.orchestrator

import com.example.navigationlab.contracts.InvariantResult
import com.example.navigationlab.contracts.LabResult
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.ResultStatus
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.contracts.TraceEventType
import com.example.navigationlab.engine.LabTraceStore
import com.example.navigationlab.engine.invariants.InvariantChecker
import com.example.navigationlab.engine.invariants.PassThroughChecker
import kotlinx.coroutines.delay

/**
 * Default orchestrator that drives scenario steps sequentially,
 * records trace events, and checks invariants after each step.
 */
class DefaultLabOrchestrator(
    private val traceStore: LabTraceStore,
    private val stepExecutor: StepExecutor,
    private val invariantChecker: InvariantChecker = PassThroughChecker,
    private val scriptedDelayMs: Long = 500L,
    private val stressRepetitions: Int = 50,
) : LabOrchestrator {

    override suspend fun run(scenario: LabScenario, mode: RunMode): LabResult {
        traceStore.startCase(scenario.id)

        return try {
            when (mode) {
                RunMode.MANUAL -> runManual(scenario)
                RunMode.SCRIPTED -> runScripted(scenario)
                RunMode.STRESS -> runStress(scenario)
            }
        } catch (e: Exception) {
            LabResult(
                caseId = scenario.id,
                status = ResultStatus.ERROR,
                trace = traceStore.snapshot(),
                errorMessage = e.message,
            )
        }
    }

    private suspend fun runManual(scenario: LabScenario): LabResult {
        // In manual mode, steps are driven externally (UI button presses).
        // This executes all steps for now; the UI layer will gate per-step later.
        return executeSteps(scenario)
    }

    private suspend fun runScripted(scenario: LabScenario): LabResult {
        val invariantResults = mutableListOf<InvariantResult>()

        for (step in scenario.steps) {
            traceStore.record(TraceEventType.STEP_MARKER, "Step ${step.index}: ${step.instruction}")
            stepExecutor.execute(step.instruction)
            invariantResults += checkInvariants(scenario)
            delay(scriptedDelayMs)
        }

        return buildResult(scenario, invariantResults)
    }

    private suspend fun runStress(scenario: LabScenario): LabResult {
        val invariantResults = mutableListOf<InvariantResult>()

        repeat(stressRepetitions) {
            traceStore.clear()
            traceStore.startCase(scenario.id)
            for (step in scenario.steps) {
                stepExecutor.execute(step.instruction)
            }
            invariantResults += checkInvariants(scenario)
        }

        return buildResult(scenario, invariantResults)
    }

    private suspend fun executeSteps(scenario: LabScenario): LabResult {
        val invariantResults = mutableListOf<InvariantResult>()

        for (step in scenario.steps) {
            traceStore.record(TraceEventType.STEP_MARKER, "Step ${step.index}: ${step.instruction}")
            stepExecutor.execute(step.instruction)
            invariantResults += checkInvariants(scenario)
        }

        return buildResult(scenario, invariantResults)
    }

    private fun checkInvariants(scenario: LabScenario): List<InvariantResult> {
        val events = traceStore.snapshot()
        return scenario.invariants.map { description ->
            val result = invariantChecker.check(description, events)
            traceStore.record(
                TraceEventType.INVARIANT,
                description,
                mapOf("passed" to result.passed.toString()),
            )
            result
        }
    }

    private fun buildResult(
        scenario: LabScenario,
        invariantResults: List<InvariantResult>,
    ): LabResult {
        val allPassed = invariantResults.all { it.passed }
        return LabResult(
            caseId = scenario.id,
            status = if (allPassed) ResultStatus.PASS else ResultStatus.FAIL,
            trace = traceStore.snapshot(),
            invariantResults = invariantResults,
        )
    }
}
