package com.example.navigationlab.contracts

/**
 * Definition of a lab test scenario.
 * Each scenario maps to one case from the catalog (A01-H05).
 */
data class LabScenario(
    /** Unique case identifier. */
    val id: LabCaseId,
    /** Short title describing the scenario. */
    val title: String,
    /** Which host topology this scenario runs on. */
    val topology: TopologyId,
    /** Preconditions that must hold before execution. */
    val preconditions: List<String> = emptyList(),
    /** Ordered execution steps. */
    val steps: List<LabStep>,
    /** Invariant descriptions checked after each step or at the end. */
    val invariants: List<String> = emptyList(),
)
