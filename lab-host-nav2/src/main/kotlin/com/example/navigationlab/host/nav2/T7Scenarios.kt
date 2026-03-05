package com.example.navigationlab.host.nav2

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definitions for T7 topology (Nav2 route -> Nav3 leaf screen).
 * Tests forward interop: Nav2 navigating to a destination that renders Nav3 NavDisplay.
 */
val T7_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.B, 4),
        title = "Nav2 route renders Nav3 leaf (Nav2 -> Nav3)",
        topology = TopologyId.T7,
        preconditions = listOf(
            "Nav2 NavHost active with standard routes and one Nav3 leaf route",
        ),
        steps = listOf(
            LabStep(1, "Create Nav2 NavHost with home, screen_a, and nav3_leaf routes",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate from home to screen_a (pure Nav2 hop)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Navigate from screen_a to nav3_leaf route (crosses into Nav3)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Inside Nav3 leaf: navigate from LeafHome to LeafDetail",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Pop Nav3 leaf back stack (LeafDetail -> LeafHome)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Pop Nav2 back stack (nav3_leaf -> screen_a)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(7, "Verify Nav2 back stack is home + screen_a",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Nav3 NavDisplay renders correctly inside Nav2 composable route",
            "Nav3 leaf back stack is independent from Nav2 back stack",
            "Popping from Nav3 leaf root returns control to Nav2",
            "Nav2 back stack intact after Nav3 leaf interaction",
        ),
    ),
)
