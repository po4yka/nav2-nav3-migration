# Decouple God Module Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Decouple `app` from concrete host modules by introducing a `LabHostProvider` interface in `lab-contracts` and a thin `lab-catalog-wiring` module that aggregates all concrete providers.

**Architecture:** Define a `LabHostProvider` interface in `lab-contracts` (scenarios + intent factory). Each host module's existing provider objects implement it. A new `lab-catalog-wiring` module is the single place that imports all concrete providers and feeds them to `LabScenarioCatalog`. `lab-catalog` becomes pure abstraction. `app` depends only on `lab-catalog-wiring` (which transitively brings everything), dropping 6 direct host module dependencies.

**Tech Stack:** Kotlin, Gradle (Android library modules), Koin DI

**Dependency graph before:**
```
app -> lab-catalog, lab-contracts, lab-engine, lab-host-fragment, lab-host-nav2, lab-host-nav3, lab-deeplink, lab-back, lab-results, lab-recipes
lab-catalog -> lab-contracts, lab-host-fragment, lab-host-nav2, lab-host-nav3, lab-recipes
```

**Dependency graph after:**
```
app -> lab-catalog-wiring, lab-contracts, lab-engine, lab-deeplink, lab-back, lab-results
lab-catalog-wiring -> lab-catalog, lab-host-fragment, lab-host-nav2, lab-host-nav3, lab-recipes
lab-catalog -> lab-contracts (ONLY)
```

---

### Task 1: Define `LabHostProvider` interface in `lab-contracts`

**Files:**
- Create: `lab-contracts/src/main/kotlin/com/example/navigationlab/contracts/LabHostProvider.kt`

**Step 1: Create the interface**

```kotlin
package com.example.navigationlab.contracts

import android.content.Context
import android.content.Intent

/**
 * Contract for host modules that provide lab scenarios and can launch
 * their host activity for a given case.
 */
interface LabHostProvider {
    /** All scenarios registered by this provider. */
    val scenarios: List<LabScenario>

    /** Create an Intent to launch the host activity for the given case. */
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent
}
```

**Step 2: Verify contracts module builds**

Run: `./gradlew :lab-contracts:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
feat(contracts): add LabHostProvider interface
```

---

### Task 2: Make all existing provider objects implement `LabHostProvider`

**Files:**
- Modify: `lab-host-fragment/src/main/kotlin/com/example/navigationlab/host/fragment/Stub.kt`
- Modify: `lab-host-nav2/src/main/kotlin/com/example/navigationlab/host/nav2/Stub.kt`
- Modify: `lab-host-nav3/src/main/kotlin/com/example/navigationlab/host/nav3/Stub.kt`
- Modify: `lab-recipes/src/main/kotlin/com/example/navigationlab/recipes/RecipeProviders.kt`

**Step 1: Update each provider object to implement the interface**

For every `object XxxProvider` in each file, change:
```kotlin
object FragmentHostProvider {
    val scenarios: List<LabScenario> = ...
    fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent = ...
}
```
to:
```kotlin
object FragmentHostProvider : LabHostProvider {
    override val scenarios: List<LabScenario> = ...
    override fun createHostIntent(context: Context, caseId: LabCaseId, runMode: String): Intent = ...
}
```

Add `import com.example.navigationlab.contracts.LabHostProvider` to each file.

Apply to all 23 provider objects across the 4 files:
- `Stub.kt` (fragment): `FragmentHostProvider`, `DualHostProvider`, `FragmentNav2HostProvider`, `FragmentNav3HostProvider`, `ComposeToXmlBridgeProvider`, `XmlToComposeBridgeProvider`
- `Stub.kt` (nav2): `Nav2HostProvider`, `Nav2ToNav3InteropProvider`
- `Stub.kt` (nav3): `Nav3HostProvider`, `Nav3ToNav2InteropProvider`, `Nav3FragmentIslandProvider`, `Nav3NestedChainProvider`, `XmlInComposeBridgeProvider`
- `RecipeProviders.kt`: `RecipeBasicProvider`, `RecipeInteropProvider`, `RecipeMigrationProvider`, `RecipeResultsProvider`, `RecipeAppStateProvider`, `RecipeDeepLinkProvider`, `RecipeTransitionProvider`, `RecipeAdaptiveProvider`, `RecipeConditionalProvider`, `RecipeModalMatrixProvider`

