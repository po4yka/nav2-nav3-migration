# Navigation Interop Lab

Standalone Android test app for validating Nav2/Nav3 migration and interoperability in isolated scenarios.

## Build and Verify

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-testkit:connectedAndroidTest
```

## Module Structure

| Module | Description |
|--------|-------------|
| `app` | Entry point: `NavigationLabActivity`, case browser |
| `lab-contracts` | Shared contracts and logging (`LabCaseId`, `LabScenario`, `NavLogger`) |
| `lab-engine` | Scenario orchestration and invariant checks |
| `lab-host-fragment` | Fragment host topologies |
| `lab-host-nav2` | Nav2 host topologies |
| `lab-host-nav3` | Nav3 host topologies |
| `lab-deeplink` | Deeplink simulator and fake managers |
| `lab-back` | Back orchestration and related utilities |
| `lab-results` | Trace/results rendering |
| `lab-recipes` | Recipe scenarios `R01-R25` |
| `lab-testkit` | Instrumentation tests (Espresso + Compose) |

## Conventions

- Kotlin `2.3.10`, AGP `9.1.0`, Gradle wrapper `9.4.0`
- minSdk `24`, compileSdk/targetSdk `36`, Java `17`
- Compose BOM `2026.02.01`, Nav2 `2.9.7`, Nav3 `1.0.1`
- Koin `4.1.1`
- Type-safe Nav3 keys use `@Serializable`
- `NavLogger` emits structured events under `TAG="NavRecipe"`

## Scope Summary

- Topologies: `T1-T8`
- Interop case families: `A-H` (76 scenarios)
- Recipe suite: `R01-R25` (25 scenarios)
- Total scenarios: `101`

## Adding a Recipe

1. Define key(s) in `lab-recipes/.../keys/`
2. Add scenario metadata in `RecipeScenarios.kt`
3. Register providers in `RecipeProviders.kt`
4. Implement host activity/content under `lab-recipes/.../hosts` and `.../content`
5. Add `NavLogger` calls around navigation events

## Adding an Interop Case (A-H)

1. Add case definition to `navigation_interop_lab_architecture.md`
2. Implement in relevant host module (`lab-host-fragment`, `lab-host-nav2`, `lab-host-nav3`)
3. Add invariant checks in `lab-engine`
4. Add/extend instrumentation coverage in `lab-testkit` when automatable
