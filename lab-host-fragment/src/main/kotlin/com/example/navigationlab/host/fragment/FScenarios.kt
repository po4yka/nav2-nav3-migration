package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** F-family deeplink scenarios on T6 (fragment host + Compose Nav2 leaf). */
val F_T6_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.F, 3),
        title = "Blocked deeplink routes to fallback without clearing request context",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Primary deeplink manager can return BLOCKED (feature gate/auth lock)",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity and confirm host ready",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.LIFECYCLE)),
            LabStep(2, "Dispatch blocked deeplink /space/private-room",
                expectedEvents = listOf(TraceEventType.DEEPLINK)),
            LabStep(3, "Apply fallback menu/home route while retaining blocked context metadata",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify trace exposes chainOutcome=BLOCKED and final outcome=FALLBACK",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Blocked deeplink does not silently disappear from trace history",
            "Fallback navigation occurs even when primary manager blocks destination",
            "Request context remains available for post-fallback diagnostics",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.F, 5),
        title = "Deeplink arrives before nav host ready",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity created but compose nav host not ready yet",
            "Deeplink request is buffered through simulator entrypoint",
        ),
        steps = listOf(
            LabStep(1, "Launch FragmentNav2HostActivity and dispatch deeplink before host-ready signal",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.DEEPLINK)),
            LabStep(2, "Simulator returns fallback with HOST_NOT_READY reason",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.INVARIANT)),
            LabStep(3, "Mark host ready and re-dispatch buffered deeplink",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify final destination is deterministic after readiness handoff",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Pre-ready deeplink is not treated as successful direct navigation",
            "Host readiness gate produces explicit fallback diagnostics",
            "Replay after readiness resolves to deterministic route without duplicate pushes",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.F, 6),
        title = "Deeplink during active screen-channel mode",
        topology = TopologyId.T6,
        preconditions = listOf(
            "FragmentNav2HostActivity loaded with ComposeNav2Fragment",
            "Screen-channel mode is active (sendToChannel=true style)",
        ),
        steps = listOf(
            LabStep(1, "Create FragmentNav2HostActivity and activate channel mode",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.LIFECYCLE)),
            LabStep(2, "Dispatch deeplink while sendToChannelActive=true",
                expectedEvents = listOf(TraceEventType.DEEPLINK)),
            LabStep(3, "Verify simulator routes to fallback with CHANNEL_ACTIVE reason",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE, TraceEventType.INVARIANT)),
            LabStep(4, "Disable channel mode and dispatch same deeplink again",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE)),
            LabStep(5, "Verify second dispatch is handled normally by manager chain",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Channel-active deeplink does not mutate primary nav stack directly",
            "Fallback reason CHANNEL_ACTIVE is emitted for observability",
            "Turning channel mode off restores normal deeplink handling deterministically",
        ),
    ),
)
