# Navigation Interop Lab

Concise contributor and agent summary for the repository.

## Read Order

1. [MIGRATION_RESEARCH_GUIDE.md](MIGRATION_RESEARCH_GUIDE.md)
2. [README.md](README.md)
3. [skills/nav2-nav3-refactor/references/migration-evidence.md](skills/nav2-nav3-refactor/references/migration-evidence.md)
4. [navigation_interop_lab_architecture.md](navigation_interop_lab_architecture.md)

## Build And Verify

Fast local checks:

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-recipes:assembleDebug
./gradlew :lab-testkit:assembleDebug :lab-testkit:assembleDebugAndroidTest
```

Full instrumentation smoke:

```bash
./gradlew :lab-testkit:connectedAndroidTest
```

Targeted migration classes:

- `com.example.navigationlab.testkit.RecipeMigrationTest`
- `com.example.navigationlab.testkit.CoreInteropBehaviorTest`
- `com.example.navigationlab.testkit.T7ModalInteropTest`
- `com.example.navigationlab.testkit.T8ModalInteropTest`
- `com.example.navigationlab.testkit.SystemBackParityModalTest`
- `com.example.navigationlab.testkit.ProcessDeathRestoreInteropTest`

## Module Structure

| Module | Description |
|---|---|
| `app` | Entry point: `NavigationLabActivity`, case browser |
| `lab-catalog` | Scenario registry and host-launch mapping |
| `lab-contracts` | Shared contracts and logging (`LabCaseId`, `LabScenario`, `NavLogger`) |
| `lab-engine` | Scenario orchestration, invariants, trace store |
| `lab-host-fragment` | Fragment host topologies and bridge scenarios |
| `lab-host-nav2` | Nav2 host topologies and Nav2 -> Nav3 interop |
| `lab-host-nav3` | Nav3 host topologies and Nav3 -> Nav2 interop |
| `lab-deeplink` | Deeplink simulator and fake managers |
| `lab-back` | Back orchestration utilities |
| `lab-results` | Trace and result panels |
| `lab-recipes` | Recipe scenarios `R01-R25` and Nav3 helper patterns |
| `lab-testkit` | Instrumentation tests |

## Conventions

- Kotlin `2.3.10`, AGP `9.1.0`, Gradle wrapper `9.4.0`
- minSdk `24`, compileSdk and targetSdk `36`, Java `17`
- Compose BOM `2026.02.01`, Nav2 `2.9.7`, Nav3 `1.0.1`
- Koin `4.1.1`
- Typed Nav3 keys use `@Serializable`
- `NavLogger` emits structured events under `TAG="NavRecipe"`
- The lab is isolated from production modules

## Scope Summary

- topologies: `T1-T8`
- interop case families: `A-H` (76 scenarios)
- recipe suite: `R01-R25` (25 scenarios)
- total scenarios: `101`

## High-Value Starting Points

- `R05` and `R06` for baseline Nav2 vs target Nav3 design
- `T7` and `T8` for temporary interop islands
- `D*` and `E*` for modal and back semantics
- `F*` for deeplink behavior
- `G*` for restore and process-death behavior

## When Adding Or Updating Recipes

1. Define key types in `lab-recipes/.../keys/`.
2. Add scenario metadata in `RecipeScenarios.kt`.
3. Register providers in `RecipeProviders.kt`.
4. Implement host activity and content under `lab-recipes/.../hosts` and `.../content`.
5. Add `NavLogger` calls around navigation edges.
6. Add or extend targeted instrumentation coverage.

## When Adding Or Updating Interop Cases

1. Update the case definition in `navigation_interop_lab_architecture.md`.
2. Implement the host behavior in the relevant host module.
3. Add or adjust invariant checks in `lab-engine`.
4. Extend `lab-testkit` when the scenario is automatable.
