# Nav2 to Nav3 Migration Research Guide

This repository is an Android navigation lab for answering migration questions before changing production flows. It is best used as a decision aid, not as a drop-in migration framework.

## Start Here

Read the documents in this order:

1. [README.md](README.md) for repository scope, setup, and verification entrypoints.
2. [skills/nav2-nav3-refactor/references/migration-evidence.md](skills/nav2-nav3-refactor/references/migration-evidence.md) for the mapping from migration questions to experiments, code anchors, and tests.
3. [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md) for the full topology and scenario catalog.
4. Historical docs only when you need original planning context:
   - [PROMPT.md](PROMPT.md)
   - [specs/navigation-interop-lab/requirements.md](specs/navigation-interop-lab/requirements.md)
   - [specs/navigation-interop-lab/rough-idea.md](specs/navigation-interop-lab/rough-idea.md)

## Best First Experiments

If you only have time to inspect a few scenarios, start with these:

| Question | Scenarios | Primary code | Primary tests |
|---|---|---|---|
| What does the current Nav2 baseline look like? | `R05` | `lab-recipes/.../RecipeMigrationHostActivity.kt` (`Nav2MigrationBegin`) | `RecipeMigrationTest` |
| What does the target Nav3 shape look like? | `R06` | `lab-recipes/.../RecipeMigrationHostActivity.kt` (`Nav3MigrationEnd`) | `RecipeMigrationTest` |
| Can a Nav2 parent host a Nav3 leaf safely? | `T7`, `B04`, `B13`, `B14`, `E09` | `lab-host-nav2/.../Nav2ToNav3InteropActivity.kt` | `CoreInteropBehaviorTest`, `T7ModalInteropTest` |
| Can a Nav3 parent host a Nav2 leaf safely? | `T8`, `B03`, `B15`, `B16`, `G08` | `lab-host-nav3/.../Nav3ToNav2InteropActivity.kt` | `CoreInteropBehaviorTest`, `T8ModalInteropTest`, `ProcessDeathRestoreInteropTest` |
| How should modal and back semantics be validated? | `D*`, `E*` | Host-specific `DScenarios.kt` / `EScenarios.kt` | `SystemBackParityModalTest`, modal interop tests |
| How should deeplink fallback be validated? | `F*`, `R13` | `lab-deeplink/.../DeeplinkSimulator.kt` | `DeeplinkFamiliesBehaviorParityTest` |
| What must survive recreate and process death? | `G*`, `R02`, `R09-R12` | `NavigationState.kt`, `AppState.kt`, interop hosts | `ProcessDeathRestoreInteropTest`, `GStateRestoreSmokeTest` |

## How To Use The Lab

### Manual exploration

- Launch the app and use the case browser in `NavigationLabActivity`.
- Search by scenario code, title, or topology.
- Filter by family (`A-H`, `R`) or topology (`T1-T8`).
- Use the inline trace panel and result summary to inspect stack, container, and invariant behavior while the scenario runs.

### Scripted or stress runs

- `Scripted` mode is useful when you want deterministic replay of scenario steps.
- `Stress` mode is useful when you are probing transaction races, modal ordering, or back-stack corruption.

### Read the code after the scenario, not before

The fastest way to learn from the repo is:

1. Pick a migration question.
2. Find the matching scenario family or recipe.
3. Read the corresponding test to understand the asserted behavior.
4. Read the host activity and helper code that implements that behavior.

This order keeps the behavioral goal clear before you dive into implementation details.

## Recommended Research Workflows

### Workflow 1: Baseline Nav2 to target Nav3

Use this when migrating a top-level Nav2 flow to Nav3 while keeping UX parity.

1. Inspect `R05` for the current Nav2 shape:
   - nested `navigation {}` sections
   - top-level tab switching
   - dialog-as-overlay behavior
2. Inspect `R06` for the target Nav3 shape:
   - typed keys
   - `NavigationState`
   - `Navigator`
   - independent per-tab stacks
3. Run `RecipeMigrationTest`.
4. Use `NavigationState.kt`, `Navigator.kt`, and `AppState.kt` as the design references for new Nav3 state management.

### Workflow 2: Incremental interop rollout

Use this when only part of a flow is moving to Nav3.

1. For Nav2 parent -> Nav3 leaf, use `T7`.
2. For Nav3 parent -> Nav2 leaf, use `T8`.
3. Validate modal isolation, back priority, and restore behavior with the targeted interop tests before removing temporary bridges.

### Workflow 3: High-risk semantics

Use this when a migration is likely to break behavior that users notice immediately.

- Modals and overlays: `D*`
- Back handling and nested stacks: `E*`
- Deeplink/fallback behavior: `F*`
- Recreate/process death and restore: `G*`
- Race conditions and transaction timing: `H*`

## Verification Strategy

### Fast local verification

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-recipes:assembleDebug
./gradlew :lab-testkit:assembleDebug :lab-testkit:assembleDebugAndroidTest
```

Use this when no emulator or device is available.

### Targeted migration verification

```bash
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.RecipeMigrationTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.CoreInteropBehaviorTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.T7ModalInteropTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.T8ModalInteropTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.SystemBackParityModalTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.ProcessDeathRestoreInteropTest
```

### Full smoke verification

```bash
./gradlew :lab-testkit:connectedAndroidTest
```

This requires an emulator or device.

## What Conclusions You Can Safely Draw

This repo is well suited for:

- choosing between temporary interop strategies
- validating typed-key Nav3 patterns
- checking back, modal, deeplink, and restore semantics
- building parity test ideas for a production migration

This repo is not sufficient on its own for:

- proving production business-flow parity
- validating production analytics or side effects
- validating performance in the production app architecture
- replacing production navigation code wholesale without additional product-specific tests

## Repository Boundaries

- The lab is intentionally isolated from production modules.
- Some evidence in the architecture document references external production snapshots for motivation; those files are not part of this repository.
- Treat the external references as rationale for why the lab exists, not as files you can inspect locally.
