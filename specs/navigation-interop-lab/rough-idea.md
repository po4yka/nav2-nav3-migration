# Rough Idea

Implement an Android testing application ("Navigation Interop Lab") to validate risky navigation combinations before touching production code.

## Current Status

This was the initial high-level sketch. Current project status:
- Implemented directly in this repository
- Topologies `T1-T8` implemented
- Scenarios implemented: `A-H` (76) and `R01-R25` (25)
- CI smoke workflow exists and runs instrumentation tests
- Primary operational documentation is in [README.md](../../README.md)

## Source

Based on the architecture blueprint in `navigation_interop_lab_architecture.md` (root of this repository).

## Key Goals

- Standalone Android project that reproduces real navigation patterns from production
- Test Nav2/Nav3 interoperability, XML/Compose bridging, back handling, deeplinks, and state restore
- Provide both manual (case browser UI) and automated (instrumentation tests) verification modes
- Structured trace logging with pass/fail invariant checks per scenario
- Modular architecture: separate modules for contracts, engine, host topologies, deeplink simulation, back handling, and results

## Scope

- 8 host topologies (T1-T8)
- 76 test cases across 8 families (A-H)
- 5 delivery milestones (M1-M5)
- No production dependencies -- fully independent build
