# Navigation Interop Lab - Build Prompt (Historical)

This file preserves the original implementation prompt/spec used to bootstrap the project.
For day-to-day usage and current status, use [README.md](README.md).

## Objective

Build a multi-module Android test application in this repository to validate Nav2/Nav3 interoperability:
- Nav2 <-> Nav3 bridging
- Fragment <-> Compose transitions
- hybrid back-stack behavior
- deeplink handling/fallback
- state restore across configuration changes and process death

## Current Implementation Snapshot

As of current repository state:
- Milestones `M1-M5` are implemented
- Case families `A-H` (49 scenarios) are implemented
- Recipe suite `R01-R19` (19 scenarios) is implemented
- CI smoke workflow exists at `.github/workflows/android-instrumentation-smoke.yml`

## Architecture Reference

- Full blueprint: [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md)
- Requirements Q&A: [specs/navigation-interop-lab/requirements.md](specs/navigation-interop-lab/requirements.md)

## Baseline Technical Requirements

- minSdk `24`, targetSdk/compileSdk `36`
- Java `17`
- Kotlin `2.3.10`
- AGP `9.1.0`
- Gradle wrapper `9.4.0`
- Nav3 `1.0.1` (with Material 3 integration)
- Koin for dependency injection
- In-memory trace store (`LabTraceStore`)
- Invariant failures shown in trace panel and logged to logcat
- Self-contained lab (no direct dependency on production repository modules)

## Module Layout

```text
app/                  -- NavigationLabActivity, case browser entry
lab-contracts/        -- LabCaseId, LabScenario, LabResult, LabRoute, LabTraceEvent, NavLogger
lab-engine/           -- NavigationLabEngine, CaseBrowserScreen, orchestrator, invariants
lab-host-fragment/    -- Fragment host topologies and stub fragments
lab-host-nav2/        -- Nav2 host, Compose screens, Nav2 graphs
lab-host-nav3/        -- Nav3 host, NavDisplay integration
lab-deeplink/         -- DeeplinkSimulator, fake deeplink managers
lab-back/             -- BackOrchestrator, back-handling infrastructure
lab-results/          -- Results display, inline trace panel
lab-recipes/          -- Recipe scenarios R01-R19 and helpers
lab-testkit/          -- androidTest instrumentation tests
```

## Host Topologies

`T1-T8` as documented in the architecture document.

## Scenario Families

- `A` (A01-A07): container and host ownership
- `B` (B01-B12): Nav2/Nav3 interoperability
- `C` (C01-C08): XML <-> Compose connection
- `D` (D01-D09): dialog/sheet/overlay semantics
- `E` (E01-E08): back handling and nested stacks
- `F` (F01-F08): deeplink and fallback behavior
- `G` (G01-G07): state restore and argument stability
- `H` (H01-H05): transaction safety and race conditions
- `R` (R01-R19): Nav3 recipes and migration patterns

## Run Modes

- Manual: step-by-step, inline trace visible
- Scripted: delayed auto-advance
- Stress: rapid repeated execution

## Acceptance / Verification Commands

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-testkit:connectedAndroidTest
```

## Milestones

1. `M1`: repo boots, case browser, `T1/T2/T3`
2. `M2`: `A*`, `B*`, `C*` implemented
3. `M3`: `D*`, `E*`, `F*` + trace/invariants
4. `M4`: `G*`, `H*` automation + CI smoke
5. `M5`: `R01-R19` recipes, transitions, app-state helpers, observability
