package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * C-family scenario definition for C02: Compose route hosts XML via AndroidViewBinding.
 * Uses T5 topology (Nav3 root + legacy fragment island via AndroidView).
 * Tests that embedded XML view state is preserved across Nav3 back stack changes.
 */
val C_NAV3_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.C, 2),
        title = "Compose route hosts XML via AndroidViewBinding and keeps state",
        topology = TopologyId.T5,
        preconditions = listOf(
            "Nav3FragmentIslandActivity active with Nav3 NavDisplay",
            "LegacyIslandKey renders AndroidView(FragmentContainerView) inside Nav3 entry",
            "IslandStubFragment available for embedding in the container",
        ),
        steps = listOf(
            LabStep(1, "Create Nav3FragmentIslandActivity with Nav3 NavDisplay (Home key)",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Navigate Nav3 to LegacyIslandKey (activates AndroidView with FragmentContainerView)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(3, "Add IslandStubFragment to the legacy container with label='Island A'",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify fragment state: label is 'Island A', fragment is attached and visible",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(5, "Navigate Nav3 away from LegacyIslandKey to ScreenA (pure Nav3 hop)",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE)),
            LabStep(6, "Navigate Nav3 back to LegacyIslandKey (pop ScreenA)",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(7, "Verify AndroidView(FragmentContainerView) re-created; check island fragment state",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "AndroidView correctly renders FragmentContainerView inside Nav3 entry",
            "Fragment embedded via AndroidView survives initial render",
            "Nav3 back stack changes re-create AndroidView (factory called again)",
            "Fragment state may or may not persist across Nav3 key re-entry (document behavior)",
            "No crash when AndroidView is removed and re-added by Nav3 NavDisplay",
        ),
    ),
)
