package com.example.navigationlab.contracts

/**
 * Typed execution contract for a scenario step.
 *
 * The runtime uses [commands] to perform deterministic side effects and uses
 * [observedEvents] for expectation validation.
 */
data class LabAction(
    val observedEvents: List<TraceEventType> = emptyList(),
    val commands: Set<LabActionCommand> = defaultCommands(observedEvents),
    val deeplink: LabDeeplinkRequest = LabDeeplinkRequest(),
) {
    companion object {
        fun fromExpectedEvents(expectedEvents: List<TraceEventType>): LabAction =
            LabAction(observedEvents = expectedEvents.distinct())
    }
}

enum class LabActionCommand {
    /** Perform a logical forward stack mutation (navigate/push/switch). */
    TRACK_FORWARD_STACK,
    /** Perform a back pop/dismiss through [BackOrchestrator]. */
    DISPATCH_BACK,
    /** Increment overlay depth for container/modal style operations. */
    TRACK_OVERLAY_OPEN,
    /** Increment child depth for nested fragment/container style operations. */
    TRACK_CHILD_PUSH,
    /** Dispatch typed deeplink request through simulator chain. */
    DISPATCH_DEEPLINK,
    /** Reset one-shot root-exit gate before back handling. */
    RESET_ROOT_EXIT_GATE,
}

enum class LabDeeplinkSource {
    INTENT,
    INTERNAL,
}

data class LabDeeplinkRequest(
    val path: String = "/unknown/feature-entry",
    val source: LabDeeplinkSource = LabDeeplinkSource.INTENT,
    val hostReady: Boolean = true,
    val sendToChannelActive: Boolean = false,
    val restoredAfterProcessDeath: Boolean = false,
)

private fun defaultCommands(events: List<TraceEventType>): Set<LabActionCommand> {
    val distinct = events.toSet()
    return buildSet {
        if (TraceEventType.BACK_EVENT in distinct) {
            add(LabActionCommand.DISPATCH_BACK)
        } else if (TraceEventType.STACK_CHANGE in distinct) {
            add(LabActionCommand.TRACK_FORWARD_STACK)
        }

        if (TraceEventType.DEEPLINK in distinct) {
            add(LabActionCommand.DISPATCH_DEEPLINK)
        }

        if (
            TraceEventType.CONTAINER_CHANGE in distinct &&
            TraceEventType.STACK_CHANGE in distinct &&
            TraceEventType.BACK_EVENT !in distinct
        ) {
            add(LabActionCommand.TRACK_OVERLAY_OPEN)
        }

        if (
            TraceEventType.FRAGMENT_TRANSACTION in distinct &&
            TraceEventType.STACK_CHANGE in distinct &&
            TraceEventType.BACK_EVENT !in distinct
        ) {
            add(LabActionCommand.TRACK_CHILD_PUSH)
        }
    }
}
