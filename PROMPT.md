# Navigation Interop Lab -- Android Test Application

> **Note:** This file is the original build prompt/specification used to generate the project. For user-facing documentation, see [`README.md`](README.md).

## Objective

Build a multi-module Android test application inside this repository that validates navigation interoperability patterns: Nav2/Nav3 bridging, Fragment/Compose transitions, hybrid back-stack behavior, deeplink handling, and state restore across configuration changes and process death.

## Architecture Reference

Full blueprint: `navigation_interop_lab_architecture.md` (root of repo).
Requirements Q&A: `specs/navigation-interop-lab/requirements.md`.

## Key Requirements

- **minSdk 24**, latest stable Kotlin / AGP / Gradle.
- **Koin** for dependency injection.
- **Nav3**: `androidx.navigation3:*` 1.0.1 (stable) with Material 3 integration.
- **No sync mechanism** with production repo -- lab is self-contained.
- **GitHub Actions CI** smoke pipeline for instrumentation tests.
- **In-memory** `LabTraceStore` (no persistence).
- Invariant failures logged to **logcat** and shown in **inline trace panel**.
- Fake screens are **minimal** -- colored boxes with route labels.

## Module Layout

```
app/                  -- NavigationLabActivity, case browser entry
lab-contracts/        -- LabCaseId, LabScenario, LabResult, LabRoute, LabTraceEvent
lab-engine/           -- NavigationLabEngine, CaseBrowserScreen, orchestrator, invariants
lab-host-fragment/    -- Fragment host topologies and stub fragments
lab-host-nav2/        -- Nav2 host, Compose screens, Nav2 graphs
lab-host-nav3/        -- Nav3 host, NavDisplay integration
lab-deeplink/         -- DeeplinkSimulator, fake deeplink managers
lab-back/             -- BackOrchestrator, back-handling test infrastructure
lab-results/          -- Results display, inline trace panel
lab-testkit/          -- androidTest instrumentation tests (Espresso + Compose)
```

All modules depend on `:lab-contracts`. `:app` depends on all modules.

## Host Topologies (T1-T8)

| ID | Description |
|----|-------------|
| T1 | Activity(XML) -> FragmentContainerView -> Fragments |
| T2 | Activity(XML) -> ComposeView -> Nav2 NavHost |
| T3 | Activity(XML) -> ComposeView -> Nav3 NavDisplay |
| T4 | Activity(XML) -> ComposeView + overlay FrameLayout (dual containers) |
| T5 | Nav3 root -> LegacyIslandEntry -> AndroidViewBinding(FragmentContainerView) |
| T6 | Fragment host -> ComposeView -> internal Nav2 |
| T7 | Nav2 route -> Nav3 leaf screen |
| T8 | Nav3 key -> Nav2 leaf graph |

## Test Case Families (A-H)

Implement all cases from the architecture doc:
- **A** (A01-A07): Container and host ownership
- **B** (B01-B12): Nav2/Nav3 interoperability
- **C** (C01-C08): XML <-> Compose screen connection
- **D** (D01-D09): Dialog/bottom-sheet/overlay semantics
- **E** (E01-E08): Back handling and nested stacks
- **F** (F01-F08): Deeplink and fallback behavior
- **G** (G01-G07): State restore and argument stability
- **H** (H01-H05): Transaction safety and race conditions

## Run Modes

- **Manual**: step-by-step from case browser, inline trace panel visible.
- **Scripted**: auto-advance through steps with configurable delays.
- **Stress**: rapid repeated execution to detect race conditions.

## Acceptance Criteria

- Given the app launches, when the case browser opens, then all case families (A-H) are listed.
- Given a topology (T1-T8) is selected, when the host is created, then the correct container hierarchy is inflated.
- Given a case runs in manual mode, when each step completes, then trace events appear in the inline panel.
- Given a case runs in scripted mode, when started, then steps auto-advance with the configured delay.
- Given an invariant check fails, when the failure occurs, then it is shown in the trace panel AND logged to logcat.
- Given all critical-path cases (A*, B*, E*, F*, G*) exist, when `./gradlew :lab-testkit:connectedAndroidTest` runs, then instrumentation tests pass.
- Given the CI workflow exists, when a PR is opened, then GitHub Actions runs the smoke pipeline.

## Milestones

1. **M1**: Repo boots (`./gradlew :app:assembleDebug`), case browser opens, T1/T2/T3 topologies work.
2. **M2**: All A*, B*, C* cases implemented and manually runnable.
3. **M3**: D*, E*, F* cases implemented; trace logging and pass/fail invariants active.
4. **M4**: G*, H* cases automated in androidTest; GitHub Actions CI smoke pipeline added.