**Step 2: Verify all host modules build**

Run: `./gradlew :lab-host-fragment:assembleDebug :lab-host-nav2:assembleDebug :lab-host-nav3:assembleDebug :lab-recipes:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```
refactor: implement LabHostProvider in all provider objects
```

---

### Task 3: Refactor `LabScenarioCatalog` to accept `LabHostProvider` list

**Files:**
- Modify: `lab-catalog/src/main/kotlin/com/example/navigationlab/catalog/LabScenarioCatalog.kt`
- Modify: `lab-catalog/build.gradle.kts`

**Step 1: Rewrite `LabScenarioCatalog` to accept providers via constructor/init**

```kotlin
package com.example.navigationlab.catalog

import android.content.Context
import android.content.Intent
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabHostProvider
import com.example.navigationlab.contracts.LabScenario

typealias IntentFactory = (Context, LabCaseId, String) -> Intent

/** Shared source of truth for all registered scenario providers and host launchers. */
class LabScenarioCatalog(providers: List<LabHostProvider>) {

    val scenarios: List<LabScenario> = providers
        .flatMap { it.scenarios }
        .sortedWith(compareBy({ it.id.family.ordinal }, { it.id.number }))

    val launchByCaseCode: Map<String, IntentFactory> = buildMap {
        providers.forEach { provider ->
            provider.scenarios.forEach { scenario ->
                val previous = put(scenario.id.code, provider::createHostIntent)
                require(previous == null) {
                    "Duplicate host launch mapping for case ${scenario.id.code}"
                }
            }
        }
    }
}
```

Key changes:
- `object` -> `class` with `providers: List<LabHostProvider>` constructor parameter
- Remove all 23 concrete provider imports
- Remove `ProviderEntry` data class (replaced by `LabHostProvider` interface)
- Use `provider::createHostIntent` as the intent factory

**Step 2: Remove host module dependencies from `lab-catalog/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.catalog"
}

dependencies {
    implementation(project(":lab-contracts"))
}
```

Remove these 4 lines:
```
implementation(project(":lab-host-fragment"))
implementation(project(":lab-host-nav2"))
implementation(project(":lab-host-nav3"))
implementation(project(":lab-recipes"))
```

**Step 3: Verify catalog module builds in isolation**

Run: `./gradlew :lab-catalog:assembleDebug`
Expected: BUILD SUCCESSFUL (no more concrete provider imports)

**Step 4: Commit**

```
refactor(catalog): accept LabHostProvider list, drop host module deps
```

---

### Task 4: Create `lab-catalog-wiring` module

**Files:**
- Modify: `settings.gradle.kts` (add include)
- Create: `lab-catalog-wiring/build.gradle.kts`
- Create: `lab-catalog-wiring/src/main/AndroidManifest.xml`
- Create: `lab-catalog-wiring/src/main/kotlin/com/example/navigationlab/catalog/wiring/CatalogWiring.kt`

**Step 1: Add module to settings.gradle.kts**

Add after `include(":lab-catalog")`:
```kotlin
include(":lab-catalog-wiring")
```

**Step 2: Create `lab-catalog-wiring/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.catalog.wiring"
}

dependencies {
    implementation(project(":lab-catalog"))
    implementation(project(":lab-contracts"))
    implementation(project(":lab-host-fragment"))
    implementation(project(":lab-host-nav2"))
    implementation(project(":lab-host-nav3"))
    implementation(project(":lab-recipes"))
}
```

**Step 3: Create `lab-catalog-wiring/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

**Step 4: Create `CatalogWiring.kt`**

