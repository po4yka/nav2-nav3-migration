package com.example.navigationlab.contracts

/** Outcome of a deeplink being processed through the manager chain. */
enum class DeeplinkOutcome {
    /** Deeplink was handled and navigation occurred. */
    HANDLED,
    /** Deeplink was blocked (feature gate, auth, etc.) -- no navigation. */
    BLOCKED,
    /** No manager claimed the deeplink. */
    IGNORED,
    /** Primary handler failed; fallback handler took over. */
    FALLBACK,
}
