package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** H-family transaction-order scenario on T5 legacy island topology. */
val H_T5_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.H, 3),
        title = "executePendingTransactions ordering guarantees",
        topology = TopologyId.T5,
        preconditions = listOf(
            "Nav3FragmentIslandActivity has pending legacy fragment transactions",
            "Scenario runner can force pending transaction drain",
        ),
        steps = listOf(
            LabStep(1, "Queue multiple legacy fragment operations in island container",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(2, "Call executePendingTransactions and capture resulting order",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dispatch back and verify unwind order after forced drain",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Validate ordering expectations in trace timeline",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Pending transaction drain produces stable, inspectable ordering",
            "Back handling remains deterministic after forced execution",
            "No orphaned fragment records remain after drain + unwind",
        ),
    ),
)

/** H-family concurrent-source race scenario on T8 interop topology. */
val H_T8_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.H, 4),
        title = "Concurrent navigation events from UI and deeplink sources",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav3ToNav2InteropActivity supports parent/child navigation updates",
            "Deeplink simulator can dispatch while UI navigation events are active",
        ),
        steps = listOf(
            LabStep(1, "Start UI-driven navigation chain into Nav2 leaf detail",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Dispatch deeplink update concurrently with UI event stream",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Resolve race by applying deterministic priority/order policy",
                expectedEvents = listOf(TraceEventType.INVARIANT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify final stack snapshot contains no interleaving corruption",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Concurrent UI/deeplink events do not corrupt parent or child stacks",
            "Resolution policy yields deterministic final destination",
            "Trace retains source attribution for both competing events",
        ),
    ),
)
