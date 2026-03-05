package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definitions for T5 topology (Nav3 root + legacy island fragment host).
 * Tests Nav3 hosting a fragment island via AndroidView(FragmentContainerView).
 */
val T5_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.B, 5),
        title = "Nav3 root + legacy island fragment host (Nav3 -> FragmentManager)",
        topology = TopologyId.T5,
        preconditions = listOf(
            "Nav3 NavDisplay active with typed keys and one LegacyIsland key",
        ),
        steps = listOf(
            LabStep(1, "Create Nav3 NavDisplay with Home, ScreenA, and LegacyIsland keys",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate from Home to ScreenA (pure Nav3 hop)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(3, "Navigate from ScreenA to LegacyIsland key (activates fragment container)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Add IslandStubFragment to the legacy island container",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(5, "Replace island fragment with a different one",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(6, "Pop island fragment back stack (restore previous fragment)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(7, "Pop Nav3 back stack (LegacyIsland -> ScreenA)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(8, "Verify Nav3 back stack is Home + ScreenA, island container removed",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "FragmentContainerView renders correctly inside Nav3 entry via AndroidView",
            "Fragment island back stack is independent from Nav3 back stack",
            "Popping from island returns to previous island fragment",
            "Popping Nav3 key removes the fragment container entirely",
            "Nav3 back stack intact after island interaction",
        ),
    ),
)
