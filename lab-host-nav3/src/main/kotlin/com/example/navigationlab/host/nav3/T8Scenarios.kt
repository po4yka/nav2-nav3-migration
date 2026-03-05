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
    LabScenario(
        id = LabCaseId(CaseFamily.B, 10),
        title = "Cross-engine pop: pop from child engine should not corrupt parent stack",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav3 NavDisplay active with typed keys and one Nav2 leaf key",
            "Nav3 has multiple keys on back stack before entering Nav2 leaf",
        ),
        steps = listOf(
            LabStep(1, "Create Nav3 NavDisplay with Home and ScreenA on back stack",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav3: Home -> ScreenA (build parent stack depth)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Navigate Nav3: ScreenA -> Nav2Leaf key (cross into child engine)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Navigate Nav2 leaf: leaf_home -> leaf_detail (push in child)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Pop Nav2 leaf (leaf_detail -> leaf_home) -- child pop only",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Verify Nav3 parent back stack unchanged: [Home, ScreenA, Nav2Leaf]",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(7, "Pop Nav2 leaf to root (leaf_home is start) -- at child root now",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(8, "Pop Nav3 back stack (Nav2Leaf -> ScreenA)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(9, "Verify Nav3 back stack is [Home, ScreenA] -- parent stack intact",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Popping Nav2 child does not remove or modify Nav3 parent keys",
            "Nav3 back stack depth unchanged during Nav2 child navigation",
            "Parent can resume navigation after child pop completes",
            "No orphaned destinations or corrupted parent state",
            "Cross-engine boundary is cleanly maintained",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.B, 15),
        title = "Parent-level modal/popup while Nav2 leaf is active",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav3ToNav2InteropActivity supports parent dialog/popup entries",
            "Nav2 leaf can stay active while parent overlays are pushed",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav3 Home -> Nav2Leaf and push Nav2 leaf_detail",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Open parent dialog and then parent popup while child leaf stays active",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dismiss popup then dialog, verify Nav2 leaf route still leaf_detail",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
            LabStep(4, "Pop child Nav2 leaf detail and return to Nav3 parent key",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Parent overlays do not mutate child Nav2 leaf route or depth",
            "Overlay unwind order is LIFO at parent level",
            "Child navigation remains deterministic after parent overlay lifecycle",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.B, 16),
        title = "Nav2 leaf modal dismiss without mutating Nav3 parent stack",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav2 leaf supports dialog/sheet/fullscreen modal routes",
            "Nav3 parent stack has at least one non-root key before entering child",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav3 Home -> ScreenA -> Nav2Leaf",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Open Nav2 leaf dialog, sheet, and fullscreen dialog routes in sequence",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dismiss each child modal and verify Nav3 parent depth unchanged",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
            LabStep(4, "Pop Nav3 child key and confirm parent stack returns to ScreenA",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Child modal dismiss pops child stack only",
            "Nav3 parent stack is not mutated by Nav2 leaf modal lifecycle",
            "Parent navigation resumes correctly after child modal unwind",
        ),
    ),
)
