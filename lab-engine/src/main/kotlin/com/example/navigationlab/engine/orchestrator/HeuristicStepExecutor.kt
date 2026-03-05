package com.example.navigationlab.engine.orchestrator

import com.example.navigationlab.contracts.LabStep

/**
 * Default fallback [StepExecutor] for action-driven steps.
 *
 * Uses typed [LabStep.action] instead of parsing natural-language instructions.
 */
class HeuristicStepExecutor : StepExecutor {

    override suspend fun execute(step: LabStep): StepExecutionResult {
        val observed = step.action.observedEvents.distinct()

        return StepExecutionResult(
            observedEvents = observed,
            metadata = mapOf(
                "executor" to "action",
                "commands" to step.action.commands.joinToString(",") { it.name },
            ),
        )
    }
}
