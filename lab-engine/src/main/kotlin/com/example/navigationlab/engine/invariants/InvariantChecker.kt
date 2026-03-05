package com.example.navigationlab.engine.invariants

import com.example.navigationlab.contracts.InvariantResult
import com.example.navigationlab.contracts.LabInvariantSpec
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
     * @param spec typed invariant definition
     * @param events trace events collected so far
     * @return result indicating whether the invariant holds
     */
    fun check(spec: LabInvariantSpec, events: List<LabTraceEvent>): InvariantResult
}

/**
 * Default checker that is trace-aware and fails fast when step-level expected
 * event validation has not been satisfied.
 */
object TraceInvariantChecker : InvariantChecker {
    override fun check(spec: LabInvariantSpec, events: List<LabTraceEvent>): InvariantResult {
        return when (spec) {
            is LabInvariantSpec.StepExpectationsSatisfied ->
                checkStepExpectations(spec.description, events)
            is LabInvariantSpec.TraceContainsEventType ->
                checkEventTypePresence(spec, events)
            is LabInvariantSpec.TraceContainsMetadata ->
                checkMetadataPresence(spec, events)
        }
    }

    private fun checkStepExpectations(
        description: String,
        events: List<LabTraceEvent>,
    ): InvariantResult {
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

    private fun checkEventTypePresence(
        spec: LabInvariantSpec.TraceContainsEventType,
        events: List<LabTraceEvent>,
    ): InvariantResult {
        val passed = events.any { it.type == spec.eventType }
        return InvariantResult(
            description = spec.description,
            passed = passed,
            failureMessage = if (passed) null else "No ${spec.eventType.name} events found in trace.",
        )
    }

    private fun checkMetadataPresence(
        spec: LabInvariantSpec.TraceContainsMetadata,
        events: List<LabTraceEvent>,
    ): InvariantResult {
        val passed = events.any { event ->
            val typeMatches = spec.eventType == null || event.type == spec.eventType
            typeMatches && event.metadata[spec.key] == spec.expectedValue
        }
        return InvariantResult(
            description = spec.description,
            passed = passed,
            failureMessage = if (passed) {
                null
            } else {
                val scope = spec.eventType?.name ?: "ANY"
                "Missing metadata ${spec.key}=${spec.expectedValue} for event type $scope."
            },
        )
    }
}
