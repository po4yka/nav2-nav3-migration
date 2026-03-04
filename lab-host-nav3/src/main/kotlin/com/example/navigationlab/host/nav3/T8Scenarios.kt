package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definitions for T8 topology (Nav3 key -> Nav2 leaf graph).
 * Tests forward interop: Nav3 navigating to a key that renders a Nav2 NavHost.
 */
val T8_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.B, 3),
        title = "Nav3 root key renders Nav2 leaf graph (Nav3 -> Nav2)",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav3 NavDisplay active with typed keys and one Nav2 leaf key",
        ),
        steps = listOf(
            LabStep(1, "Create Nav3 NavDisplay with Home, ScreenA, and Nav2Leaf keys",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate from Home to ScreenA (pure Nav3 hop)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Navigate from ScreenA to Nav2Leaf key (crosses into Nav2)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Inside Nav2 leaf: navigate from leaf_home to leaf_detail",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Pop Nav2 leaf back stack (leaf_detail -> leaf_home)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Pop Nav3 back stack (Nav2Leaf -> ScreenA)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(7, "Verify Nav3 back stack is Home + ScreenA",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Nav2 NavHost renders correctly inside Nav3 entry composable",
            "Nav2 leaf back stack is independent from Nav3 back stack",
            "Popping from Nav2 leaf root returns control to Nav3",
            "Nav3 back stack intact after Nav2 leaf interaction",
        ),
    ),
)
