package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** E-family scenarios on T1 (fragment callback ownership only). */
val E_T1_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.E, 2),
        title = "Fragment OnBackPressedCallback only",
        topology = TopologyId.T1,
        preconditions = listOf(
            "FragmentHostActivity loaded with root fragment in main container",
            "Top fragment registers OnBackPressedCallback with activity dispatcher",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentHostActivity and push root -> detail fragment",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(2, "Enable detail fragment OnBackPressedCallback",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(3, "Dispatch back: callback consumes event and pops detail fragment",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Dispatch back again at root",
                expectedEvents = listOf(TraceEventType.BACK_EVENT)),
        ),
        invariants = listOf(
            "Fragment callback receives back before activity default handling",
            "First back removes only detail fragment",
            "Second back is delegated to root exit behavior",
        ),
    ),
)

/** E-family scenarios on T4 dual-container host. */
val E_T4_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.E, 6),
        title = "Back from root triggers exit only once (double-back policy)",
        topology = TopologyId.T4,
        preconditions = listOf(
            "DualHostActivity running at root state (no overlay fragments)",
            "BackOrchestrator root policy configured as SINGLE_SHOT",
        ),
        steps = listOf(
            LabStep(1, "Create DualHostActivity with base container visible and overlay hidden",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Dispatch back at root: BackOrchestrator emits root-exit event",
                expectedEvents = listOf(TraceEventType.BACK_EVENT)),
            LabStep(3, "Dispatch back again without reset",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.INVARIANT)),
            LabStep(4, "Reset root-exit gate and dispatch back once more",
                expectedEvents = listOf(TraceEventType.BACK_EVENT)),
        ),
        invariants = listOf(
            "Root exit callback is fired only once while single-shot gate is active",
            "Repeated back at root does not trigger duplicate exit callbacks",
            "After gate reset, root exit can be emitted again exactly once",
        ),
    ),
)

/** E-family scenarios on T6 Nav2 host (fragment + compose nested back layers). */
val E_T6_NAV2_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.E, 3),
        title = "Two-tier back (Compose BackHandler + Fragment callback)",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Activity overlay container available above fragment content",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity and navigate Nav2 home -> screen_a",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.STACK_CHANGE)),
            LabStep(2, "Show activity overlay fragment above compose content",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Dispatch back: fragment callback consumes and dismisses overlay",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Dispatch back again: compose/nav layer consumes and pops to home",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Back priority is overlay callback first, then compose/nav stack",
            "Overlay dismissal does not mutate Nav2 stack depth",
            "Second back pops only Nav2 top destination",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.E, 5),
        title = "Back from mixed stack where top is activity overlay fragment",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Nav2 graph includes bottom_sheet destination",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav2: home -> screen_a -> bottom_sheet",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Add activity overlay fragment above bottom_sheet",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Back press: remove activity overlay only",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Back press: dismiss bottom_sheet",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Back press: pop screen_a -> home",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Mixed-stack unwind order is overlay fragment -> sheet destination -> base route",
            "Each back event consumes exactly one layer",
            "Final state returns to home without orphan overlays",
        ),
    ),
)

/** E-family scenario on T6 Nav3 host (pending transaction + modal back ordering). */
val E_T6_NAV3_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.E, 8),
        title = "Back while pending transaction exists (executePendingTransactions edge)",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav3HostActivity loaded with ComposeNav3Fragment",
            "Activity overlay container and Nav3 modal are both available",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav3: Home -> ScreenA -> ResultModal",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(2, "Start overlay fragment transaction and dispatch back before pending ops drain",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.BACK_EVENT)),
            LabStep(3, "Flush pending fragment ops with executePendingTransactions",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Dispatch back again to pop Nav3 modal",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Pending fragment transactions do not skip back priority layers",
            "First back resolves overlay layer before Nav3 modal pop",
            "After pending ops flush, modal stack remains consistent and poppable",
        ),
    ),
)
