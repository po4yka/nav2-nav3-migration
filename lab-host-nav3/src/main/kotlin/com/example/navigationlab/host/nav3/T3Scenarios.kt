package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definitions for T3 topology (Activity -> ComposeView -> Nav3 NavDisplay).
 * Covers Nav2/Nav3 interoperability cases (B-family) focused on pure Nav3 behavior.
 */
val T3_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.B, 2),
        title = "Pure Nav3 graph baseline (no fragments)",
        topology = TopologyId.T3,
        preconditions = listOf("Nav3 NavDisplay inflated with typed key destinations"),
        steps = listOf(
            LabStep(1, "Create NavDisplay with Home, ScreenA, ScreenB keys",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate from Home to ScreenA",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Navigate from ScreenA to ScreenB",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Pop back to ScreenA",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify back stack has Home and ScreenA",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Nav3 NavDisplay is active with typed key destinations",
            "Back stack correctly reflects navigation history",
            "Pop restores previous destination",
        ),
    ),
)
