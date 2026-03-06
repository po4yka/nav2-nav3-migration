package com.example.navigationlab.execution

import com.example.navigationlab.back.BackChain
import com.example.navigationlab.back.BackLayer
import com.example.navigationlab.back.BackOrchestrator
import com.example.navigationlab.back.BackOutcome
import com.example.navigationlab.back.BackPopper
import com.example.navigationlab.back.RootExitPolicy
import com.example.navigationlab.contracts.LabAction
import com.example.navigationlab.contracts.LabActionCommand
import com.example.navigationlab.contracts.LabDeeplinkSource
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TraceEventType
import com.example.navigationlab.deeplink.DeeplinkDispatchResult
import com.example.navigationlab.deeplink.DeeplinkRequest
import com.example.navigationlab.deeplink.DeeplinkSimulator
import com.example.navigationlab.deeplink.DeeplinkSource
import com.example.navigationlab.engine.orchestrator.HeuristicStepExecutor
import com.example.navigationlab.engine.orchestrator.StepExecutionResult
import com.example.navigationlab.engine.orchestrator.StepExecutor

/**
 * Runtime step executor that integrates lab infrastructure:
 * - [BackOrchestrator] for deterministic back resolution.
 * - [DeeplinkSimulator] for F-family deeplink routing simulation.
 * - [HeuristicStepExecutor] as action-driven baseline event provider.
 */
class LabRuntimeStepExecutor(
    private val fallbackExecutor: StepExecutor = HeuristicStepExecutor(),
    private val deeplinkSimulator: DeeplinkSimulator = DeeplinkSimulator.default(),
) : StepExecutor {

    @Volatile private var overlayDepth: Int = 0
    @Volatile private var childDepth: Int = 0
    @Volatile private var navDepth: Int = 1
    @Volatile private var rootExitCount: Int = 0

    private val backOrchestrator = BackOrchestrator(
        chain = BackChain(
            overlay = BackPopper {
                if (overlayDepth > 0) {
                    overlayDepth -= 1
                    true
                } else {
                    false
                }
            },
            childStack = BackPopper {
                if (childDepth > 0) {
                    childDepth -= 1
                    true
                } else {
                    false
                }
            },
            navStack = BackPopper {
                if (navDepth > 1) {
                    navDepth -= 1
                    true
                } else {
                    false
                }
            },
            onRootExit = { rootExitCount += 1 },
            rootExitPolicy = RootExitPolicy.SINGLE_SHOT,
        ),
    )

    override suspend fun execute(step: LabStep): StepExecutionResult {
        val baseline = fallbackExecutor.execute(step)
        val observed = linkedSetOf<TraceEventType>().apply { addAll(baseline.observedEvents) }
        val metadata = linkedMapOf<String, String>().apply { putAll(baseline.metadata) }
        val commands = step.action.commands

        trackForwardDepths(step.action)

        if (LabActionCommand.RESET_ROOT_EXIT_GATE in commands) {
            backOrchestrator.resetRootExitGate()
            metadata["back_gate_reset"] = "true"
        }

        if (LabActionCommand.DISPATCH_DEEPLINK in commands) {
            val deeplinkResult = dispatchDeeplink(step.action)
            observed += TraceEventType.DEEPLINK
            if (deeplinkResult.route != null) {
                observed += TraceEventType.STACK_CHANGE
            }
            metadata["deeplink_outcome"] = deeplinkResult.outcome.name
            metadata["deeplink_chain_outcome"] = deeplinkResult.chainOutcome.name
            metadata["deeplink_source"] = deeplinkResult.source.name
            metadata["deeplink_route"] = deeplinkResult.route.orEmpty()
            metadata["deeplink_consumed_by"] = deeplinkResult.consumedBy.orEmpty()
            metadata["deeplink_fallback_reason"] = deeplinkResult.fallbackReason?.name.orEmpty()
            metadata["deeplink_visited"] = deeplinkResult.visitedManagers.joinToString(",")
            metadata["deeplink_restored_after_process_death"] =
                deeplinkResult.restoredAfterProcessDeath.toString()
        }

        if (LabActionCommand.DISPATCH_BACK in commands) {
            val outcome = backOrchestrator.onBackPressed()
            observed += TraceEventType.BACK_EVENT
            when (outcome) {
                is BackOutcome.Consumed -> {
                    metadata["back_outcome"] = "CONSUMED"
                    metadata["back_layer"] = outcome.layer.name
                    when (outcome.layer) {
                        BackLayer.OVERLAY -> observed += TraceEventType.CONTAINER_CHANGE
                        BackLayer.CHILD_STACK,
                        BackLayer.NAV_STACK,
                        -> observed += TraceEventType.STACK_CHANGE
                    }
                }

                BackOutcome.RootExit -> {
                    metadata["back_outcome"] = "ROOT_EXIT"
                    metadata["back_layer"] = "ROOT"
                }

                BackOutcome.Ignored -> {
                    metadata["back_outcome"] = "IGNORED"
                    metadata["back_layer"] = "NONE"
                }
            }
        }

        metadata["depth_overlay"] = overlayDepth.toString()
        metadata["depth_child"] = childDepth.toString()
        metadata["depth_nav"] = navDepth.toString()
        metadata["root_exit_count"] = rootExitCount.toString()
        metadata["root_exit_dispatched"] = backOrchestrator.isRootExitDispatched.toString()

        return StepExecutionResult(
            observedEvents = observed.toList(),
            metadata = metadata,
        )
    }

    private fun trackForwardDepths(action: LabAction) {
        if (LabActionCommand.TRACK_FORWARD_STACK in action.commands) {
            navDepth += 1
        }

        if (LabActionCommand.TRACK_OVERLAY_OPEN in action.commands) {
            overlayDepth += 1
        }

        if (LabActionCommand.TRACK_CHILD_PUSH in action.commands) {
            childDepth += 1
        }
    }

    private fun dispatchDeeplink(action: LabAction): DeeplinkDispatchResult {
        val deeplink = action.deeplink
        val request = DeeplinkRequest(
            path = deeplink.path,
            source = when (deeplink.source) {
                LabDeeplinkSource.INTERNAL -> DeeplinkSource.INTERNAL
                LabDeeplinkSource.INTENT -> DeeplinkSource.INTENT
            },
            hostReady = deeplink.hostReady,
            sendToChannelActive = deeplink.sendToChannelActive,
            restoredAfterProcessDeath = deeplink.restoredAfterProcessDeath,
        )
        return deeplinkSimulator.dispatch(request)
    }
}
