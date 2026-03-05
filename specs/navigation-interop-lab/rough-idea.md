# Rough Idea

Implement an Android testing application ("Navigation Interop Lab") to validate risky navigation combinations before touching production code.

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
- 49 test cases across 8 families (A-H)
- 4 delivery milestones (M1-M4)
- No production dependencies -- fully independent build
