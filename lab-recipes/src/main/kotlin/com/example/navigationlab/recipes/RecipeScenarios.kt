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

val R_APP_STATE_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 9),
        title = "Multi-stack with tab history (LifoUniqueQueue)",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with AppState orchestrator",
            "3-tab layout with LifoUniqueQueue tracking tab visit order",
        ),
        steps = listOf(
            LabStep(1, "Open Tab Alpha (default start tab)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate to Alpha Detail",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Switch to Tab Beta via bottom bar",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Press back from Tab Beta root; returns to Tab Alpha (not exit)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Switch to Tab Gamma, then Tab Beta",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(6, "Press back from Tab Beta root; returns to Gamma (LIFO order)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "LifoUniqueQueue tracks tab visit order",
            "Back from tab root goes to previously visited tab",
            "Each tab preserves its own back stack",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 10),
        title = "Bottom bar visibility control",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with AppState + AnimatedVisibility bottom bar",
            "TOP_LEVEL_NAVIGATION_BEHAVIOR_MAP maps routes to HIDE/SAME_AS_PARENT",
        ),
        steps = listOf(
            LabStep(1, "Open Tab Alpha (bottom bar visible)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate to TabAlphaDetail (bar hides with slide animation)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Switch to Tab Beta, navigate to TabBetaDetail (bar stays same as parent)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Press back to Tab Alpha root (bar reappears)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "HIDE behavior hides bottom bar",
            "SAME_AS_PARENT inherits current visibility",
            "Returning to tab root restores bar",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 11),
        title = "ViewModel preservation in Nav3 entries",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with rememberViewModelStoreNavEntryDecorator",
            "RecipeViewModel receives NavKey arg",
        ),
        steps = listOf(
            LabStep(1, "Open Tab Gamma",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate to TabGammaDetail(\"test\")",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify RecipeViewModel.result == \"test\"",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Rotate device or trigger config change",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(5, "Verify ViewModel preserved (result still \"test\")",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "rememberViewModelStoreNavEntryDecorator preserves ViewModel",
            "ViewModel survives configuration changes",
            "ViewModel receives NavKey arg via factory",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 12),
        title = "Result consumption with LaunchedEffect",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with ResultStore + LaunchedEffect consumption pattern",
            "TabAlphaDetail observes result; TabAlphaEdit sets result",
        ),
        steps = listOf(
            LabStep(1, "Open Tab Alpha, navigate to TabAlphaDetail",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(2, "Navigate to TabAlphaEdit",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Enter text, tap Done",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify TabAlphaDetail shows result via LaunchedEffect",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Result cleared from store after consumption",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "ResultStore.setResult delivers to consumer",
            "LaunchedEffect consumes and clears result",
            "Consumed result survives recomposition via remember",
        ),
    ),
)

val R_DEEP_LINK_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 13),
        title = "Deep link bridging to Nav3",
        topology = TopologyId.T3,
        preconditions = listOf(
            "RecipeDeepLinksActivity trampoline parses custom URI scheme",
            "RecipeDeepLinkHostActivity handles deep link intent in Nav3",
        ),
        steps = listOf(
            LabStep(1, "Launch RecipeDeepLinksActivity with URI recipes://target?param=hello",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify RecipeDeepLinkHostActivity launches",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Verify DeepLinkTarget screen shows param \"hello\"",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Press back returns to DeepLinkHome",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Trampoline activity parses URI and forwards via Intent extras",
            "Nav3 host handles deep link on creation",
            "Back stack is correct after deep link",
        ),
    ),
)

val R_TRANSITION_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 14),
        title = "Custom transition animations",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with global transitionSpec (horizontal slide)",
            "Per-entry metadata override on TransitionFade (fade transition)",
            "predictivePopTransitionSpec for back gesture animation",
        ),
        steps = listOf(
            LabStep(1, "NavDisplay shows TransitionHome",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Slide Transition' - observe horizontal slide animation",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Tap 'Go to Fade' - observe fade animation (per-entry override)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Press back - observe fade pop animation",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Press back again - observe horizontal slide pop animation",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Global transitionSpec applies horizontal slide by default",
            "Per-entry metadata overrides global transition with fade",
            "predictivePopTransitionSpec activates on back gesture",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 15),
        title = "Dialog destination via SceneStrategy",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with DialogSceneStrategy",
            "DialogRoute entry has DialogSceneStrategy.dialog() metadata",
        ),
        steps = listOf(
            LabStep(1, "NavDisplay shows TransitionHome",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Open Dialog' - dialog renders as overlay",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify content behind dialog is visible",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Tap 'Dismiss' - dialog removed from back stack",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify TransitionHome is restored",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "DialogSceneStrategy renders entry as dialog overlay",
            "Dialog dismissal removes entry from backstack",
            "Underlying content remains visible behind dialog",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 16),
        title = "Bottom sheet destination via SceneStrategy",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with custom BottomSheetSceneStrategy",
            "SheetRoute entry has BottomSheetSceneStrategy.bottomSheet() metadata",
            "Strategy chaining: bottomSheetStrategy then dialogStrategy",
        ),
        steps = listOf(
            LabStep(1, "NavDisplay shows TransitionHome",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Open Bottom Sheet' - ModalBottomSheet renders",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify ModalBottomSheet is displayed with content",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Swipe down or tap 'Close Sheet' to dismiss",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify back stack is correct after dismissal",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "BottomSheetSceneStrategy renders as ModalBottomSheet",
            "Swipe dismiss triggers onBack to remove from backstack",
            "Strategy chaining respects priority order (bottomSheet then dialog)",
        ),
    ),
)

