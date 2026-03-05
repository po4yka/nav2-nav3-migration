package com.example.navigationlab.engine.invariants

import com.example.navigationlab.contracts.InvariantResult
import com.example.navigationlab.contracts.LabTraceEvent
import com.example.navigationlab.contracts.TraceEventType

/**
 * Checks invariant conditions against collected trace events.
 * Implementations are provided per-topology or per-case-family.
 */
fun interface InvariantChecker {

    /**
     * Evaluate the invariant against the current trace.
     *
     * @param description human-readable invariant description from the scenario
     * @param events trace events collected so far
     * @return result indicating whether the invariant holds
     */
    fun check(description: String, events: List<LabTraceEvent>): InvariantResult
}

/**
 * Default checker that is trace-aware and fails fast when step-level expected
 * event validation has not been satisfied.
 */
object TraceInvariantChecker : InvariantChecker {
    override fun check(description: String, events: List<LabTraceEvent>): InvariantResult {
        val stepExpectationResults = events.filter {
            it.type == TraceEventType.INVARIANT &&
                it.metadata["scope"] == "step_expectation"
        }

        if (stepExpectationResults.isEmpty()) {
            return InvariantResult(
                description = description,
                passed = false,
                failureMessage = "No step expectation validation events found.",
            )
        }

        val failedExpectations = stepExpectationResults.filter {
            it.metadata["passed"] == "false"
        }
        if (failedExpectations.isNotEmpty()) {
            val failedSteps = failedExpectations
                .mapNotNull { it.metadata["step"] }
                .distinct()
                .sorted()
                .joinToString(",")
            return InvariantResult(
                description = description,
                passed = false,
                failureMessage = "Step expectation failures detected (steps: $failedSteps).",
            )
        }

        return InvariantResult(description = description, passed = true)
    }
}
