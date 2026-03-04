package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/**
 * Scenario definitions for T1 topology (Activity -> FragmentContainerView -> Fragments).
 * Covers container/host ownership cases (A-family) that require pure fragment navigation.
 */
val T1_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.A, 1),
        title = "Single deterministic container exists before first navigation",
        topology = TopologyId.T1,
        preconditions = listOf("FragmentContainerView inflated in XML layout"),
        steps = listOf(
            LabStep(1, "Inflate activity layout with FragmentContainerView",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Verify container is present and visible",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(3, "Add first fragment to container",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "FragmentContainerView exists before first fragment transaction",
            "Exactly one container is active",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.A, 4),
        title = "Popup fragment in overlay should not replace base fragment content",
        topology = TopologyId.T1,
        preconditions = listOf("Base fragment displayed in container"),
        steps = listOf(
            LabStep(1, "Display base fragment in container",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(2, "Add popup fragment above base",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(3, "Verify base fragment is still in backstack",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Remove popup and verify base is restored",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Base fragment remains in backstack during overlay",
            "Removing overlay restores base fragment",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.A, 5),
        title = "Overlay removal restores previous base content and back stack",
        topology = TopologyId.T1,
        preconditions = listOf("Multiple fragments in backstack"),
        steps = listOf(
            LabStep(1, "Navigate: Home -> Screen A -> Screen B",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.STACK_CHANGE)),
            LabStep(2, "Add overlay fragment",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(3, "Pop overlay via back",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(4, "Verify Screen B is top of stack",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Back stack depth is preserved after overlay removal",
            "Correct fragment is visible after overlay pop",
        ),
    ),
)
