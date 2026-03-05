package com.example.navigationlab.host.nav3

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** F-family deeplink scenarios on T3 (pure Nav3 host). */
val F_T3_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.F, 4),
        title = "Unknown path fallback to menu navigator",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3HostActivity running with home key at stack root",
            "No registered manager claims /unknown/* paths",
        ),
        steps = listOf(
            LabStep(1, "Create Nav3HostActivity and confirm root key",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE)),
            LabStep(2, "Dispatch deeplink /unknown/feature-entry",
                expectedEvents = listOf(TraceEventType.DEEPLINK)),
            LabStep(3, "Simulator exhausts manager chain and returns fallback menu/home route",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE)),
            LabStep(4, "Verify trace chainOutcome=IGNORED and fallbackReason=UNKNOWN_PATH",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Unknown deeplink path is always recovered to deterministic menu fallback",
            "No partial/invalid destination keys are pushed to Nav3 stack",
            "Trace includes UNKNOWN_PATH for postmortem analysis",
        ),
    ),
    LabScenario(
        id = LabCaseId(CaseFamily.F, 8),
        title = "Deeplink source attribution consistency",
        topology = TopologyId.T3,
        preconditions = listOf(
            "Nav3HostActivity running with deterministic manager chain",
            "Simulator supports INTENT and INTERNAL source attribution",
        ),
        steps = listOf(
            LabStep(1, "Dispatch deeplink from INTENT source and navigate",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE)),
            LabStep(2, "Dispatch equivalent deeplink from INTERNAL source",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify destination parity while preserving source metadata",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Equivalent deeplink paths resolve to same destination regardless of source",
            "Trace source attribution remains stable (INTENT vs INTERNAL)",
            "Fallback behavior does not erase origin metadata",
        ),
    ),
)

/** F-family deeplink scenario on T8 (Nav3 root -> Nav2 leaf restore path). */
val F_T8_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.F, 7),
        title = "Deeplink after process death restore resumes deterministic route",
        topology = TopologyId.T8,
        preconditions = listOf(
            "Nav3ToNav2InteropActivity restores parent and leaf stacks after process recreation",
            "Restored deeplink metadata includes restoredAfterProcessDeath=true",
        ),
        steps = listOf(
            LabStep(1, "Simulate process death with pending deeplink target in saved state",
                expectedEvents = listOf(TraceEventType.LIFECYCLE)),
            LabStep(2, "Recreate activity and replay deeplink through simulator with restore flag",
                expectedEvents = listOf(TraceEventType.DEEPLINK, TraceEventType.CONTAINER_CHANGE, TraceEventType.STACK_CHANGE)),
            LabStep(3, "Verify recovered route is identical to pre-death target",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
            LabStep(4, "Verify no duplicate parent/child pushes after restore replay",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Restore replay resolves to deterministic destination across Nav3/Nav2 boundary",
            "restoredAfterProcessDeath metadata remains true through dispatch result",
            "Replayed deeplink does not duplicate stack entries after recreation",
        ),
    ),
)
