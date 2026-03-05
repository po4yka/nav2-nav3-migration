package com.example.navigationlab.contracts

/** A single execution step within a lab scenario. */
data class LabStep(
    /** 1-based step index. */
    val index: Int,
    /** What the step does (e.g., "Navigate to screen B"). */
    val instruction: String,
    /** Expected trace events after this step completes. */
    val expectedEvents: List<TraceEventType> = emptyList(),
    /**
     * Typed action consumed by executors.
     *
     * Defaults to event-driven action derived from [expectedEvents] so existing
     * scenario definitions remain compatible.
     */
    val action: LabAction = LabAction.fromExpectedEvents(expectedEvents),
)
