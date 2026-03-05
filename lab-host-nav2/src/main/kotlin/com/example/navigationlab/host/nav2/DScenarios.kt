package com.example.navigationlab.host.nav2

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** D-family modal semantics baselines on T2 pure Nav2 host. */
val D_T2_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.D, 10),
        title = "Pure Nav2 bottom-sheet semantics baseline",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2HostActivity supports bottom_sheet dialog destination",
            "Root route is home before modal open",
        ),
        steps = listOf(
            LabStep(1, "Open bottom_sheet route from home",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify sheet is visible and Nav2 host remains alive behind it",
                expectedEvents = listOf(TraceEventType.INVARIANT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dismiss sheet route",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify current route restored to home with no extra pop",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Sheet open adds one modal destination on top of parent route",
            "Sheet dismiss removes only modal destination",
            "Parent Nav2 stack remains unchanged after dismiss",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 11),
        title = "Pure Nav2 dialog semantics baseline",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2HostActivity supports result_dialog destination",
            "Nav2 stack has stable parent route before dialog open",
        ),
        steps = listOf(
            LabStep(1, "Open result_dialog route from home",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify dialog visibility and preserved parent route",
                expectedEvents = listOf(TraceEventType.INVARIANT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dismiss dialog route",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify Nav2 back stack depth equals pre-dialog depth",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Dialog open/close does not mutate parent history",
            "Only modal route is popped on dismiss",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 12),
        title = "Pure Nav2 fullscreen dialog semantics baseline",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2HostActivity supports full_screen_dialog destination",
            "Fullscreen dialog is rendered with platform width disabled",
        ),
        steps = listOf(
            LabStep(1, "Open full_screen_dialog route from home",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify fullscreen modal visibility and parent stack stability",
                expectedEvents = listOf(TraceEventType.INVARIANT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dismiss fullscreen dialog route",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify host remains at original parent route",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Fullscreen dialog behaves as single top-most overlay destination",
            "Dismiss does not trigger extra parent stack pops",
        ),
    ),
)
