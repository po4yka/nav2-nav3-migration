package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** G-family restore scenario on T3 pure Nav3 host. */
val G_T3_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.G, 3),
        title = "Process-death style restore of Nav3 back stack keys",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3HostActivity uses typed key back stack",
            "Saved state contains key stack snapshot before recreation",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav3 Home -> ScreenA -> ScreenB",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(2, "Capture and persist key snapshot, then recreate host",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(3, "Restore key stack from saved state boundary",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.LIFECYCLE)),
            LabStep(4, "Validate restored key ordering and depth",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Key ordering is preserved across restore boundary",
            "No duplicate keys are appended during recreation",
            "Root key remains present as first stack element",
        ),
    ),
)

/** G-family restore scenario on T5 Nav3 + legacy fragment island. */
val G_T5_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.G, 4),
        title = "Restore legacy fragment stack in Nav3 island mode",
        topology = TopologyId.T5,
        preconditions = listOf(
            "Nav3FragmentIslandActivity can host legacy fragment island entry",
            "Island fragment transactions are added to back stack",
        ),
        steps = listOf(
            LabStep(1, "Navigate to legacy island and push fragment stack depth > 1",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.STACK_CHANGE)),
            LabStep(2, "Recreate host while island stack is non-empty",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(3, "Rehydrate Nav3 root key and legacy island fragment hierarchy",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify child fragment stack remains unwindable",
                expectedEvents = listOf(TraceEventType.INVARIANT, TraceEventType.BACK_EVENT)),
        ),
        invariants = listOf(
            "Island fragment stack remains attached after recreation",
            "Nav3 parent stack and legacy child stack keep deterministic order",
            "Back unwind still prefers child island entries before Nav3 parent pop",
        ),
    ),
)

/** G-family rotation scenario on T8 Nav3 root + Nav2 leaf graph. */
val G_T8_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.G, 2),
        title = "Rotation with Nav3 stack and Nav2 leaf graph",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav3ToNav2InteropActivity can host Nav2 leaf key destination",
            "Both parent Nav3 and child Nav2 stacks can be non-root",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav3 Home -> Nav2Leaf and Nav2 leaf_home -> leaf_detail",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Trigger configuration change while both stacks are active",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(3, "Recreate host and recover parent/child stack snapshots",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Assert interop back order remains deterministic after restore",
                expectedEvents = listOf(TraceEventType.INVARIANT, TraceEventType.BACK_EVENT)),
        ),
        invariants = listOf(
            "Rotation path does not crash with nested parent/child stacks",
            "Parent Nav3 and child Nav2 stacks remain independently unwindable",
            "Restore does not introduce duplicated interop entries",
        ),
    ),
)