```kotlin
package com.example.navigationlab.catalog.wiring

import com.example.navigationlab.catalog.LabScenarioCatalog
import com.example.navigationlab.contracts.LabHostProvider
import com.example.navigationlab.host.fragment.ComposeToXmlBridgeProvider
import com.example.navigationlab.host.fragment.DualHostProvider
import com.example.navigationlab.host.fragment.FragmentHostProvider
import com.example.navigationlab.host.fragment.FragmentNav2HostProvider
import com.example.navigationlab.host.fragment.FragmentNav3HostProvider
import com.example.navigationlab.host.fragment.XmlToComposeBridgeProvider
import com.example.navigationlab.host.nav2.Nav2HostProvider
import com.example.navigationlab.host.nav2.Nav2ToNav3InteropProvider
import com.example.navigationlab.host.nav3.Nav3FragmentIslandProvider
import com.example.navigationlab.host.nav3.Nav3HostProvider
import com.example.navigationlab.host.nav3.Nav3NestedChainProvider
import com.example.navigationlab.host.nav3.Nav3ToNav2InteropProvider
import com.example.navigationlab.host.nav3.XmlInComposeBridgeProvider
import com.example.navigationlab.recipes.RecipeAdaptiveProvider
import com.example.navigationlab.recipes.RecipeAppStateProvider
import com.example.navigationlab.recipes.RecipeBasicProvider
import com.example.navigationlab.recipes.RecipeConditionalProvider
import com.example.navigationlab.recipes.RecipeDeepLinkProvider
import com.example.navigationlab.recipes.RecipeInteropProvider
import com.example.navigationlab.recipes.RecipeMigrationProvider
import com.example.navigationlab.recipes.RecipeModalMatrixProvider
import com.example.navigationlab.recipes.RecipeResultsProvider
import com.example.navigationlab.recipes.RecipeTransitionProvider

/** All host providers across every module. Single wiring point. */
val allHostProviders: List<LabHostProvider> = listOf(
    // Fragment topologies (T1, T4, T6)
    FragmentHostProvider,
    DualHostProvider,
    FragmentNav2HostProvider,
    FragmentNav3HostProvider,
    ComposeToXmlBridgeProvider,
    XmlToComposeBridgeProvider,
    // Nav2 topologies (T2, T7)
    Nav2HostProvider,
    Nav2ToNav3InteropProvider,
    // Nav3 topologies (T8, interop)
    Nav3HostProvider,
    Nav3ToNav2InteropProvider,
    Nav3FragmentIslandProvider,
    Nav3NestedChainProvider,
    XmlInComposeBridgeProvider,
    // Recipe scenarios (R01-R25)
    RecipeBasicProvider,
    RecipeInteropProvider,
    RecipeMigrationProvider,
    RecipeResultsProvider,
    RecipeAppStateProvider,
    RecipeDeepLinkProvider,
    RecipeTransitionProvider,
    RecipeAdaptiveProvider,
    RecipeConditionalProvider,
    RecipeModalMatrixProvider,
)

/** Pre-wired catalog with all host providers registered. */
fun createWiredCatalog(): LabScenarioCatalog = LabScenarioCatalog(allHostProviders)
```

**Step 5: Verify wiring module builds**

Run: `./gradlew :lab-catalog-wiring:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```
feat: add lab-catalog-wiring module as single concrete registration point
```

---

### Task 5: Update `app` to use wiring module and drop direct host deps

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/java/com/example/navigationlab/di/LabModule.kt`

**Step 1: Update `app/build.gradle.kts` dependencies**

Replace:
```kotlin
implementation(project(":lab-catalog"))
implementation(project(":lab-contracts"))
implementation(project(":lab-engine"))
implementation(project(":lab-host-fragment"))
implementation(project(":lab-host-nav2"))
implementation(project(":lab-host-nav3"))
implementation(project(":lab-deeplink"))
implementation(project(":lab-back"))
implementation(project(":lab-results"))
implementation(project(":lab-recipes"))
```

With:
```kotlin
implementation(project(":lab-catalog-wiring"))
implementation(project(":lab-catalog"))
implementation(project(":lab-contracts"))
implementation(project(":lab-engine"))
implementation(project(":lab-deeplink"))
implementation(project(":lab-back"))
implementation(project(":lab-results"))
```

Removes: `:lab-host-fragment`, `:lab-host-nav2`, `:lab-host-nav3`, `:lab-recipes`
Adds: `:lab-catalog-wiring` (which transitively provides all four)

**Step 2: Update `LabModule.kt` DI wiring**

