package com.example.navigationlab.contracts

/**
 * Result of executing a lab scenario.
 */
data class LabResult(
    /** Which case was executed. */
    val caseId: LabCaseId,
    /** Overall outcome. */
    val status: ResultStatus,
    /** Collected trace events during execution. */
    val trace: List<LabTraceEvent> = emptyList(),
    /** Individual invariant check outcomes. */
    val invariantResults: List<InvariantResult> = emptyList(),
    /** Optional error message if the scenario failed to run. */
    val errorMessage: String? = null,
)

enum class ResultStatus {
    PASS,
    FAIL,
    SKIPPED,
    ERROR,
}

data class InvariantResult(
    /** Description of the invariant being checked. */
    val description: String,
    /** Whether the invariant held. */
    val passed: Boolean,
    /** Detail message when the invariant failed. */
    val failureMessage: String? = null,
)
