package com.example.navigationlab.execution

import com.example.navigationlab.back.BackChain
import com.example.navigationlab.back.BackLayer
import com.example.navigationlab.back.BackOrchestrator
import com.example.navigationlab.back.BackOutcome
import com.example.navigationlab.back.BackPopper
import com.example.navigationlab.back.RootExitPolicy
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TraceEventType
import com.example.navigationlab.deeplink.DeeplinkDispatchResult
import com.example.navigationlab.deeplink.DeeplinkRequest
import com.example.navigationlab.deeplink.DeeplinkSimulator
import com.example.navigationlab.deeplink.DeeplinkSource
import com.example.navigationlab.engine.orchestrator.HeuristicStepExecutor
import com.example.navigationlab.engine.orchestrator.StepExecutionResult
import com.example.navigationlab.engine.orchestrator.StepExecutor
import java.util.Locale

/**
 * Runtime step executor that integrates real lab infrastructure:
 * - [BackOrchestrator] for deterministic back resolution.
 * - [DeeplinkSimulator] for F-family deeplink routing simulation.
 * - [HeuristicStepExecutor] as baseline event inference.
 */
class LabRuntimeStepExecutor(
    private val fallbackExecutor: StepExecutor = HeuristicStepExecutor(),
    private val deeplinkSimulator: DeeplinkSimulator = DeeplinkSimulator.default(),
) : StepExecutor {

    private var overlayDepth: Int = 0
    private var childDepth: Int = 0
    private var navDepth: Int = 1
    private var rootExitCount: Int = 0

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
        val text = step.instruction.lowercase(Locale.ROOT)

        trackForwardDepths(text)

        if (containsAny(text, "reset root-exit gate")) {
            backOrchestrator.resetRootExitGate()
            metadata["back_gate_reset"] = "true"
        }

        if (containsAny(text, "deeplink")) {
            val deeplinkResult = dispatchDeeplink(text)
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

        if (containsAny(text, "back", "pop", "dismiss")) {
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

    private fun trackForwardDepths(text: String) {
        if (containsAny(text, "navigate", "push", "switch") && !containsAny(text, "back", "pop")) {
            navDepth += 1
        }

        if (containsAny(
                text,
                "show overlay",
                "add overlay",
                "open overlay",
                "overlay fragment",
                "open dialog",
                "show dialog",
                "open sheet",
                "show sheet",
                "open modal",
                "show modal",
                "open popup",
                "show popup",
            )
        ) {
            overlayDepth += 1
        }

        if (containsAny(text, "fragment")
            && containsAny(text, "replace", "add ", "show")
            && !containsAny(text, "overlay")
        ) {
            childDepth += 1
        }
    }

    private fun dispatchDeeplink(text: String): DeeplinkDispatchResult {
        val source = if (containsAny(text, "internal source", "source=internal")) {
            DeeplinkSource.INTERNAL
        } else {
            DeeplinkSource.INTENT
        }

        val path = Regex("/[a-z0-9_\\-/]*[a-z0-9_\\-]")
            .find(text)
            ?.value
            ?: "/unknown/feature-entry"

        val request = DeeplinkRequest(
            path = path,
            source = source,
            hostReady = !containsAny(text, "host not ready"),
            sendToChannelActive = containsAny(text, "channel active", "sendtochannelactive=true"),
            restoredAfterProcessDeath = containsAny(text, "process death", "restore flag", "restored"),
        )
        return deeplinkSimulator.dispatch(request)
    }

    private fun containsAny(text: String, vararg tokens: String): Boolean =
        tokens.any { token -> text.contains(token) }
}