val R_ADAPTIVE_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 17),
        title = "Adaptive list-detail layout",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with ListDetailSceneStrategy from material3-adaptive",
            "ItemList has listPane() metadata, ItemDetail has detailPane() metadata",
            "WindowSizeClass drives pane count",
        ),
        steps = listOf(
            LabStep(1, "Open list (narrow screen: full-screen list)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Select item (narrow: pushes detail as full screen)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Rotate to landscape or use tablet (both panes visible side-by-side)",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(4, "Select different item (detail updates in-place)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Tap 'Show Extra Pane' (expanded: third pane appears)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(6, "Press back (returns to list-detail or list only)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "WindowSizeClass drives pane count",
            "List + detail render side-by-side on wide screens",
            "Single pane on narrow screens",
            "Extra pane supported on expanded screens",
        ),
    ),
)

val R_CONDITIONAL_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 18),
        title = "Conditional navigation (auth gate)",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3 NavDisplay with ConditionalNavigator",
            "GateProfile has requiresLogin = true",
            "ConditionalNavigator redirects to GateLogin when not logged in",
        ),
        steps = listOf(
            LabStep(1, "Open GateHome screen",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Tap 'Go to Profile' (not logged in) -> redirected to login",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify login screen is shown with redirect target preserved",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Tap 'Log In' -> auto-navigated to profile",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify profile screen is shown (login entry removed from backstack)",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(6, "Tap 'Logout' -> back to home",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "requiresLogin flag triggers redirect to login",
            "Login preserves redirectToKey for original target",
            "Successful login navigates to original target",
            "Login entry removed from backstack after login",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 19),
        title = "Advanced deep links with synthetic backstack",
        topology = TopologyId.T3,
        preconditions = listOf(
            "RecipeAdvancedDeepLinksActivity trampoline parses recipes://advanced URI",
            "RecipeConditionalHostActivity handles advanced deep link intent",
            "Synthetic backstack: [AdvancedDeepHome, AdvancedDeepTarget]",
        ),
        steps = listOf(
            LabStep(1, "Launch via adb: recipes://advanced?name=Alice&location=NYC",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify target screen shows name=Alice, location=NYC",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(3, "Press back -> returns to AdvancedDeepHome (synthetic backstack)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Press back again -> exits activity",
                expectedEvents = listOf(TraceEventType.BACK_EVENT)),
        ),
        invariants = listOf(
            "Trampoline parses URI params and forwards via Intent extras",
            "Synthetic backstack includes home -> target",
            "Back navigation follows synthetic stack",
            "Up button behavior matches back behavior",
        ),
    ),
)

