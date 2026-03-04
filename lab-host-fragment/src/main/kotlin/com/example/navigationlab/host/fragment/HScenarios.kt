package com.example.navigationlab.host.fragment

import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.LabStep
import com.example.navigationlab.contracts.TopologyId
import com.example.navigationlab.contracts.TraceEventType

/** H-family transaction-safety scenario on T1 fragment host. */
val H_T1_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.H, 1),
        title = "State-loss commit during onStop/background transition",
        topology = TopologyId.T1,
        preconditions = listOf(
            "FragmentHostActivity is running with fragment transactions enabled",
            "Background transition boundary is simulated through lifecycle callbacks",
        ),
        steps = listOf(
            LabStep(1, "Push foreground fragment state and transition activity toward onStop",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(2, "Issue commitAllowingStateLoss-style fragment update during stop boundary",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.LIFECYCLE)),
            LabStep(3, "Return to foreground and inspect resulting fragment hierarchy",
                expectedEvents = listOf(TraceEventType.LIFECYCLE, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Verify no crash and deterministic stack snapshot is emitted",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Background-state transaction path does not crash host process",
            "Post-resume fragment hierarchy remains inspectable and deterministic",
            "Trace clearly marks lifecycle window where state-loss commit occurred",
        ),
    ),
)

/** H-family visibility/transaction race scenario on T4 dual-container host. */
val H_T4_SCENARIOS: List<LabScenario> = listOf(
    LabScenario(
        id = LabCaseId(CaseFamily.H, 5),
        title = "Container visibility update races with fragment transaction commit",
        topology = TopologyId.T4,
        preconditions = listOf(
            "DualHostActivity is active with compose base and overlay FrameLayout",
            "Overlay visibility can be toggled independently from fragment commit timing",
        ),
        steps = listOf(
            LabStep(1, "Set overlay visible and enqueue overlay fragment transaction",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(2, "Toggle overlay container visibility while transaction is pending",
                expectedEvents = listOf(TraceEventType.CONTAINER_CHANGE, TraceEventType.FRAGMENT_TRANSACTION)),
            LabStep(3, "Drain pending operations and snapshot final container state",
                expectedEvents = listOf(TraceEventType.FRAGMENT_TRANSACTION, TraceEventType.CONTAINER_CHANGE)),
            LabStep(4, "Assert final visibility/stack state is deterministic",
                expectedEvents = listOf(TraceEventType.INVARIANT)),
        ),
        invariants = listOf(
            "Visibility toggles do not orphan overlay fragments",
            "Pending transaction drain converges to stable overlay state",
            "Compose base container remains unaffected by overlay race path",
        ),
    ),
)