```kotlin
package com.example.navigationlab.di

import com.example.navigationlab.catalog.wiring.createWiredCatalog
import com.example.navigationlab.engine.NavigationLabEngine
import com.example.navigationlab.launch.CaseHostLauncher
import org.koin.dsl.module

val labModule = module {
    single {
        val catalog = createWiredCatalog()
        NavigationLabEngine().apply {
            registerAll(catalog.scenarios)
        }
    }

    single {
        val catalog = createWiredCatalog()
        CaseHostLauncher(
            launchByCaseCode = catalog.launchByCaseCode,
        )
    }
}
```

Note: `LabScenarioCatalog` is now a class, not an object. We call `createWiredCatalog()` to get the instance. Alternatively, make it a singleton in Koin:

```kotlin
val labModule = module {
    single { createWiredCatalog() }

    single {
        NavigationLabEngine().apply {
            registerAll(get<com.example.navigationlab.catalog.LabScenarioCatalog>().scenarios)
        }
    }

    single {
        CaseHostLauncher(
            launchByCaseCode = get<com.example.navigationlab.catalog.LabScenarioCatalog>().launchByCaseCode,
        )
    }
}
```

Use the Koin singleton approach -- cleaner and avoids creating two catalog instances.

**Step 3: Build full project**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 4: Run all unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL, all tests pass

**Step 5: Commit**

```
refactor(app): use lab-catalog-wiring, drop direct host module deps
```

---

### Task 6: Update `lab-testkit` to use wiring module

**Files:**
- Modify: `lab-testkit/build.gradle.kts`

**Step 1: Update dependencies**

Replace:
```kotlin
implementation(project(":lab-catalog"))
implementation(project(":lab-contracts"))
implementation(project(":lab-deeplink"))
implementation(project(":lab-engine"))
implementation(project(":lab-host-fragment"))
implementation(project(":lab-host-nav2"))
implementation(project(":lab-host-nav3"))
implementation(project(":lab-recipes"))
```

With:
```kotlin
implementation(project(":lab-catalog-wiring"))
implementation(project(":lab-catalog"))
implementation(project(":lab-contracts"))
implementation(project(":lab-deeplink"))
implementation(project(":lab-engine"))
```

The 4 host modules come transitively through `:lab-catalog-wiring`.

**Step 2: Verify testkit builds**

Run: `./gradlew :lab-testkit:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Check if testkit directly imports provider types**

Run: `rg "import com.example.navigationlab.host\.(fragment|nav2|nav3)|import com.example.navigationlab.recipes" lab-testkit/ --max-count 5`

If any direct imports exist, keep those specific module dependencies. Otherwise the transitive deps suffice.

**Step 4: Commit**

```
refactor(testkit): use lab-catalog-wiring, drop direct host module deps
```

---

### Task 7: Verify and clean up

**Step 1: Full build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 2: Full unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: all pass

**Step 3: Lint check**

Run: `./gradlew lintDebug`
Expected: no new issues

**Step 4: Verify dependency graph is correct**

Run: `./gradlew :app:dependencies --configuration debugRuntimeClasspath 2>&1 | grep "project :lab-host"`

Expected: `:lab-host-*` should appear only as transitive deps of `:lab-catalog-wiring`, NOT as direct deps of `:app`.

**Step 5: Final commit**

```
chore: verify clean module boundaries after god-module refactor
```

---

## Summary of Changes

| Module | Before | After |
|--------|--------|-------|
| `lab-contracts` | No provider interface | + `LabHostProvider` interface |
| `lab-catalog` | `object` with 23 hardcoded imports, deps on 4 host modules | `class` accepting `List<LabHostProvider>`, deps on `lab-contracts` only |
| `lab-catalog-wiring` (NEW) | N/A | Single wiring point: imports all 23 providers, deps on all host modules |
| `app` | 10 direct module deps | 7 deps (dropped 4 host modules, added wiring) |
| `lab-testkit` | 8 direct module deps | 5 deps (dropped 4 host modules, added wiring) |
| Host modules | `object` providers | Same objects, now implement `LabHostProvider` |

**Net result:** `app` and `lab-catalog` no longer know about concrete host implementations. Only `lab-catalog-wiring` does.
