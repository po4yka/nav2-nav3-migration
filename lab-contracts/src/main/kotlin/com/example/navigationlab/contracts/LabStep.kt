package com.example.navigationlab.contracts

/** A single execution step within a lab scenario. */
data class LabStep(
    /** 1-based step index. */
    val index: Int,
    /** What the step does (e.g., "Navigate to screen B"). */
    val instruction: String,
    /** Expected trace events after this step completes. */
    val expectedEvents: List<TraceEventType> = emptyList(),
)
