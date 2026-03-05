package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * C-family scenario definitions for XML <-> Compose screen connection.
 * T4-based scenarios (Compose root with fragment overlay): C01, C04, C07, C08.
 */
val C_T4_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.C, 1),
        title = "XML Activity hosts Compose root; compose route opens fragment",
        topology = TopologyId.T4,
        preconditions = listOf(
            "DualHostActivity inflated with ComposeView base + overlay FrameLayout",
            "ComposeView showing Home screen, overlay container hidden",
        ),
        steps = listOf(
            LabStep(1, "Create DualHostActivity with Compose base content visible",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Update Compose base content to Screen A via setBaseContent",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "From Compose context: open fragment overlay via showOverlayFragment",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify fragment overlay is visible alongside Compose base",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Dismiss overlay and verify Compose content still on Screen A",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.CONTAINER_CHANGE)),
        ),
        invariants = listOf(
            "Compose route can trigger fragment transaction in activity overlay",
            "Fragment overlay renders above ComposeView content",
            "Compose content preserves state while overlay is shown",
            "Dismissing fragment overlay does not affect Compose content",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.C, 4),
        title = "Compose screen opens XML dialog fragment and receives result",
        topology = TopologyId.T4,
        preconditions = listOf(
            "DualHostActivity inflated with ComposeView base content",
            "DialogFragment class available for showing via supportFragmentManager",
        ),
        steps = listOf(
            LabStep(1, "Create DualHostActivity with Compose base content visible",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "From Compose context: show XML DialogFragment via supportFragmentManager",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Verify DialogFragment is visible as floating window",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Confirm dialog and return result via fragment result API",
                expectedEvents = listOf(TraceEventType.BACK_EVENT)),
            LabStep(5, "Verify Compose screen received result from DialogFragment",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "XML DialogFragment renders correctly above ComposeView",
            "DialogFragment result propagated to Compose caller via fragment result API",
            "Dismissing dialog does not disrupt Compose content state",
            "Back press in dialog dismisses dialog without affecting Compose back stack",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.C, 7),
        title = "Compose route args -> Fragment arguments mapping correctness",
        topology = TopologyId.T4,
        preconditions = listOf(
            "DualHostActivity inflated with ComposeView base content",
            "Fragment overlay container available for receiving fragments with Bundle args",
        ),
        steps = listOf(
            LabStep(1, "Create DualHostActivity with Compose base content (label='Home', color=0)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Set Compose content with args: label='ProductDetail', colorIndex=2",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Open LabStubFragment in overlay with matching Bundle args (label='ProductDetail', color=COLORS[2])",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify fragment received correct args: label matches, color matches",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Dismiss overlay and verify Compose args unchanged",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Compose route args correctly mapped to Fragment Bundle arguments",
            "String args preserved across Compose->Fragment boundary",
            "Int/color args correctly serialized to Bundle",
            "Args survive fragment lifecycle (attach/detach)",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.C, 8),
        title = "Recreate activity and verify XML/Compose bridge rebuild order",
        topology = TopologyId.T4,
        preconditions = listOf(
            "DualHostActivity with Compose base + overlay fragment both visible",
            "Activity supports save/restore for overlay visibility and base content",
        ),
        steps = listOf(
            LabStep(1, "Create DualHostActivity with Compose base content (label='ScreenB', colorIndex=3)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Show overlay fragment with label='Overlay'",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Trigger activity recreation (simulate config change)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify XML layout re-inflated: ComposeView present in view hierarchy",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Verify ComposeView content restored: base label is 'ScreenB', colorIndex is 3",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(6, "Verify overlay restored: overlay container visible, fragment present",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(7, "Verify rebuild order: XML inflation -> ComposeView setContent -> overlay restore",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Activity recreates without crash",
            "XML layout correctly re-inflated before ComposeView setup",
            "ComposeView content restored from saved state (label + color)",
            "Overlay visibility restored from saved state",
            "No duplicate Compose content or fragment instances after recreate",
            "Back stack depth correct after restore",
        ),
    ),
)

/**
 * C-family scenario definitions for XML <-> Compose screen connection.
 * T6-based scenarios (Fragment host with internal ComposeView): C03, C05, C06.
 */
val C_T6_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.C, 3),
        title = "Fragment hosts ComposeView with internal nav graph",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity hosts ComposeNav2Fragment",
            "ComposeNav2Fragment contains ComposeView with Nav2 NavHost (home, screen_a, screen_b routes)",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity with ComposeNav2Fragment in main container",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify ComposeView renders inside fragment with Nav2 NavHost on home route",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(3, "Navigate Nav2 from home to screen_a inside fragment",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Navigate Nav2 from screen_a to screen_b inside fragment",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Pop Nav2 back (screen_b -> screen_a)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Verify Nav2 back stack reflects fragment-scoped navigation",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "ComposeView renders correctly inside Fragment onCreateView",
            "Nav2 NavHost operates within fragment lifecycle scope",
            "Nav2 back stack independent from activity-level back stack",
            "ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed applied",
            "Fragment re-creation preserves Nav2 navigation state",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.C, 5),
        title = "XML fragment opens Compose dialog/sheet and receives result",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity hosts ComposeNav2Fragment with Nav2 NavHost",
            "Nav2 NavHost includes dialog(result_dialog) route",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity with ComposeNav2Fragment",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav2 to screen_a (base destination for result receiver)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "From fragment context: trigger Nav2 dialog route (result_dialog)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify Compose dialog visible above fragment content",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Confirm dialog and return result via savedStateHandle",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(6, "Verify fragment-hosted Compose received dialog result ('confirmed')",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Compose dialog renders above Fragment-hosted ComposeView content",
            "Dialog result propagated via Nav2 savedStateHandle within fragment scope",
            "Dismissing Compose dialog restores previous Nav2 route in fragment",
            "Fragment lifecycle stable across Compose dialog show/dismiss",
            "XML fragment context can trigger and receive Compose dialog results",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.C, 6),
        title = "XML arguments -> Compose route args mapping correctness",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity hosts ComposeNav2Fragment",
            "ComposeNav2Fragment created with newInstance() (could carry Bundle args)",
            "Nav2 NavHost routes accept string-based route args",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity, pass case args via Intent extras",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify ComposeNav2Fragment received host context (case_id from Intent)",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(3, "Navigate Nav2 to screen_a route (base route arg mapping)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Pass argument from XML Fragment context to Nav2 route via navController.navigate",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify Compose route received correct args from XML context",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(6, "Verify args survive Nav2 back navigation within fragment",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Intent extras accessible from Fragment-hosted Compose context",
            "Fragment Bundle args mappable to Nav2 route arguments",
            "String args correctly passed across XML->Compose boundary",
            "Args survive navigation within fragment-scoped Nav2 graph",
            "No type coercion errors at XML/Compose boundary",
        ),
    ),
)
