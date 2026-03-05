package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** G-family state-restore scenarios on T6 (fragment host + Compose Nav2 leaf). */
val G_T6_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.G, 1),
        title = "Rotation with Nav2 stack and fragment overlay",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity is active with ComposeNav2Fragment attached",
            "Activity-level overlay container is available above Nav2 content",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav2 home -> screen_a and open activity overlay fragment",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Trigger configuration change (rotation) while overlay is visible",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Rebind fragment and Nav2 controller state after recreation",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Validate top route/overlay ordering remains deterministic",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Rotation does not crash while mixed Nav2 and fragment overlay layers are active",
            "Host recreates with deterministic container hierarchy on T6",
            "Trace timeline retains explicit recreation boundary for diagnostics",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.G, 7),
        title = "Restore when dialog or sheet is top-most destination",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity Nav2 graph includes dialog and sheet destinations",
            "Dialog/sheet route can be top-most when recreation begins",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav2 home -> bottom_sheet (or result_dialog)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Trigger configuration change while modal destination is top-most",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(3, "Recreate host and inspect modal destination consistency",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Dismiss restored modal and verify parent route integrity",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Top-most modal route restore does not corrupt parent Nav2 stack",
            "Modal dismissal after recreation consumes only one stack layer",
            "No duplicate modal entries appear after restore cycle",
        ),
    ),
)
