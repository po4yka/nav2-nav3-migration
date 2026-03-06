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
import java.util.concurrent.atomic.AtomicReference

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

    /**
     * Grouped depth state ensuring all four fields are read and written atomically.
     * Although current call sites run sequentially on the main thread, grouping into
     * an [AtomicReference] prevents subtle inconsistencies if concurrency is introduced.
     */
    private data class DepthState(
        val overlayDepth: Int = 0,
        val childDepth: Int = 0,
        val navDepth: Int = 1,
        val rootExitCount: Int = 0,
    )

    private val depthState = AtomicReference(DepthState())

    private val backOrchestrator = BackOrchestrator(
        chain = BackChain(
            overlay = BackPopper {
                var popped = false
                depthState.updateAndGet { state ->
                    if (state.overlayDepth > 0) {
                        popped = true
                        state.copy(overlayDepth = state.overlayDepth - 1)
                    } else {
                        state
                    }
                }
                popped
            },
            childStack = BackPopper {
                var popped = false
                depthState.updateAndGet { state ->
                    if (state.childDepth > 0) {
                        popped = true
                        state.copy(childDepth = state.childDepth - 1)
                    } else {
                        state
                    }
                }
                popped
            },
            navStack = BackPopper {
                var popped = false
                depthState.updateAndGet { state ->
                    if (state.navDepth > 1) {
                        popped = true
                        state.copy(navDepth = state.navDepth - 1)
                    } else {
                        state
                    }
                }
                popped
            },
            onRootExit = {
                depthState.updateAndGet { state ->
                    state.copy(rootExitCount = state.rootExitCount + 1)
                }
            },
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

        val snapshot = depthState.get()
        metadata["depth_overlay"] = snapshot.overlayDepth.toString()
        metadata["depth_child"] = snapshot.childDepth.toString()
        metadata["depth_nav"] = snapshot.navDepth.toString()
        metadata["root_exit_count"] = snapshot.rootExitCount.toString()
        metadata["root_exit_dispatched"] = backOrchestrator.isRootExitDispatched.toString()

        return StepExecutionResult(
            observedEvents = observed.toList(),
            metadata = metadata,
        )
    }

    private fun trackForwardDepths(action: LabAction) {
        depthState.updateAndGet { state ->
            var s = state
            if (LabActionCommand.TRACK_FORWARD_STACK in action.commands) {
                s = s.copy(navDepth = s.navDepth + 1)
            }
            if (LabActionCommand.TRACK_OVERLAY_OPEN in action.commands) {
                s = s.copy(overlayDepth = s.overlayDepth + 1)
            }
            if (LabActionCommand.TRACK_CHILD_PUSH in action.commands) {
                s = s.copy(childDepth = s.childDepth + 1)
            }
            s
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
