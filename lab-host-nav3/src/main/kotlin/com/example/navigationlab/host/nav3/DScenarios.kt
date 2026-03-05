package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** D-family modal semantics baselines on T3 pure Nav3 host. */
val D_T3_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.D, 13),
        title = "Pure Nav3 dialog-style modal semantics baseline",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3HostActivity supports DialogModal key",
            "Parent Nav3 stack is initialized with Home key",
        ),
        steps = listOf(
            LabStep(1, "Push DialogModal key on Nav3 stack",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify dialog-style overlay visibility with parent content preserved",
                expectedEvents = listOf(TraceEventType.INVARIANT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dismiss dialog-style modal key",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify Nav3 stack returns to pre-modal depth",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Dialog key is isolated to top of Nav3 back stack",
            "Dismiss pops only the modal key",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 14),
        title = "Pure Nav3 sheet-style modal semantics baseline",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3HostActivity supports SheetModal key",
            "Nav3 stack has deterministic root key",
        ),
        steps = listOf(
            LabStep(1, "Push SheetModal key on Nav3 stack",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify sheet-style modal is top-most entry",
                expectedEvents = listOf(TraceEventType.INVARIANT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dismiss sheet-style modal key",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify parent Nav3 entry is restored without mutation",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Sheet key overlays parent key without replacing it",
            "Modal dismiss leaves parent key order intact",
        ),
    ),
)

/** D-family legacy island modal layering baseline on T5 host. */
val D_T5_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.D, 15),
        title = "Legacy island DialogFragment/popup layering over Nav3 island",
        topology = TopologyId.T5,
        preconditions = listOf(
            "Nav3FragmentIslandActivity can enter LegacyIsland key and host fragments",
            "Island DialogFragment and parent popup overlays are available",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav3 Home -> LegacyIsland and render island fragment",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(2, "Show island DialogFragment modal on top of island content",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(3, "Open parent popup overlay while island modal is active",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Dismiss overlays in LIFO order: popup first, then island dialog",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.CONTAINER_CHANGE, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify LegacyIsland remains active and unwindable after dismiss chain",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Parent popup and island DialogFragment can coexist without host corruption",
            "Dismiss order respects top-most overlay first",
            "Legacy island fragment stack remains valid after modal chain",
        ),
    ),
)