val R_MODAL_MATRIX_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.R, 20),
        title = "Nav2 modal reference (dialog/sheet/fullscreen)",
        topology = TopologyId.T2,
        preconditions = listOf(
            "RecipeModalMatrixHostActivity runs in Nav2 reference mode",
            "Dialog, sheet, and fullscreen routes are available in one Nav2 graph",
        ),
        steps = listOf(
            LabStep(1, "Open Nav2 dialog route, then dismiss",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(2, "Open Nav2 sheet route, then dismiss",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(3, "Open Nav2 fullscreen dialog route, then dismiss",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(4, "Verify parent route depth returns to baseline after each dismiss",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Each modal type is independently launchable in Nav2",
            "Dismiss pops only the modal route",
            "Parent stack remains stable across all modal permutations",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 21),
        title = "Nav3 modal reference (dialog-style + sheet-style)",
        topology = TopologyId.T3,
        preconditions = listOf(
            "RecipeModalMatrixHostActivity runs in Nav3 reference mode",
            "Dialog-style and sheet-style modal keys are configured",
        ),
        steps = listOf(
            LabStep(1, "Push Nav3 dialog modal key and dismiss",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(2, "Push Nav3 sheet modal key and dismiss",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(3, "Verify parent Nav3 key ordering is preserved",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Dialog and sheet overlays behave as top-most Nav3 entries",
            "Modal dismissal preserves parent key ordering",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 22),
        title = "Nav2 -> Nav3 modal interop reference",
        topology = TopologyId.T7,
        preconditions = listOf(
            "RecipeModalMatrixHostActivity runs in Nav2 parent / Nav3 leaf mode",
            "Parent Nav2 and child Nav3 modal operations are both available",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav2 parent into Nav3 leaf route and open child dialog modal",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Open parent Nav2 dialog while child modal is active, then dismiss parent",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(3, "Dismiss child modal and verify parent stack remains intact",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Parent Nav2 modal lifecycle does not corrupt child Nav3 leaf stack",
            "Child modal dismiss does not mutate Nav2 parent history",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 23),
        title = "Nav3 -> Nav2 modal interop reference",
        topology = TopologyId.T8,
        preconditions = listOf(
            "RecipeModalMatrixHostActivity runs in Nav3 parent / Nav2 leaf mode",
            "Parent Nav3 popup/dialog and child Nav2 modal routes are both available",
        ),
        steps = listOf(
            LabStep(1, "Navigate Nav3 parent into Nav2 leaf and open child sheet",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Open parent Nav3 popup above child modal, then dismiss popup",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(3, "Dismiss child modal and verify parent Nav3 stack is unchanged",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Parent Nav3 overlays do not mutate child Nav2 routes",
            "Child modal dismiss preserves Nav3 parent depth and key order",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 24),
        title = "Nav3 legacy island + popup/dialog reference",
        topology = TopologyId.T5,
        preconditions = listOf(
            "RecipeModalMatrixHostActivity runs in legacy island mode",
            "Fragment island, island dialog fragment, and parent popup/dialog overlays are enabled",
        ),
        steps = listOf(
            LabStep(1, "Enter legacy island and attach island fragment content",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(2, "Open island DialogFragment and parent popup overlay",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Dismiss popup then dialog fragment, verify island remains active",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.CONTAINER_CHANGE, TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Legacy island modal and parent popup can coexist safely",
            "Dismiss order is top-most overlay first",
            "Island fragment stack remains healthy after overlay chain",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.R, 25),
        title = "Restore/unwind reference for stacked modals across engines",
        topology = TopologyId.T8,
        preconditions = listOf(
            "RecipeModalMatrixHostActivity supports child-modal restore metadata",
            "Parent Nav3 and child Nav2 stacks are active before recreation",
        ),
        steps = listOf(
            LabStep(1, "Navigate into child Nav2 leaf and open child dialog modal",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Open parent Nav3 dialog above child modal and recreate host",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE, TraceEventType.LIFECYCLE)),
            LabStep(3, "Restore parent/child modal state and unwind in deterministic order",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify no duplicated parent keys or child routes after restore",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Restore keeps child modal and parent stack consistent",
            "Back unwind remains child-first/parent-second after recreation",
            "No duplicate modal entries appear during replay",
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
