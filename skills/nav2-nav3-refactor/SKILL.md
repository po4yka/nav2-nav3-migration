---
name: nav2-nav3-refactor
description: Refactor Android code from Navigation 2 to Navigation 3 with behavior parity. Use when replacing NavHostController string-route flows with NavDisplay/NavKey state-driven flows, introducing temporary Nav2/Nav3 interop islands, or validating back/modal/deeplink/restore semantics against this repository's migration experiments (R05/R06, T7/T8, B/E/G tests).
---

# Nav2 to Nav3 Refactor

Read these before editing:

1. [../../MIGRATION_RESEARCH_GUIDE.md](../../MIGRATION_RESEARCH_GUIDE.md)
2. [references/migration-evidence.md](references/migration-evidence.md)

## Workflow

1. Capture the current Nav2 behavior.
2. Identify the migration question category: baseline replacement, temporary interop island, modal/back parity, deeplink parity, or restore parity.
3. Introduce typed Nav3 keys and explicit navigation state.
4. Keep interop boundaries while refactoring incrementally.
5. Prove parity with targeted instrumentation tests.
6. Remove temporary Nav2 paths only after parity passes.

## 1) Capture Baseline Nav2 Behavior

- Identify top-level routes, leaf routes, dialogs/sheets, deep links, and back behavior in the target module.
- Add test hooks that expose route/depth/modal state in the host activity when needed.
- Mirror the baseline pattern from `R05`:
  - `NavHost` + `navigation {}` sections for per-tab graphs.
  - Bottom bar switches tabs with `popUpTo` to enforce stack policy.
  - Dialog routes modeled as overlays.

Use as baseline anchors:
- `lab-recipes/.../RecipeMigrationHostActivity.kt` (`Nav2MigrationBegin`)
- `lab-recipes/.../RecipeScenarios.kt` (`R05` invariants)
- `lab-testkit/.../RecipeMigrationTest.kt`

## 2) Introduce Nav3 Model Without Breaking UX

- Convert routes to typed `@Serializable` keys implementing `NavKey`.
- Build `NavigationState` with:
  - `topLevelRoute`
  - per-top-level back stacks via `rememberNavBackStack`
  - decorated entries via `rememberDecoratedNavEntries`
- Build a `Navigator` abstraction that centralizes:
  - top-level tab switching
  - push navigation
  - back behavior at root vs non-root

Use as target anchors:
- `lab-recipes/.../RecipeMigrationHostActivity.kt` (`Nav3MigrationEnd`)
- `lab-recipes/.../helpers/NavigationState.kt`
- `lab-recipes/.../helpers/Navigator.kt`
- `lab-recipes/.../helpers/AppState.kt`

## 3) Keep Interop Islands During Incremental Rollout

- For Nav2 parent -> Nav3 leaf, copy T7 pattern:
  - Parent `NavHost` route hosts a child `NavDisplay`.
  - Child back pops child stack first, then parent.
- For Nav3 parent -> Nav2 leaf, copy T8 pattern:
  - Parent `NavDisplay` key hosts a child `NavHost`.
  - Parent and child modal layers stay isolated.
  - Save/restore pending child routes for recreate scenarios.

Use as interop anchors:
- `lab-host-nav2/.../Nav2ToNav3InteropActivity.kt` (T7)
- `lab-host-nav3/.../Nav3ToNav2InteropActivity.kt` (T8)

## 4) Preserve High-Risk Semantics

Validate explicitly after each migration phase:
- Child pop does not mutate parent stack.
- Parent modal open/close does not mutate child stack.
- System back parity matches API dismiss.
- Recreate/process-death restore keeps expected top-layer state.
- Deep-link fallback and replay behavior stays deterministic.

Prioritize cases from `B*`, `E*`, `G*`, and `R05/R06`.

When the migration question is specifically about:

- interop boundaries: prioritize `T7`, `T8`, and `CoreInteropBehaviorTest`
- modal ordering and system back parity: prioritize `D*`, `E*`, and `SystemBackParityModalTest`
- deeplink fallback: prioritize `F*` and `DeeplinkFamiliesBehaviorParityTest`
- restore and process death: prioritize `G*` and `ProcessDeathRestoreInteropTest`

## 5) Verification Gates

Run baseline build checks:

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-recipes:assembleDebug
```

Run targeted migration/interoperability tests:

```bash
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.RecipeMigrationTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.CoreInteropBehaviorTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.T7ModalInteropTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.T8ModalInteropTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.SystemBackParityModalTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.ProcessDeathRestoreInteropTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.DeeplinkFamiliesBehaviorParityTest
```

If emulator/device is unavailable, run compile-only fallback and report that instrumentation coverage is pending:

```bash
./gradlew :lab-testkit:assembleDebug :lab-testkit:assembleDebugAndroidTest
```

## 6) Completion Criteria

- Remove deprecated Nav2-only paths for the migrated scope.
- Keep typed keys and explicit navigator/state abstractions.
- Keep parity tests passing for migrated families.
- Keep `NavLogger` instrumentation around navigation edges until rollout confidence is high.
