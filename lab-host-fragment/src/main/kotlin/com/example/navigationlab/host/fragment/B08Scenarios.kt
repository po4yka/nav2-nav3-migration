package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definition for B08: Fragment screen launches Nav3 modal entry and returns result.
 * Uses T6 topology pattern (Fragment host -> ComposeView -> Nav3 NavDisplay).
 */
val B08_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.B, 8),
        title = "Fragment screen launches Nav3 modal entry and returns result",
        topology = TopologyId.T6,
        preconditions = listOf(
            "ComposeNav3Fragment loaded with Nav3 NavDisplay in activity main container",
            "Nav3 NavDisplay has Home, ScreenA, and ResultModal keys",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav3HostActivity with ComposeNav3Fragment",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav3 from Home to ScreenA inside fragment",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Navigate Nav3 to ResultModal key (modal overlay appears)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Modal confirms and returns result via shared state",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify modal result received and Nav3 back on ScreenA",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Nav3 modal entry renders correctly inside Fragment-hosted ComposeView",
            "Modal result passed back via fragment shared state",
            "Popping modal restores previous Nav3 key (ScreenA)",
            "Fragment lifecycle stable across modal show/dismiss",
            "Nav3 back stack correctly reflects Home + ScreenA after modal dismiss",
        ),
    ),
)
