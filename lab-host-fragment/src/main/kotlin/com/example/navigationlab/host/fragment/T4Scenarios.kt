package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definitions for T4 topology (Activity -> ComposeView + overlay FrameLayout).
 * Covers container/host ownership cases that require dual containers.
 */

/** A02, A03 -- dual-container base cases. */
private val T4_BASE_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.A, 2),
        title = "Late container inflation fallback path",
        topology = TopologyId.T4,
        preconditions = listOf(
            "Overlay FrameLayout starts GONE",
            "Compose base container is visible with content",
        ),
        steps = listOf(
            LabStep(1, "Verify overlay container is GONE",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(2, "Attempt to show fragment (overlay is hidden)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Make overlay visible as fallback inflation",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Add fragment to newly-visible overlay",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(5, "Verify fragment renders in overlay and base content is preserved",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Overlay container becomes visible only after fallback inflation",
            "Base Compose content remains unchanged after overlay inflation",
            "Fragment is correctly hosted in overlay container",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.A, 3),
        title = "Dual-container visibility race (base vs overlay controller conflict)",
        topology = TopologyId.T4,
        preconditions = listOf(
            "Compose base container has content",
            "Overlay FrameLayout starts GONE",
        ),
        steps = listOf(
            LabStep(1, "Display content in Compose base container",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Make overlay visible and add fragment",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(3, "Verify both containers are visible simultaneously",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Update base Compose content while overlay is visible",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify overlay fragment is not disturbed by base update",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(6, "Dismiss overlay and verify base content is intact",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
        ),
        invariants = listOf(
            "Both containers can be visible simultaneously without conflict",
            "Updating base content does not affect overlay fragment",
            "Dismissing overlay does not corrupt base Compose state",
        ),
    ),
)

/** A06, A07 -- inflation race and config change cases. */
private val T4_EXTENDED_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.A, 6),
        title = "Navigation call arrives before host inflation complete",
        topology = TopologyId.T4,
        preconditions = listOf(
            "Activity created but ComposeView content not yet set",
            "Overlay FrameLayout starts GONE",
        ),
        steps = listOf(
            LabStep(1, "Begin activity creation with deferred host setup",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Trigger navigation request before ComposeView has content",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Complete host inflation (set ComposeView content)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify queued navigation request is applied",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
            LabStep(5, "Verify final UI reflects the navigation target",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Navigation call before inflation does not crash",
            "Queued navigation executes once host is ready",
            "Final UI state matches the navigation target",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.A, 7),
        title = "Rotation while overlay visible keeps container ownership stable",
        topology = TopologyId.T4,
        preconditions = listOf(
            "Compose base container has content",
            "Overlay FrameLayout is visible with a fragment",
        ),
        steps = listOf(
            LabStep(1, "Display content in Compose base container",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Show overlay with fragment",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(3, "Verify both containers are visible before rotation",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Trigger configuration change (simulate rotation)",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(5, "Verify overlay is still visible after rotation",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(6, "Verify base Compose content is preserved after rotation",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(7, "Dismiss overlay and verify base content is still correct",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
        ),
        invariants = listOf(
            "Container ownership does not transfer during rotation",
            "Overlay visibility persists across configuration change",
            "Fragment back stack survives rotation",
            "No duplicate fragments or containers after config change",
        ),
    ),
)

/** All T4 scenarios: base (A02, A03) + extended (A06, A07). */
val T4_SCENARIOS: List<LabScenario> = T4_BASE_SCENARIOS + T4_EXTENDED_SCENARIOS
