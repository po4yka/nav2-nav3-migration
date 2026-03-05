package com.example.navigationlab.host.nav2

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** E-family scenarios on T2 (Compose + Nav2 back behavior). */
val E_T2_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.E, 1),
        title = "Compose BackHandler only",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2HostActivity loaded with Compose NavHost routes",
            "Current compose destination owns a BackHandler without fragment callbacks",
        ),
        steps = listOf(
            LabStep(1, "Create Nav2HostActivity and navigate home -> screen_a",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.STACK_CHANGE)),
            LabStep(2, "Dispatch back while compose BackHandler is active",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify current destination is home (compose layer consumed first back)",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Dispatch back at home",
                expectedEvents = listOf(TraceEventType.BACK_EVENT)),
        ),
        invariants = listOf(
            "Compose BackHandler consumes back before activity exit",
            "Back consumption pops one Nav2 destination only",
            "Root back is delegated to host exit behavior",
        ),
    ),
)

/** E-family scenario on T7 interop (popup -> child modal -> parent route unwind order). */
val E_T7_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.E, 9),
        title = "Back-order chain: popup -> child modal -> parent route",
        topology = TopologyId.T7,
        preconditions = listOf(
            "Nav2ToNav3InteropActivity supports parent dialog and Nav3 leaf modal keys",
            "Parent and child layers can be inspected independently",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav2 Home -> nav3_leaf and open child Nav3 dialog modal",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Open parent Nav2 dialog on top of child modal",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dispatch back: dismiss parent dialog first",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Dispatch back again: dismiss child modal before popping parent route",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Dispatch back at child root: pop nav3_leaf route to Nav2 parent",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Back dispatch prioritizes top-most parent dialog over child modal",
            "Child modal is dismissed before Nav2 parent route pop",
            "Unwind order is deterministic across parent/child interop layers",
        ),
    ),
)
