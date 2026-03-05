package com.example.navigationlab.contracts

/**
 * A structured event in the lab trace timeline.
 * Captured by LabTraceStore during scenario execution.
 */
data class LabTraceEvent(
    /** Monotonic timestamp in milliseconds (from SystemClock.elapsedRealtime). */
    val timestampMs: Long,
    /** Category of the event. */
    val type: TraceEventType,
    /** Human-readable description of what happened. */
    val description: String,
    /** Optional key-value metadata for programmatic assertion. */
    val metadata: Map<String, String> = emptyMap(),
)

enum class TraceEventType {
    /** Navigation stack push/pop/replace. */
    STACK_CHANGE,
    /** Container visibility or inflation change. */
    CONTAINER_CHANGE,
    /** FragmentManager transaction (add/remove/replace/attach/detach). */
    FRAGMENT_TRANSACTION,
    /** System or user back event dispatched. */
    BACK_EVENT,
    /** Deeplink received and routed. */
    DEEPLINK,
    /** Lifecycle event (onCreate, onResume, etc.). */
    LIFECYCLE,
    /** Invariant check result (pass or fail). */
    INVARIANT,
    /** Scenario step boundary marker. */
    STEP_MARKER,
}
