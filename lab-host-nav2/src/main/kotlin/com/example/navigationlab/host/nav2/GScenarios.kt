package com.example.navigationlab.host.nav2

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** G-family state and argument scenarios on T2 Nav2 baseline host. */
val G_T2_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.G, 5),
        title = "Non-saveable argument injection failure path",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2HostActivity is active with deterministic route mapping",
            "Synthetic argument injector can provide non-saveable payload",
        ),
        steps = listOf(
            LabStep(1, "Launch Nav2HostActivity and navigate to argument-receiving destination",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.STACK_CHANGE)),
            LabStep(2, "Inject non-saveable argument surrogate (lambda-like payload)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
            LabStep(3, "Trigger recreate boundary and evaluate recovery/fallback behavior",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify failure is explicit and does not corrupt host stack",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Non-saveable payload path is detected explicitly, not silently ignored",
            "Host remains alive and stack-inspectable after recreate cycle",
            "Fallback/default value behavior is deterministic across repeats",
        ),
    ),
)

/** G-family default-argument drift scenario on T7 Nav2->Nav3 interop host. */
val G_T7_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.G, 6),
        title = "Runtime default argument drift across interop boundary",
        topology = TopologyId.T7,
        preconditions = listOf(
            "Nav2ToNav3InteropActivity can resolve defaults in both parent and leaf layers",
            "Default source value may be computed at different lifecycle moments",
        ),
        steps = listOf(
            LabStep(1, "Navigate to Nav3 leaf route from Nav2 parent with deferred arg resolution",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Recreate host and re-enter same route with equivalent inputs",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Compare resolved default values before/after recreate",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Default argument resolution remains stable across recreation boundaries",
            "Interop bridge does not duplicate or drop default-derived arguments",
            "Parent and leaf stacks stay synchronized after replay",
        ),
    ),
)
