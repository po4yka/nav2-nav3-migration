package com.example.navigationlab.engine.orchestrator

import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TraceEventType
import java.util.Locale

/**
 * Default fallback [StepExecutor] that infers observed event types from
 * step instruction text. This keeps scenario execution wired even when a
 * topology-specific executor is not yet implemented.
 */
class HeuristicStepExecutor : StepExecutor {

    override suspend fun execute(step: LabStep): StepExecutionResult {
        val text = step.instruction.lowercase(Locale.ROOT)
        val observed = linkedSetOf<TraceEventType>()

        if (containsAny(text, "navigate", "push", "switch", "open route")) {
            observed += TraceEventType.STACK_CHANGE
        }
        if (containsAny(text, "pop", "back", "dismiss")) {
            observed += TraceEventType.BACK_EVENT
            observed += TraceEventType.STACK_CHANGE
        }
        if (containsAny(text, "create", "inflate", "visible", "container", "overlay")) {
            observed += TraceEventType.CONTAINER_CHANGE
        }
        if (containsAny(text, "fragment", "transaction", "replace", "add ")) {
            observed += TraceEventType.FRAGMENT_TRANSACTION
        }
        if (containsAny(text, "deeplink")) {
            observed += TraceEventType.DEEPLINK
        }
        if (containsAny(text, "recreate", "rotation", "process death", "lifecycle")) {
            observed += TraceEventType.LIFECYCLE
        }
        if (containsAny(text, "verify", "assert", "validate")) {
            observed += TraceEventType.INVARIANT
        }

        return StepExecutionResult(
            observedEvents = observed.toList(),
            metadata = mapOf(
                "executor" to "heuristic",
                "instruction" to step.instruction,
            ),
        )
    }

    private fun containsAny(text: String, vararg tokens: String): Boolean =
        tokens.any { token -> text.contains(token) }
}

