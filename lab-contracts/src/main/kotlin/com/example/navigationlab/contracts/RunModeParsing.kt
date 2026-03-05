package com.example.navigationlab.contracts

/**
 * Parse a raw run mode string from intents or persisted state.
 */
fun parseRunModeOrDefault(
    raw: String?,
    default: RunMode = RunMode.MANUAL,
): RunMode = raw
    ?.let { value -> RunMode.entries.firstOrNull { it.name.equals(value, ignoreCase = true) } }
    ?: default

