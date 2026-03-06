# Navigation Interop Lab

Standalone Android test application for validating `Nav2` -> `Nav3` migration and interoperability before touching production flows.

The lab focuses on deterministic, reproducible scenarios for:

- container ownership
- cross-engine routing (`Nav2` <-> `Nav3`)
- XML <-> Compose bridges
- back-stack unwind correctness
- deeplink handling and fallback
- recreate and process-death stability

## Start Here

Use the documentation in this order:

1. [MIGRATION_RESEARCH_GUIDE.md](MIGRATION_RESEARCH_GUIDE.md) for the fastest path through the repository as a migration-research tool.
2. [README.md](README.md) for setup, commands, module layout, and repository boundaries.
3. [skills/nav2-nav3-refactor/references/migration-evidence.md](skills/nav2-nav3-refactor/references/migration-evidence.md) for the question -> scenario -> test mapping.
4. [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md) for the full topology and case catalog.
5. Historical context only when needed:
   - [PROMPT.md](PROMPT.md)
   - [specs/navigation-interop-lab/requirements.md](specs/navigation-interop-lab/requirements.md)
   - [specs/navigation-interop-lab/rough-idea.md](specs/navigation-interop-lab/rough-idea.md)
   - [CLAUDE.md](CLAUDE.md)

## Quick Start

Prerequisites:

- JDK `17`
- Android SDK `36`
- Android emulator or device for `connectedAndroidTest`

