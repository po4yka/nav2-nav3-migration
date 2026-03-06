# Navigation Interop Lab

Standalone Android test application for validating Nav2 -> Nav3 migration and interop behavior before touching production flows.

The lab focuses on deterministic, reproducible scenarios for:
- container ownership
- cross-engine routing (Nav2 <-> Nav3)
- XML <-> Compose bridges
- back-stack unwind correctness
- deeplink handling and fallback
- restore/process-death stability

## Documentation Index

- [README.md](README.md) - project overview, setup, and runbook
- [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md) - architecture blueprint and full case catalog
- [PROMPT.md](PROMPT.md) - original implementation prompt/spec context
- [specs/navigation-interop-lab/requirements.md](specs/navigation-interop-lab/requirements.md) - requirements clarification Q&A
- [specs/navigation-interop-lab/rough-idea.md](specs/navigation-interop-lab/rough-idea.md) - initial scope sketch
- [CLAUDE.md](CLAUDE.md) - concise contributor/agent-oriented project summary

## Quick Start

Prerequisites:
- JDK 17
- Android SDK 36
- Android emulator/device for instrumentation tests

```bash
# Build debug app
./gradlew :app:assembleDebug

# Run lint across all modules
./gradlew lintDebug

# Run instrumentation smoke suite
./gradlew :lab-testkit:connectedAndroidTest
```

## Project Structure

| Module | Description |
|--------|-------------|
| `app` | Entry point (`NavigationLabActivity`) and case browser |
| `lab-contracts` | Shared contracts (`LabCaseId`, `LabScenario`, `LabResult`, `LabTraceEvent`, `NavLogger`) |
| `lab-engine` | Engine/orchestration, case browser UI, invariant checks |
| `lab-host-fragment` | Fragment-hosted topologies and stub fragments |
| `lab-host-nav2` | Nav2 host topologies and interop scenarios |
| `lab-host-nav3` | Nav3 host topologies and interop scenarios |
| `lab-deeplink` | Deeplink simulator and fake deeplink managers |
| `lab-back` | Back orchestration and back-handling test infrastructure |
| `lab-results` | Results/trace rendering components |
| `lab-recipes` | Nav3 recipe scenarios (`R01-R25`) and helpers |
| `lab-testkit` | `androidTest` instrumentation coverage (Espresso + Compose) |

All modules depend on `:lab-contracts`. `:app` depends on all feature/test modules.

## Host Topologies

| ID | Description |
|----|-------------|
| T1 | `Activity(XML)` -> `FragmentContainerView` -> Fragments |
| T2 | `Activity(XML)` -> `ComposeView` -> Nav2 `NavHost` |
| T3 | `Activity(XML)` -> `ComposeView` -> Nav3 `NavDisplay` |
| T4 | `Activity(XML)` -> `ComposeView` + overlay `FrameLayout` |
| T5 | `Nav3 root` -> `LegacyIslandEntry` -> `AndroidViewBinding(FragmentContainerView)` |
| T6 | Fragment host -> `ComposeView` -> internal Nav2 |
| T7 | Nav2 route -> Nav3 leaf screen |
| T8 | Nav3 key -> Nav2 leaf graph |

## Scenario Coverage

### A-H interop families (76 scenarios)

| Family | Cases | Focus |
|--------|-------|-------|
| A | A01-A07 | Container and host ownership |
| B | B01-B16 | Nav2/Nav3 interoperability |
| C | C01-C08 | XML <-> Compose connection |
| D | D01-D15 | Dialog/sheet/overlay semantics |
| E | E01-E09 | Back handling and nested stacks |
| F | F01-F08 | Deeplink and fallback behavior |
| G | G01-G08 | State restore and argument stability |
| H | H01-H05 | Transaction safety and race conditions |

### R recipes (25 scenarios)

`lab-recipes` contains `R01-R25`, grouped as:
- Basic (`R01-R03`)
- Interop (`R04`)
- Migration (`R05-R06`)
- Results (`R07-R08`)
- App state (`R09-R12`)
- Deep links (`R13`)
- Transitions (`R14-R16`)
- Adaptive (`R17`)
- Conditional (`R18-R19`)
- Modal interop matrix (`R20-R25`)

Total implemented scenarios: **101** (`76 + 25`).

## Verification Baseline

Current quality gates expected for this repository:
- `./gradlew lintDebug` -> success
- `./gradlew :app:assembleDebug` -> success
- `./gradlew :lab-recipes:assembleDebug` -> success
- `./gradlew :lab-testkit:connectedAndroidTest` -> smoke instrumentation suite

## NavLogger

`NavLogger` (`:lab-contracts`) writes structured navigation traces to logcat with `TAG="NavRecipe"`.

Available methods:
- `push`
- `pop`
- `back`
- `tabSwitch`
- `deepLink`
- `redirect`
- `result`
- `visibility`

## Run Modes

| Mode | Description |
|------|-------------|
| Manual | Step-by-step run from case browser with visible trace panel |
| Scripted | Auto-advance scenario steps with delay |
| Stress | Repeated rapid execution to expose race conditions |

## Tech Stack

| Component | Version |
|-----------|---------|
| Gradle Wrapper | 9.4.0 |
| AGP | 9.1.0 |
| Kotlin | 2.3.10 |
| Compose BOM | 2026.02.01 |
| Navigation 2 | 2.9.7 |
| Navigation 3 | 1.0.1 |
| Koin | 4.1.1 |
| minSdk | 24 |
| targetSdk / compileSdk | 36 |

## CI

GitHub Actions workflow: `.github/workflows/android-instrumentation-smoke.yml`
- triggers: push to `main`, pull requests
- runs: `:lab-testkit:connectedAndroidTest` on emulator matrix (no per-API `:app:assembleDebug`)

## Milestones

| Milestone | Status | Output |
|-----------|--------|--------|
| M1 | Done | Repo boots, case browser opens, `T1/T2/T3` topologies |
| M2 | Done | `A*`, `B*`, `C*` scenarios runnable |
| M3 | Done | `D*`, `E*`, `F*` scenarios + trace/invariants |
| M4 | Done | `G*`, `H*` automation + CI smoke pipeline |
| M5 | Done | `R01-R25` recipe suite, helpers, navigation observability |
