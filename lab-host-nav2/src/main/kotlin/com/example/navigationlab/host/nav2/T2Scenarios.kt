package com.example.navigationlab.host.nav2

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definitions for T2 topology (Activity -> ComposeView -> Nav2 NavHost).
 * Covers Nav2/Nav3 interoperability cases (B-family) focused on pure Nav2 behavior.
 */
val T2_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.B, 1),
        title = "Pure Nav2 graph baseline (no fragments)",
        topology = TopologyId.T2,
        preconditions = listOf("Nav2 NavHost inflated with composable routes"),
        steps = listOf(
            LabStep(1, "Create NavHost with home, screen_a, screen_b routes",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate from home to screen_a",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Navigate from screen_a to screen_b",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(4, "Pop back to screen_a",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify back stack has home and screen_a",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Nav2 NavHost is active with composable destinations",
            "Back stack correctly reflects navigation history",
            "Pop restores previous destination",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.B, 11),
        title = "singleTop semantics parity for Nav2 routes",
        topology = TopologyId.T2,
        preconditions = listOf("Nav2 NavHost with composable routes active"),
        steps = listOf(
            LabStep(1, "Navigate to screen_a",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(2, "Navigate to screen_a again with launchSingleTop=true",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify only one instance of screen_a in back stack",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Navigate to screen_a without singleTop",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify two instances of screen_a in back stack",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "singleTop=true prevents duplicate entries",
            "singleTop=false allows duplicate entries",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.B, 12),
        title = "Clear-to-root parity across Nav2 graph",
        topology = TopologyId.T2,
        preconditions = listOf("Nav2 NavHost active with multiple destinations in stack"),
        steps = listOf(
            LabStep(1, "Navigate: home -> screen_a -> screen_b",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(2, "Navigate to home clearing back stack (popUpTo home, inclusive=false)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify only home remains in stack",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Back press should exit activity",
                expectedEvents = listOf(TraceEventType.BACK_EVENT)),
        ),
        invariants = listOf(
            "popUpTo(home) clears intermediate destinations",
            "Back from root exits the activity",
        ),
    ),
)
