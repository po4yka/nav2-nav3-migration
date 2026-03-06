package com.example.navigationlab.engine.orchestrator

import com.example.navigationlab.contracts.LabResult
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.contracts.TraceEventType

/**
 * Orchestrates execution of a [LabScenario].
 *
 * Host topology modules provide concrete [StepExecutor] implementations;
 * the orchestrator drives the step sequence, delegates each step to the
 * executor, records trace events, checks invariants, and aggregates
 * results into a [LabResult].
 *
 * The default implementation ([DefaultLabOrchestrator][com.example.navigationlab.engine.orchestrator.DefaultLabOrchestrator])
 * supports three [RunMode]s:
 * - **MANUAL** -- steps execute sequentially with no inter-step delay.
 * - **SCRIPTED** -- steps execute sequentially with a configurable delay between steps.
 * - **STRESS** -- the full step sequence repeats many times to surface timing-sensitive failures.
 *
 * Implementations must be safe to call from a coroutine on the **main dispatcher**.
 */
interface LabOrchestrator {

    /**
     * Execute all steps of [scenario] under the given [mode] and return the
     * aggregated [LabResult], including the captured trace and invariant outcomes.
     */
    suspend fun run(scenario: LabScenario, mode: RunMode): LabResult
}

/**
 * Executes a single scenario step. Provided by host topology modules.
 *
 * ## Contract
 *
 * Implementers **must**:
 * 1. Perform the navigation action described by [LabStep.instruction].
 * 2. Return a [StepExecutionResult] whose [StepExecutionResult.observedEvents]
 *    lists every [TraceEventType] that was actually observed during execution.
 *    The orchestrator compares these against [LabStep.expectedEvents] to
 *    determine whether the step passed; missing expected events cause an
 *    invariant failure.
 * 3. Optionally populate [StepExecutionResult.metadata] with arbitrary
 *    key-value pairs (e.g., `"step"` index, `"depth"` info). This metadata
 *    is attached to the trace events the orchestrator records on the
 *    executor's behalf.
 *
 * Implementers **should**:
 * - Record their own fine-grained trace events via [LabTraceStore.record][com.example.navigationlab.engine.LabTraceStore.record]
 *   for richer diagnostics (the orchestrator also records step markers and
 *   observed-event summaries automatically).
 *
 * ## Threading
 *
 * [execute] is always called on the **main dispatcher**. Implementations must
 * not block the calling coroutine; long-running or blocking work should be
 * dispatched to an appropriate dispatcher internally.
 *
 * ## Timeout
 *
 * The orchestrator does **not** enforce a per-step timeout. If a step can
 * hang (e.g., waiting for an animation or network callback), the executor
 * itself is responsible for applying a timeout and returning an appropriate
 * error result or throwing an exception. Uncaught exceptions are caught by
 * the orchestrator and surfaced as an [ResultStatus.ERROR][com.example.navigationlab.contracts.ResultStatus.ERROR]
 * in the [LabResult].
 */
fun interface StepExecutor {
    /**
     * Execute the given [step] and return the execution result.
     *
     * @param step The step to execute, containing the instruction text,
     *   step index, and the list of expected [TraceEventType]s.
     * @return A [StepExecutionResult] with the observed events and optional metadata.
     */
    suspend fun execute(step: LabStep): StepExecutionResult
}

/**
 * Output of a single step execution.
 *
 * @property observedEvents The [TraceEventType]s that the executor actually
 *   observed while performing the step. The orchestrator de-duplicates these
 *   and compares them against [LabStep.expectedEvents]; any expected event
 *   not present in this list is reported as a missing-event invariant failure.
 * @property metadata Arbitrary key-value pairs attached to the trace events
 *   the orchestrator records for this step. Common keys include `"step"`
 *   (the step index) and `"depth"` (back-stack depth), but any string pair
 *   is accepted.
 */
data class StepExecutionResult(
    val observedEvents: List<TraceEventType> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
