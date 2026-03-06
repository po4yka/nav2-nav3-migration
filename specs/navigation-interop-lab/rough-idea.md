# Rough Idea

Initial high-level sketch for the Navigation Interop Lab.

This file is archival. It captures the original one-paragraph framing before the architecture, requirements, and implementation were expanded.

For current usage, start with:

- [MIGRATION_RESEARCH_GUIDE.md](../../MIGRATION_RESEARCH_GUIDE.md)
- [README.md](../../README.md)
- [navigation_interop_lab_architecture.md](../../navigation_interop_lab_architecture.md)

## Original Intent

Implement an Android testing application ("Navigation Interop Lab") to validate risky navigation combinations before touching production code.

## Current Status

The sketch has since been realized in this repository:

- topologies `T1-T8` implemented
- scenarios implemented: `A-H` (76) and `R01-R25` (25)
- CI smoke workflow present and running instrumentation tests
- primary operational documentation lives in [README.md](../../README.md)

## Key Goals

- reproduce real navigation patterns in an isolated lab
- test Nav2/Nav3 interoperability, XML/Compose bridging, back handling, deeplinks, and state restore
- support both manual exploration and automated verification
- expose structured trace logging with per-scenario invariant checks
- keep the build independent from production modules

## Scope Snapshot

- 8 host topologies (`T1-T8`)
- 76 interop test cases across `A-H`
- 25 recipe cases (`R01-R25`)
- 5 delivery milestones (`M1-M5`)
