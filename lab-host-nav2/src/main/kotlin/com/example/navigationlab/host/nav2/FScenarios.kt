package com.example.navigationlab.host.nav2

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** F-family deeplink scenarios on T2 (pure Compose + Nav2 host). */
val F_T2_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.F, 1),
        title = "Deeplink handled by first matching manager with real navigation",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2HostActivity is active at home route",
            "DeeplinkSimulator manager chain order is deterministic (SpaceManager then ProfileManager)",
        ),
        steps = listOf(
            LabStep(1, "Create Nav2HostActivity and confirm host is ready",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.LIFECYCLE)),
            LabStep(2, "Dispatch deeplink /space/project-alpha through simulator",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify destination route is space/project-alpha",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "First matching manager consumes deeplink without evaluating later managers",
            "Handled deeplink produces exactly one forward navigation transition",
            "Fallback route is not used when a concrete destination is produced",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.F, 2),
        title = "Handled=true with no navigation triggers suppression fallback",
        topology = TopologyId.T2,
        preconditions = listOf(
            "Nav2HostActivity is active at home route",
            "Primary manager can return handled-without-route for suppression bug simulation",
        ),
        steps = listOf(
            LabStep(1, "Create Nav2HostActivity and confirm home route",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Dispatch deeplink that returns handled=true with null route",
                expectedEvents = listOf(TraceEventType.DEEPLINK)),
            LabStep(3, "Verify simulator rewrites outcome to fallback menu/home route",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Handled-without-navigation is never accepted as a successful deeplink",
            "Suppression bug path resolves to explicit fallback route",
            "Trace includes fallback reason SUPPRESSED_NO_NAVIGATION for diagnostics",
        ),
    ),
)
