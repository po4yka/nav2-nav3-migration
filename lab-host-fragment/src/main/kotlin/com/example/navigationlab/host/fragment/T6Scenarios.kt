package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definitions for T6 topology (Fragment host -> ComposeView -> internal Nav2).
 * Tests fragment-hosted Compose with Nav2 navigation, including activity-level
 * fragment transactions (B06) and Nav2 dialog result passing (B07).
 */
val T6_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.B, 6),
        title = "Nav2 route triggers fragment transaction in activity container",
        topology = TopologyId.T6,
        preconditions = listOf(
            "ComposeNav2Fragment loaded with Nav2 NavHost in activity main container",
            "Activity-level overlay container available (initially hidden)",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity with ComposeNav2Fragment in main container",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2 from home to screen_a inside fragment",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "From Nav2 context: trigger activity-level fragment transaction (add overlay)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify overlay fragment is visible alongside Nav2 content",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Dismiss activity overlay",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(6, "Verify Nav2 still on screen_a after overlay dismissed",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Nav2 inside Fragment can coexist with activity-level fragment overlay",
            "Activity overlay does not disrupt Nav2 back stack",
            "Dismissing overlay restores Nav2 content visibility",
            "Nav2 navigation state preserved across overlay lifecycle",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.B, 7),
        title = "Fragment screen launches Nav2 dialog and returns result",
        topology = TopologyId.T6,
        preconditions = listOf(
            "ComposeNav2Fragment loaded with Nav2 NavHost including dialog route",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity with ComposeNav2Fragment",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2 from home to screen_a inside fragment",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Navigate Nav2 to dialog route (result_dialog)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Dialog confirms and returns result via savedStateHandle",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify dialog result received and Nav2 back on screen_a",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Nav2 dialog renders correctly inside Fragment-hosted ComposeView",
            "Dialog result passed back via savedStateHandle",
            "Popping dialog restores previous Nav2 destination",
            "Fragment lifecycle stable across dialog show/dismiss",
        ),
    ),
)
