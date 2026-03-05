package com.example.navigationlab.contracts

/**
 * Typed, executable invariant definitions.
 */
sealed interface LabInvariantSpec {
    val description: String

    /**
     * Verifies that step-level expected event validation has no failures.
     */
    data class StepExpectationsSatisfied(
        override val description: String,
    ) : LabInvariantSpec

    /**
     * Verifies that at least one trace event of [eventType] was observed.
     */
    data class TraceContainsEventType(
        val eventType: TraceEventType,
        override val description: String = "Trace contains at least one ${eventType.name} event",
    ) : LabInvariantSpec

    /**
     * Verifies that trace contains metadata key/value pair (optionally filtered by event type).
     */
    data class TraceContainsMetadata(
        val key: String,
        val expectedValue: String,
        val eventType: TraceEventType? = null,
        override val description: String =
            "Trace metadata contains $key=$expectedValue",
    ) : LabInvariantSpec
}

fun typedInvariants(descriptions: List<String>): List<LabInvariantSpec> =
    descriptions.map { description ->
        LabInvariantSpec.StepExpectationsSatisfied(description)
    }
