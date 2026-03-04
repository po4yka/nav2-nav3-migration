package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definition for B09: Nested chain stress test.
 * Nav3 -> Nav2 -> Fragment -> Nav2 dialog -> back unwind.
 * Uses T8 topology as base (Nav3 root -> nested engines).
 */
val B09_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.B, 9),
        title = "Nested chain stress: Nav3 -> Nav2 -> Fragment -> Nav2 dialog -> back unwind",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav3 NavDisplay active with Home and ChainEntry keys",
            "ChainEntry renders Nav2 NavHost with chain_root and fragment_layer routes",
            "fragment_layer hosts Fragment with ComposeView + Nav2 NavHost + dialog route",
        ),
        steps = listOf(
            LabStep(1, "Create Nav3NestedChainActivity with Nav3 NavDisplay root showing Home",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav3: Home -> ChainEntry (enters Nav2 chain layer)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Navigate Nav2 chain: chain_root -> fragment_layer (hosts fragment island)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Fragment loads with ComposeView + Nav2 NavHost showing frag_home",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(5, "Navigate fragment Nav2: frag_home -> frag_dialog (opens dialog)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(6, "Dialog confirms and returns result",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(7, "Pop fragment Nav2 (frag_home remains)",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(8, "Pop Nav2 chain (fragment_layer -> chain_root)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(9, "Pop Nav3 (ChainEntry -> Home)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(10, "Verify Nav3 back stack is [Home] only, all child stacks unwound",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Each back press unwinds the correct engine layer",
            "Back stack remains consistent across engine boundaries",
            "No stack corruption or skipped layers during unwind",
            "All four layers unwind in correct order: dialog -> fragment -> nav2 -> nav3",
            "Nav3 root back stack intact after full chain unwind",
            "Dialog result correctly received at fragment level",
        ),
    ),
)
