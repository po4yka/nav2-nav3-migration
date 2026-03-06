# Navigation Interop Lab - Historical Build Prompt

This file preserves the original bootstrap prompt and acceptance framing used to build the repository. It is archival, not the operational source of truth.

For current usage, start with:

- [MIGRATION_RESEARCH_GUIDE.md](MIGRATION_RESEARCH_GUIDE.md)
- [README.md](README.md)
- [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md)

## Original Objective

Build a multi-module Android test application in this repository to validate:

- `Nav2` <-> `Nav3` interoperability
- Fragment <-> Compose transitions
- hybrid back-stack behavior
- deeplink handling and fallback
- state restore across configuration changes and process death

## What This File Still Captures Well

- the original product intent for the lab
- the initial acceptance commands
- the high-level milestone structure

## Current Implementation Snapshot

As of current repository state:

- milestones `M1-M5` are implemented
- case families `A-H` (76 scenarios) are implemented
- recipe suite `R01-R25` (25 scenarios) is implemented
- CI smoke workflow exists at `.github/workflows/android-instrumentation-smoke.yml`

## Architecture Reference

- [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md) for the current blueprint and full case catalog
- [specs/navigation-interop-lab/requirements.md](specs/navigation-interop-lab/requirements.md) for the original requirements decisions

## Baseline Technical Requirements

- minSdk `24`, targetSdk and compileSdk `36`
- Java `17`
- Kotlin `2.3.10`
- AGP `9.1.0`
- Gradle wrapper `9.4.0`
- Nav3 `1.0.1` with Material 3 integration
- Koin for dependency injection
- in-memory trace store (`LabTraceStore`)
- invariant failures shown in the trace panel and logcat
- self-contained lab with no direct dependency on production repository modules

## Original Module Layout

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
lab-recipes/          -- Recipe scenarios R01-R25 and helpers
lab-testkit/          -- androidTest instrumentation tests
```

## Host Topologies

`T1-T8` are documented in [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md).

## Scenario Families

- `A` (`A01-A07`): container and host ownership
- `B` (`B01-B16`): Nav2/Nav3 interoperability
- `C` (`C01-C08`): XML <-> Compose connection
- `D` (`D01-D15`): dialog, sheet, and overlay semantics
- `E` (`E01-E09`): back handling and nested stacks
- `F` (`F01-F08`): deeplink and fallback behavior
- `G` (`G01-G08`): state restore and argument stability
- `H` (`H01-H05`): transaction safety and race conditions
- `R` (`R01-R25`): Nav3 recipes and migration patterns

## Original Run Modes

- manual: step-by-step, inline trace visible
- scripted: delayed auto-advance
- stress: rapid repeated execution

## Acceptance / Verification Commands

Original acceptance commands:

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-testkit:connectedAndroidTest
```

For the current recommended command set, use [README.md](README.md) and [MIGRATION_RESEARCH_GUIDE.md](MIGRATION_RESEARCH_GUIDE.md).

## Milestones

1. `M1`: repo boots, case browser, `T1/T2/T3`
2. `M2`: `A*`, `B*`, `C*` implemented
3. `M3`: `D*`, `E*`, `F*` plus trace and invariants
4. `M4`: `G*`, `H*` automation plus CI smoke
5. `M5`: `R01-R25` recipes, transitions, app-state helpers, observability
