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
val T4_SCENARIOS: List<LabScenario> = listOf(
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
