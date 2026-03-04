package com.example.navigationlab.recipes

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

val R_BASIC_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 1),
        title = "Basic Nav3 with mutableStateListOf backstack",
        topology = TopologyId.T3,
        preconditions = listOf("Nav3 NavDisplay inflated with data-object routes"),
        steps = listOf(
            LabStep(1, "NavDisplay shows RouteA (green welcome screen)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Click to navigate' to push RouteB(\"123\")",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify RouteB shows 'Route id: 123'",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Press back to return to RouteA",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Back stack is managed via mutableStateListOf",
            "NavDisplay renders correct entry for each key",
            "Pop restores previous destination",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 2),
        title = "BasicSaveable Nav3 with rememberNavBackStack persistence",
        topology = TopologyId.T3,
        preconditions = listOf("Nav3 NavDisplay inflated with @Serializable NavKey routes"),
        steps = listOf(
            LabStep(1, "NavDisplay shows SaveableRouteA (green welcome screen)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Click to navigate' to push SaveableRouteB(\"123\")",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Rotate device or trigger config change",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(4, "Verify back stack restored: SaveableRouteB still visible",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Press back to return to SaveableRouteA",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Back stack survives configuration changes via rememberNavBackStack",
            "NavKey routes are @Serializable for state persistence",
            "Pop restores previous destination",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 3),
        title = "BasicDsl Nav3 with entryProvider DSL syntax",
        topology = TopologyId.T3,
        preconditions = listOf("Nav3 NavDisplay inflated with entryProvider { entry<T> {} } DSL"),
        steps = listOf(
            LabStep(1, "NavDisplay shows DslRouteA via entry<DslRouteA> DSL",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Click to navigate' to push DslRouteB(\"123\")",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify DslRouteB shows 'Route id: 123' via entry<DslRouteB> DSL",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Press back to return to DslRouteA",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "entryProvider DSL produces correct entries",
            "Type-safe key matching via reified entry<T>",
            "Pop restores previous destination",
        ),
    ),
)

val R_INTEROP_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 4),
        title = "Nav3 interop: AndroidFragment + AndroidView",
        topology = TopologyId.T5,
        preconditions = listOf(
            "FragmentActivity base class for AndroidFragment support",
            "Nav3 NavDisplay with entryProvider DSL",
        ),
        steps = listOf(
            LabStep(1, "NavDisplay shows FragmentRoute with embedded AndroidFragment",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(2, "Verify fragment text 'My Fragment' is displayed",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(3, "Tap 'Go to View' to push ViewRoute(\"123\")",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify AndroidView shows 'My View with key: 123'",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Press back to return to FragmentRoute",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "AndroidFragment<T> renders within Nav3 entry",
            "AndroidView renders within Nav3 entry",
            "Fragment lifecycle is managed by Nav3 entry lifecycle",
        ),
    ),
)

val R_MIGRATION_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 5),
        title = "Migration baseline: Nav2 with bottom navigation",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2 NavHost with 3 top-level routes (A, B, C) and dialog D",
            "Bottom NavigationBar with 3 items",
        ),
        steps = listOf(
            LabStep(1, "NavHost shows Route A as start destination",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Go to A1' to navigate within Feature A",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Tap Route B in bottom nav to switch top-level",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Tap 'Go to B1' to navigate within Feature B",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Tap Route A in bottom nav; verify A1 is popped (popUpTo A)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(6, "Tap 'Open dialog D' to show dialog",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Nav2 NavHost manages nested navigation graphs per top-level route",
            "Bottom nav uses popUpTo to clear back stack",
            "Dialog route displays as overlay",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 6),
        title = "Migration end: Nav3 with NavigationState + Navigator",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with NavigationState for multi-stack management",
            "Navigator handles forward/back by updating NavigationState",
            "Bottom NavigationBar with 3 items",
        ),
        steps = listOf(
            LabStep(1, "NavDisplay shows Route A via NavigationState start route",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Go to A1' - Navigator pushes to current stack",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Tap Route B in bottom nav - NavigationState switches topLevelRoute",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Tap 'Go to B1' - Navigator pushes to B stack",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Tap Route A in bottom nav - switches back; each stack independent",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(6, "Press back on non-start route goes to start route stack",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "NavigationState manages per-tab back stacks via rememberNavBackStack",
            "Navigator.navigate switches topLevelRoute for top-level keys",
            "Navigator.goBack pops or returns to start route",
            "toDecoratedEntries combines stacks with SaveableStateHolder",
        ),
    ),
)

val R_RESULTS_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 7),
        title = "Results via event bus (Channel-based)",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with ResultEventBus for passing results",
            "ResultEffect composable collects results from Channel flow",
        ),
        steps = listOf(
            LabStep(1, "Home screen shows 'Hello unknown person'",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Tell us about yourself' to push PersonDetailsForm",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Fill name and favorite color, tap Submit",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify Home screen shows submitted person data via ResultEffect",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "ResultEventBus delivers results via Channel flow",
            "ResultEffect collects results in LaunchedEffect",
            "Popping form screen returns to Home with updated data",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 8),
        title = "Results via state store (saveable State-based)",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with ResultStore for passing results",
            "ResultStore survives configuration changes via rememberSaveable",
        ),
        steps = listOf(
            LabStep(1, "Home screen shows 'Hello unknown person'",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Tell us about yourself' to push PersonDetailsForm",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Fill name and favorite color, tap Submit",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify Home screen reads result from ResultStore",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Rotate device; verify result persists via saveable store",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "ResultStore persists results across configuration changes",
            "getResultState reads current result from MutableState map",
            "setResult writes to saveable state map",
        ),
    ),
)
