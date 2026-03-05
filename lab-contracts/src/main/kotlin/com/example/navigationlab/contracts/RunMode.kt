package com.example.navigationlab.contracts

/** Execution mode for a lab scenario. */
enum class RunMode {
    /** Step-by-step from case browser, inline trace panel visible. */
    MANUAL,
    /** Auto-advance through steps with configurable delays. */
    SCRIPTED,
    /** Rapid repeated execution to detect race conditions. */
    STRESS
}
