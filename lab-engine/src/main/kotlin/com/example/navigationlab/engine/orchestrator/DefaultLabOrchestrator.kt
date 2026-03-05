package com.example.navigationlab.engine.orchestrator

import com.example.navigationlab.contracts.InvariantResult
import com.example.navigationlab.contracts.LabResult
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.ResultStatus
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.contracts.TraceEventType
import com.example.navigationlab.contracts.effectiveInvariantSpecs
import com.example.navigationlab.engine.LabTraceStore
import com.example.navigationlab.engine.invariants.InvariantChecker
import com.example.navigationlab.engine.invariants.TraceInvariantChecker
import kotlinx.coroutines.delay

/**
 * Default orchestrator that drives scenario steps sequentially,
 * records trace events, and checks invariants after each step.
 */
class DefaultLabOrchestrator(
    private val traceStore: LabTraceStore,
    private val stepExecutor: StepExecutor,
    private val invariantChecker: InvariantChecker = TraceInvariantChecker,
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
        return executeSteps(scenario, delayBetweenSteps = false)
    }

    private suspend fun runScripted(scenario: LabScenario): LabResult {
        return executeSteps(scenario, delayBetweenSteps = true)
    }

    private suspend fun runStress(scenario: LabScenario): LabResult {
        val invariantResults = mutableListOf<InvariantResult>()

        repeat(stressRepetitions) {
            traceStore.clear()
            traceStore.startCase(scenario.id)
            invariantResults += executeStepSequence(
                scenario = scenario,
                delayBetweenSteps = false,
            )
        }

        return buildResult(scenario, invariantResults)
    }

    private suspend fun executeSteps(
        scenario: LabScenario,
        delayBetweenSteps: Boolean,
    ): LabResult {
        val invariantResults = executeStepSequence(
            scenario = scenario,
            delayBetweenSteps = delayBetweenSteps,
        )
        return buildResult(scenario, invariantResults)
    }

    private suspend fun executeStepSequence(
        scenario: LabScenario,
        delayBetweenSteps: Boolean,
    ): List<InvariantResult> {
        val invariantResults = mutableListOf<InvariantResult>()
        for (step in scenario.steps) {
            traceStore.record(TraceEventType.STEP_MARKER, "Step ${step.index}: ${step.instruction}")
            val executionResult = stepExecutor.execute(step)
            val observedEvents = executionResult.observedEvents.distinct()

            recordObservedEvents(step, observedEvents, executionResult.metadata)
            invariantResults += validateStepExpectations(step, observedEvents)
            invariantResults += checkInvariants(scenario)

            if (delayBetweenSteps) {
                delay(scriptedDelayMs)
            }
        }
        return invariantResults
    }

    private fun recordObservedEvents(
        step: LabStep,
        observedEvents: List<TraceEventType>,
        metadata: Map<String, String>,
    ) {
        observedEvents.forEach { type ->
            traceStore.record(
                type = type,
                description = "Observed ${type.name} for step ${step.index}",
                metadata = mapOf(
                    "step" to step.index.toString(),
                    "scope" to "step_observation",
                ) + metadata,
            )
        }
    }

    private fun validateStepExpectations(
        step: LabStep,
        observedEvents: List<TraceEventType>,
    ): InvariantResult {
        val missing = step.expectedEvents
            .distinct()
            .filterNot { it in observedEvents }

        val passed = missing.isEmpty()
        val message = if (passed) {
            null
        } else {
            "Missing expected events: ${missing.joinToString { it.name }}"
        }

        traceStore.record(
            type = TraceEventType.INVARIANT,
            description = "Step ${step.index} expected event validation",
            metadata = buildMap {
                put("scope", "step_expectation")
                put("step", step.index.toString())
                put("passed", passed.toString())
                put("expected", step.expectedEvents.joinToString(",") { it.name })
                put("observed", observedEvents.joinToString(",") { it.name })
                if (!passed) put("missing", missing.joinToString(",") { it.name })
            },
        )

        return InvariantResult(
            description = "Step ${step.index} expected events",
            passed = passed,
            failureMessage = message,
        )
    }

    private fun checkInvariants(scenario: LabScenario): List<InvariantResult> {
        val events = traceStore.snapshot()
        return scenario.effectiveInvariantSpecs.map { spec ->
            val result = invariantChecker.check(spec, events)
            traceStore.record(
                TraceEventType.INVARIANT,
                spec.description,
                mapOf(
                    "passed" to result.passed.toString(),
                    "scope" to "scenario_invariant",
                    "invariant_type" to spec::class.simpleName.orEmpty(),
                ),
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