Fast local verification:

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-recipes:assembleDebug
./gradlew :lab-testkit:assembleDebug :lab-testkit:assembleDebugAndroidTest
```

Full instrumentation smoke suite:

```bash
./gradlew :lab-testkit:connectedAndroidTest
```

## How To Use This Repository For Migration Research

### Step 1: Pick the migration question

Common starting points:

- baseline Nav2 vs target Nav3: `R05` and `R06`
- Nav2 parent -> Nav3 leaf interop: `T7`, `B04`, `B13`, `B14`, `E09`
- Nav3 parent -> Nav2 leaf interop: `T8`, `B03`, `B15`, `B16`, `G08`
- modal and back behavior: `D*`, `E*`
- deeplink and fallback behavior: `F*`
- recreate and process-death behavior: `G*`

### Step 2: Use the app to explore the scenario

`NavigationLabActivity` exposes the case browser with:

- search by case code, title, or topology
- family and topology filters
- run modes: `Manual`, `Scripted`, `Stress`
- inline trace timeline
- result summary and rerun support

### Step 3: Read tests before implementation details

The fastest way to understand intended behavior is:

1. read the matching `androidTest`
2. read the host activity for the scenario
3. read the helper types used by that host

This keeps the behavioral contract clear while you inspect implementation choices.

## Documentation Map

| Document | Purpose |
|---|---|
| [MIGRATION_RESEARCH_GUIDE.md](MIGRATION_RESEARCH_GUIDE.md) | Primary onboarding guide for nav2 -> nav3 research |
| [README.md](README.md) | Operational overview, setup, commands, module map |
| [skills/nav2-nav3-refactor/references/migration-evidence.md](skills/nav2-nav3-refactor/references/migration-evidence.md) | Concrete migration-decision evidence map |
| [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md) | Architecture blueprint and full scenario catalog |
| [CLAUDE.md](CLAUDE.md) | Concise contributor and agent summary |
| [PROMPT.md](PROMPT.md) | Historical bootstrap prompt |
| [specs/navigation-interop-lab/requirements.md](specs/navigation-interop-lab/requirements.md) | Historical requirements decisions |
| [specs/navigation-interop-lab/rough-idea.md](specs/navigation-interop-lab/rough-idea.md) | Original scope sketch |

## Project Structure

| Module | Description |
|---|---|
| `app` | Entry point (`NavigationLabActivity`) and case browser |
| `lab-catalog` | Scenario registry and launch mapping |
| `lab-contracts` | Shared contracts (`LabCaseId`, `LabScenario`, `LabResult`, `LabTraceEvent`, `NavLogger`) |
| `lab-engine` | Orchestration, case browser state, invariant checks, trace store |
| `lab-host-fragment` | Fragment-hosted topologies and XML <-> Compose bridge scenarios |
| `lab-host-nav2` | Nav2 hosts and Nav2 -> Nav3 interop scenarios |
| `lab-host-nav3` | Nav3 hosts and Nav3 -> Nav2 interop scenarios |
| `lab-deeplink` | Deeplink simulator and fake deeplink managers |
| `lab-back` | Back orchestration infrastructure |
| `lab-results` | Trace and result rendering |
| `lab-recipes` | Recipe scenarios (`R01-R25`) and reusable Nav3 helpers |
| `lab-testkit` | `androidTest` instrumentation coverage (Espresso + Compose) |

All feature and test modules share `:lab-contracts`. `:app` depends on the scenario-host modules and renders the browser UI.

## Host Topologies

| ID | Description |
|---|---|
| `T1` | `Activity(XML)` -> `FragmentContainerView` -> Fragments |
| `T2` | `Activity(XML)` -> `ComposeView` -> Nav2 `NavHost` |
| `T3` | `Activity(XML)` -> `ComposeView` -> Nav3 `NavDisplay` |
| `T4` | `Activity(XML)` -> `ComposeView` + overlay `FrameLayout` |
| `T5` | `Nav3 root` -> `LegacyIslandEntry` -> `AndroidViewBinding(FragmentContainerView)` |
| `T6` | Fragment host -> `ComposeView` -> internal Nav2 |
| `T7` | Nav2 route -> Nav3 leaf screen |
| `T8` | Nav3 key -> Nav2 leaf graph |

## Scenario Coverage

### Interop families `A-H` (76 scenarios)

| Family | Cases | Focus |
|---|---|---|
| `A` | `A01-A07` | Container and host ownership |
| `B` | `B01-B16` | Nav2/Nav3 interoperability |
| `C` | `C01-C08` | XML <-> Compose connection |
| `D` | `D01-D15` | Dialog, sheet, and overlay semantics |
| `E` | `E01-E09` | Back handling and nested stacks |
| `F` | `F01-F08` | Deeplink and fallback behavior |
| `G` | `G01-G08` | State restore and argument stability |
| `H` | `H01-H05` | Transaction safety and race conditions |

### Recipe suite `R01-R25` (25 scenarios)

`lab-recipes` groups recipes into:

- basic Nav3 examples: `R01-R03`
- Android interop inside Nav3: `R04`
- migration baseline and target: `R05-R06`
- results and event passing: `R07-R08`
- app state and multi-stack behavior: `R09-R12`
- deep links: `R13`
- transitions: `R14-R16`
- adaptive layouts: `R17`
- conditional routing: `R18-R19`
- modal interop matrix: `R20-R25`

Total implemented scenarios: **101**.

## Migration-Oriented Starting Points

| Goal | Start with |
|---|---|
| Understand current Nav2 shape vs target Nav3 shape | `R05`, `R06`, `RecipeMigrationTest` |
| Validate temporary interop islands | `T7`, `T8`, `CoreInteropBehaviorTest`, modal interop tests |
| Validate modal/back parity | `D*`, `E*`, `SystemBackParityModalTest` |
| Validate deeplink chains | `F*`, `R13`, `DeeplinkFamiliesBehaviorParityTest` |
| Validate restore/process death | `G*`, `ProcessDeathRestoreInteropTest`, `GStateRestoreSmokeTest` |

## Verification

Baseline commands expected to pass:

- `./gradlew :app:assembleDebug`
- `./gradlew lintDebug`
- `./gradlew :lab-recipes:assembleDebug`
- `./gradlew :lab-testkit:assembleDebug :lab-testkit:assembleDebugAndroidTest`

Targeted instrumentation classes for migration work:

- `com.example.navigationlab.testkit.RecipeMigrationTest`
- `com.example.navigationlab.testkit.CoreInteropBehaviorTest`
- `com.example.navigationlab.testkit.T7ModalInteropTest`
- `com.example.navigationlab.testkit.T8ModalInteropTest`
- `com.example.navigationlab.testkit.SystemBackParityModalTest`
- `com.example.navigationlab.testkit.ProcessDeathRestoreInteropTest`
- `com.example.navigationlab.testkit.DeeplinkFamiliesBehaviorParityTest`

## NavLogger

`NavLogger` in `:lab-contracts` writes structured navigation traces to logcat with `TAG="NavRecipe"`.

Available methods:

- `push`
- `pop`
- `back`
- `tabSwitch`
- `deepLink`
- `redirect`
- `result`
- `visibility`

## Tech Stack

| Component | Version |
|---|---|
| Gradle Wrapper | `9.4.0` |
| AGP | `9.1.0` |
| Kotlin | `2.3.10` |
| Compose BOM | `2026.02.01` |
| Navigation 2 | `2.9.7` |
| Navigation 3 | `1.0.1` |
| Koin | `4.1.1` |
| minSdk | `24` |
| targetSdk / compileSdk | `36` |

## CI

GitHub Actions workflow: `.github/workflows/android-instrumentation-smoke.yml`

- `quality-checks` runs `lintDebug` plus Robolectric smoke tests
- `connected-android-test` runs `:lab-testkit:connectedAndroidTest` on an emulator matrix (`API 34`, `35`, `36`)

## Repository Boundaries

- The lab is intentionally independent from production modules.
- Some rationale in the architecture document references external production snapshots; those files are not part of this repository.
- Use the repo to learn and validate migration patterns, not to claim full product parity by itself.

## Milestones

| Milestone | Status | Output |
|---|---|---|
| `M1` | Done | Repo boots, case browser opens, `T1/T2/T3` topologies |
| `M2` | Done | `A*`, `B*`, `C*` scenarios runnable |
| `M3` | Done | `D*`, `E*`, `F*` scenarios plus trace and invariants |
| `M4` | Done | `G*`, `H*` automation and CI smoke pipeline |
| `M5` | Done | `R01-R25` recipe suite, helpers, and navigation observability |
