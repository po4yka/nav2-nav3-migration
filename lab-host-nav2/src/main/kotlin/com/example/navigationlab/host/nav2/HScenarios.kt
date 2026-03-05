package com.example.navigationlab.host.nav2

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** H-family stress scenario on T2 pure Nav2 host. */
val H_T2_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.H, 2),
        title = "Rapid navigate/pop interleaving under Nav2",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2HostActivity is running on home route",
            "Scenario runner can execute rapid repeated navigation actions",
        ),
        steps = listOf(
            LabStep(1, "Burst navigate operations across screen_a/screen_b/screen_c",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(2, "Burst pop operations while navigate requests are still queued",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Repeat mixed navigate/pop loop for stress window",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(4, "Assert final back stack remains valid and bounded",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Rapid interleaving does not crash Nav2 host",
            "Back stack depth never drops below root baseline",
            "Final route resolves deterministically after stress cycle",
        ),
    ),
)
