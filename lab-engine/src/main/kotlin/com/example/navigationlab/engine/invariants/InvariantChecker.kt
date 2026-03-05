package com.example.navigationlab.engine.invariants

import com.example.navigationlab.contracts.InvariantResult
import com.example.navigationlab.contracts.LabTraceEvent

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

/** Default no-op checker that marks all invariants as passed. */
object PassThroughChecker : InvariantChecker {
    override fun check(description: String, events: List<LabTraceEvent>): InvariantResult =
        InvariantResult(description = description, passed = true)
}
