package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** E-family scenario on T5 (Nav3 root with legacy fragment island wizard flow). */
val E_T5_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.E, 4),
        title = "Wizard-like childFragmentManager stack handling",
        topology = TopologyId.T5,
        preconditions = listOf(
            "Nav3FragmentIslandActivity hosts legacy island fragment route",
            "Legacy island fragment pushes wizard steps via childFragmentManager",
        ),
        steps = listOf(
            LabStep(1, "Open legacy island from Nav3 root and render wizard step 1",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.STACK_CHANGE)),
            LabStep(2, "Push wizard step 2 then step 3 in childFragmentManager",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Dispatch back: childFragmentManager pops step 3 -> step 2",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(4, "Dispatch back: childFragmentManager pops step 2 -> step 1",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(5, "Dispatch back at step 1: control returns to Nav3 parent route",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
        ),
        invariants = listOf(
            "Child fragment wizard stack unwinds fully before parent Nav3 stack pops",
            "Each back press pops exactly one wizard step while child stack is non-empty",
            "Parent Nav3 stack remains unchanged until child stack reaches root",
        ),
    ),
)

/** E-family scenario on T8 interop (back after deeplink fallback pop chain). */
val E_T8_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.E, 7),
        title = "Back after deep-link fallback pop chain",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav3ToNav2InteropActivity active with Nav3 root and Nav2 leaf key",
            "Synthetic deeplink flow has entered Nav2 leaf detail destination",
        ),
        steps = listOf(
            LabStep(1, "Apply deeplink chain: Nav3 Home -> ScreenA -> Nav2Leaf and Nav2 leaf_home -> leaf_detail",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Run fallback pop chain to recover target screen",
                expectedEvents = listOf(TraceEventType.STACK_CHANGE, TraceEventType.BACK_EVENT)),
            LabStep(3, "Dispatch back after fallback chain completion",
                expectedEvents = listOf(TraceEventType.BACK_EVENT, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify parent/child stacks are deterministic after fallback unwind",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Fallback unwind pops child Nav2 stack before parent Nav3 keys",
            "Post-fallback back press resumes normal deterministic unwinding",
            "No stale deeplink destinations remain in either stack",
        ),
    ),
)
