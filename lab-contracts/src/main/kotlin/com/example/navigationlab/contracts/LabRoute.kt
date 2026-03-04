package com.example.navigationlab.contracts

/**
 * Represents a navigation destination in the lab.
 * Agnostic to navigation framework -- used as a common reference across Nav2/Nav3/Fragment hosts.
 */
data class LabRoute(
    /** Unique route identifier (e.g., "screen_a", "dialog_settings"). */
    val id: String,
    /** Human-readable label for the fake screen. */
    val label: String = id,
    /** Optional arguments passed to the destination. */
    val args: Map<String, String> = emptyMap(),
)
