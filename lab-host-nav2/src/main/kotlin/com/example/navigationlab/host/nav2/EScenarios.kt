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
