package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * D-family scenarios using T6 Nav2 host (Fragment -> ComposeView -> internal Nav2
 * with activity overlay container).
 */
val D_NAV2_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.D, 1),
        title = "Nav2 bottomSheet route behavior baseline",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Nav2 graph includes bottom_sheet route (sheet-styled dialog destination)",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity and verify home route is visible",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2: home -> screen_a",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Open bottom_sheet route",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify sheet overlay is visible above compose content",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Dismiss sheet with back",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Verify Nav2 returns to screen_a",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Sheet route appears as top-most destination without replacing underlying screen",
            "Back dismisses sheet before parent destinations",
            "Underlying Nav2 destination remains screen_a after sheet dismissal",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 2),
        title = "Nav2 dialog route behavior baseline",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Nav2 graph includes result_dialog destination",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity and verify home route is visible",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2: home -> screen_a",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Open result_dialog route",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify dialog is visible as floating destination",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Dismiss dialog with back",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Verify Nav2 returns to screen_a",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Dialog route is rendered above host content",
            "Dialog dismissal only removes dialog destination",
            "Parent stack remains intact after dialog close",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 3),
        title = "Custom dialogFullScreen behavior baseline",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Nav2 graph includes full_screen_dialog destination",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity and verify home route is visible",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2: home -> screen_b",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Open full_screen_dialog route",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify fullscreen dialog overlays entire content area",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Dismiss fullscreen dialog with back",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Verify Nav2 returns to screen_b",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Fullscreen dialog destination renders edge-to-edge",
            "Back dismisses fullscreen dialog before parent destination",
            "Underlying destination remains screen_b after dismiss",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 4),
        title = "Overlay fragment above Nav2 sheet; back order correctness",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Activity overlay container is available",
            "Nav2 graph includes bottom_sheet route",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2: home -> screen_a -> bottom_sheet",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Add activity-level overlay fragment above sheet",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify overlay fragment is top-most over sheet",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Back press once: dismiss activity overlay only",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(6, "Verify bottom_sheet remains visible",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(7, "Back press again: dismiss bottom_sheet",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(8, "Verify Nav2 returns to screen_a",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Back unwind order is overlay fragment -> Nav2 sheet -> base destination",
            "Overlay dismissal does not pop Nav2 sheet",
            "Sheet dismissal does not pop screen_a",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 6),
        title = "Simultaneous nested overlays from child and activity managers",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Nav2 graph includes full_screen_dialog destination",
            "Activity overlay container is available",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2: home -> screen_b -> full_screen_dialog",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Add activity overlay fragment while fullscreen dialog is visible",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify both overlays are active (activity overlay + Nav2 dialog)",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Dismiss activity overlay",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(6, "Verify fullscreen dialog is still visible",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(7, "Dismiss fullscreen dialog",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(8, "Verify Nav2 returns to screen_b",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Child-manager and activity-manager overlays can coexist without transaction conflicts",
            "Dismissing one overlay layer leaves the other intact",
            "Final unwind returns to the expected base route",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 7),
        title = "Sheet dismiss should not pop parent graph unexpectedly",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Nav2 graph includes bottom_sheet route",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2: home -> screen_a -> screen_b",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Open bottom_sheet route from screen_b",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Dismiss sheet with back",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify current route is still screen_b",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Sheet dismissal removes only the sheet destination",
            "Parent graph route (screen_b) remains current after dismiss",
            "No unexpected pop to screen_a or home after sheet close",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 8),
        title = "Fullscreen dialog should preserve transparent-background semantics",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Nav2 graph includes full_screen_dialog destination with translucent backdrop",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2: home -> screen_a",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Open full_screen_dialog route",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify translucent backdrop keeps underlying content context visible",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Dismiss fullscreen dialog",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Verify Nav2 returns to screen_a",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Fullscreen dialog uses transparent/translucent background semantics",
            "Dialog content stays fullscreen while backdrop remains translucent",
            "Underlying route state is preserved after dismissal",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.D, 9),
        title = "Transition from overlay fragment back into compose sheet state",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Activity overlay container is available",
            "Nav2 graph includes bottom_sheet route",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2: home -> screen_a -> bottom_sheet",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Add activity overlay fragment above sheet",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Back press to dismiss overlay fragment",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(5, "Verify bottom_sheet resumes as top-most compose destination",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(6, "Dismiss bottom_sheet",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(7, "Verify Nav2 returns to screen_a",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Overlay-to-sheet transition preserves compose destination state",
            "Overlay dismissal returns control to existing sheet destination",
            "Final sheet dismissal returns to the original base destination",
        ),
    ),
)

/**
 * D-family scenario using T6 Nav3 host (Fragment -> ComposeView -> Nav3 modal
 * with activity overlay container).
 */
val D_NAV3_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.D, 5),
        title = "Overlay fragment above Nav3 entry; back order correctness",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav3HostActivity loaded with ComposeNav3Fragment",
            "Nav3 modal key is available",
            "Activity overlay container is available",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav3HostActivity",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav3: Home -> ScreenA -> ResultModal",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Add activity-level overlay fragment above Nav3 modal",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify overlay fragment is top-most over Nav3 modal entry",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Back press once: dismiss activity overlay only",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.CONTAINER_CHANGE)),
            LabStep(6, "Verify Nav3 modal entry remains visible",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(7, "Back press again: pop Nav3 modal entry",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(8, "Verify Nav3 returns to ScreenA",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Back unwind order is activity overlay -> Nav3 modal",
            "Dismissing activity overlay does not mutate Nav3 stack",
            "Popping Nav3 modal returns to previous Nav3 destination",
        ),
    ),
)
