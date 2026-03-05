---
description: Plan and execute Nav2 to Nav3 refactors with parity guarantees using this repository's migration lab patterns.
argument-hint: "[feature/module scope]"
---

# /nav2-nav3-refactor

Refactor the requested scope from Navigation 2 to Navigation 3 incrementally, using repository-proven interop and parity tests.

## Inputs

- Scope argument from `/nav2-nav3-refactor ...` (feature, host, or module).
- Existing docs and experiments in this repository.

If scope is omitted, infer the smallest safe scope from recent user context and state the assumption.

## Procedure

### 1) Build migration context

Read these first:

- `README.md`
- `navigation_interop_lab_architecture.md`
- `lab-recipes/src/main/kotlin/com/example/navigationlab/recipes/hosts/RecipeMigrationHostActivity.kt`
- `lab-recipes/src/main/kotlin/com/example/navigationlab/recipes/helpers/NavigationState.kt`
- `lab-recipes/src/main/kotlin/com/example/navigationlab/recipes/helpers/Navigator.kt`

### 2) Extract baseline behavior

From current Nav2 implementation, capture:

- Top-level navigation model.
- Leaf navigation model.
- Modal behavior (dialog/sheet/fullscreen).
- System back behavior.
- Deeplink and restore behavior.

Do not refactor until this baseline is explicit.

### 3) Refactor in phases

Implement in this order:

1. Introduce typed `@Serializable` Nav3 keys (`NavKey`) matching existing routes.
2. Introduce explicit navigation state + navigator abstraction.
3. Move rendering to `NavDisplay` while preserving UX semantics.
4. Keep temporary interop islands as needed:
   - Nav2 parent -> Nav3 leaf pattern from `Nav2ToNav3InteropActivity` (T7).
   - Nav3 parent -> Nav2 leaf pattern from `Nav3ToNav2InteropActivity` (T8).
5. Remove obsolete Nav2-only paths after parity tests pass.

### 4) Preserve parity invariants

Check these invariants during refactor:

- Child pop does not mutate parent stack.
- Parent modal lifecycle does not mutate child stack.
- System back parity matches explicit dismiss API.
- Recreate/process-death restore preserves top-layer expectations.
- Deep-link fallback/replay remains deterministic.

### 5) Verify with targeted test classes

Run:

```bash
./gradlew :app:assembleDebug
./gradlew lintDebug
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.RecipeMigrationTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.CoreInteropBehaviorTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.T7ModalInteropTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.T8ModalInteropTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.SystemBackParityModalTest
./gradlew :lab-testkit:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.navigationlab.testkit.ProcessDeathRestoreInteropTest
```

If no emulator/device is available, run:

```bash
./gradlew :lab-testkit:assembleDebug :lab-testkit:assembleDebugAndroidTest
```

Then report instrumentation coverage as pending.

## Output Requirements

Always provide:

1. A short migration plan for the requested scope.
2. Concrete file edits made.
3. Parity checks run and pass/fail status.
4. Remaining risks (if any) tied to specific scenario families (`B`, `E`, `F`, `G`, `H`).
