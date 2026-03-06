# Nav2 to Nav3 Migration Evidence Map

Use this file as the authoritative mapping between migration decisions and validated repository experiments.

Read [../../../MIGRATION_RESEARCH_GUIDE.md](../../../MIGRATION_RESEARCH_GUIDE.md) first if you are new to the repository.

## Documentation Anchors

- `MIGRATION_RESEARCH_GUIDE.md`
  - Best starting point for research workflows, recommended experiments, and verification strategy.
- `README.md`
  - Defines scope, module map, topologies `T1-T8`, and verification baseline.
- `navigation_interop_lab_architecture.md`
  - Defines critical interop families `A-H`, recipe suite `R01-R25`, and minimum migration decision coverage (`A*`, `B*`, `E*`, `F*`, `G*`, plus one `H*` stress case).
- `PROMPT.md`
  - Captures historical requirements and acceptance commands.

## Question To Evidence Map

| Migration question | Start here | Code anchors | Tests |
|---|---|---|---|
| How does the current Nav2 baseline behave? | `R05` | `RecipeMigrationHostActivity.kt` (`Nav2MigrationBegin`) | `RecipeMigrationTest` |
| What should the target Nav3 state model look like? | `R06` | `RecipeMigrationHostActivity.kt` (`Nav3MigrationEnd`), `NavigationState.kt`, `Navigator.kt` | `RecipeMigrationTest` |
| How should I keep a Nav2 parent while migrating a leaf to Nav3? | `T7`, `B04`, `B13`, `B14`, `E09` | `Nav2ToNav3InteropActivity.kt` | `CoreInteropBehaviorTest`, `T7ModalInteropTest` |
| How should I keep a Nav3 parent while preserving a Nav2 leaf? | `T8`, `B03`, `B15`, `B16`, `G08` | `Nav3ToNav2InteropActivity.kt` | `CoreInteropBehaviorTest`, `T8ModalInteropTest`, `ProcessDeathRestoreInteropTest` |
| What should be treated as high-risk parity behavior? | `D*`, `E*`, `F*`, `G*` | Host-specific scenario files and helpers | modal, deeplink, and restore tests |

## Migration Baseline and Target (R05/R06)

- Baseline Nav2 (`R05`)
  - `lab-recipes/src/main/kotlin/com/example/navigationlab/recipes/hosts/RecipeMigrationHostActivity.kt`
  - Look at `Nav2MigrationBegin(...)`, `nav2FeatureASection`, `nav2FeatureBSection`, `nav2FeatureCSection`.
  - Key semantics:
    - Nav2 `NavHost` + nested `navigation {}` sections.
    - Bottom nav tab switches using `popUpTo`.
    - Dialog route as overlay.
- Target Nav3 (`R06`)
  - Same file, `Nav3MigrationEnd(...)`.
  - Key semantics:
    - `NavDisplay` driven by `NavigationState`.
    - `Navigator` encapsulates forward/back/tab-switch behavior.
    - Top-level stacks remain independent.

## Shared Helpers to Reuse

- `lab-recipes/src/main/kotlin/com/example/navigationlab/recipes/helpers/NavigationState.kt`
  - `rememberNavigationState(...)`
  - `toDecoratedEntries(...)` with saveable state decorators.
- `lab-recipes/src/main/kotlin/com/example/navigationlab/recipes/helpers/Navigator.kt`
  - `navigate(...)` and `goBack(...)` with top-level vs leaf behavior.
- `lab-recipes/src/main/kotlin/com/example/navigationlab/recipes/helpers/AppState.kt`
  - Tab history, back policy, and bottom-bar visibility strategies.

Use these helpers together:

- `NavigationState` for explicit per-stack state
- `Navigator` for push, pop, and top-level routing policy
- `AppState` when the migrated surface has multiple top-level destinations or bottom navigation

## Interop Boundary References

- Nav2 parent -> Nav3 leaf (`T7`)
  - `lab-host-nav2/src/main/kotlin/com/example/navigationlab/host/nav2/hosts/Nav2ToNav3InteropActivity.kt`
  - Useful for:
    - Child `NavDisplay` inside parent Nav2 route.
    - Parent/child modal isolation.
    - Deferred defaults and restore handling.
- Nav3 parent -> Nav2 leaf (`T8`)
  - `lab-host-nav3/src/main/kotlin/com/example/navigationlab/host/nav3/hosts/Nav3ToNav2InteropActivity.kt`
  - Useful for:
    - Child `NavHost` inside parent Nav3 key.
    - Parent/child stack independence.
    - Persist/restore parent tokens and pending child route.

## Parity Test Matrix

- `lab-testkit/src/androidTest/kotlin/com/example/navigationlab/testkit/RecipeMigrationTest.kt`
  - Asserts `R05` and `R06` flows are behaviorally consistent.
- `lab-testkit/src/androidTest/kotlin/com/example/navigationlab/testkit/CoreInteropBehaviorTest.kt`
  - Core `B*` interop scenarios (parent/child unwind correctness, singleTop/clear semantics).
- `lab-testkit/src/androidTest/kotlin/com/example/navigationlab/testkit/T7ModalInteropTest.kt`
  - `B13/B14/E09` modal and back-order semantics for Nav2->Nav3 interop.
- `lab-testkit/src/androidTest/kotlin/com/example/navigationlab/testkit/T8ModalInteropTest.kt`
  - `B15/B16/G08` modal isolation and recreate semantics for Nav3->Nav2 interop.
- `lab-testkit/src/androidTest/kotlin/com/example/navigationlab/testkit/SystemBackParityModalTest.kt`
  - System back parity vs API dismiss for Nav2/Nav3/interop modal paths.
- `lab-testkit/src/androidTest/kotlin/com/example/navigationlab/testkit/ProcessDeathRestoreInteropTest.kt`
  - Recreate + cold relaunch restore behavior for interop modal chains.
- `lab-testkit/src/androidTest/kotlin/com/example/navigationlab/testkit/DeeplinkFamiliesBehaviorParityTest.kt`
  - Deep-link manager/fallback behavior across hosts.

## Suggested Research Order

1. `RecipeMigrationTest`
2. `CoreInteropBehaviorTest`
3. `T7ModalInteropTest` or `T8ModalInteropTest`, depending on interop direction
4. `SystemBackParityModalTest`
5. `ProcessDeathRestoreInteropTest`
6. `DeeplinkFamiliesBehaviorParityTest`

## Minimum Command Set

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-recipes:assembleDebug
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.RecipeMigrationTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.CoreInteropBehaviorTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.SystemBackParityModalTest
```

Run additional targeted classes from the matrix when changing modal, restore, or deep-link behavior.

If no emulator or device is available:

```bash
./gradlew :lab-testkit:assembleDebug :lab-testkit:assembleDebugAndroidTest
```
