# Navigation Interop Lab

Standalone Android test app validating Nav2/Nav3 migration patterns in isolated, deterministic scenarios.

## Build

```bash
./gradlew :app:assembleDebug
./gradlew :lab-testkit:connectedAndroidTest
```

## Module Structure

| Module | Description |
|--------|-------------|
| `app` | Entry point: `NavigationLabActivity`, case browser |
| `lab-contracts` | Shared types (`LabCaseId`, `LabScenario`, `NavLogger`, etc.) |
| `lab-engine` | Orchestrator, case browser UI, invariant checks |
| `lab-host-fragment` | Fragment host topologies and stub fragments |
| `lab-host-nav2` | Nav2 host, Compose screens, Nav2 graphs |
| `lab-host-nav3` | Nav3 host, `NavDisplay` integration |
| `lab-deeplink` | Deeplink simulator and fake managers |
| `lab-back` | Back orchestrator and back-handling infrastructure |
| `lab-results` | Results display, inline trace panel |
| `lab-recipes` | 19 Nav3 recipe scenarios (R01-R19) |
| `lab-testkit` | Instrumentation tests (Espresso + Compose) |

All modules depend on `:lab-contracts`. `:app` depends on all modules.

## Conventions

- Kotlin 2.3.10, AGP 9.1.0, minSdk 24, compileSdk 36, Java 17
- Compose BOM 2026.02.01, Nav2 2.9.7, Nav3 1.0.1
- Koin 4.1.1 for DI
- `@Serializable` `NavKey` for type-safe Nav3 routing
- Fake screens are minimal colored boxes (see `RecipeStubScreens.kt`)
- `NavLogger` for structured logging (`TAG="NavRecipe"`)
- `DefaultTransitions` for consistent animation across recipes

## Adding a New Recipe

1. Define `@Serializable` `NavKey` data classes in `RecipeKeys.kt`
2. Add `LabScenario` entry in `RecipeScenarios.kt`
3. Register scenario list in `RecipeProviders.kt`
4. Implement host activity in `lab-recipes/hosts/`
5. Add `NavLogger` calls on navigation actions (push, pop, back, etc.)

## Adding a New Test Case (A-H)

1. Define scenario in `navigation_interop_lab_architecture.md` under the appropriate family
2. Implement in the appropriate host module (`lab-host-fragment`, `lab-host-nav2`, or `lab-host-nav3`)
3. Add invariant checks in `lab-engine/invariants/`
4. Add instrumentation test in `lab-testkit` if automatable
