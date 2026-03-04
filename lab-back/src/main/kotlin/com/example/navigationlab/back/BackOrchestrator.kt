package com.example.navigationlab.back

/**
 * Deterministic back dispatcher for mixed navigation stacks.
 *
 * Priority order:
 * 1) Activity/host overlays
 * 2) Nested child stack (fragment child manager, leaf graph, etc.)
 * 3) Main nav stack (Nav2/Nav3)
 * 4) Root exit policy
 */
class BackOrchestrator(
    private val chain: BackChain,
) {
    private var rootExitDispatched: Boolean = false

    /** Resolve one back event according to [chain] priority. */
    fun onBackPressed(): BackOutcome {
        if (chain.overlay?.pop() == true) {
            return BackOutcome.Consumed(BackLayer.OVERLAY)
        }
        if (chain.childStack?.pop() == true) {
            return BackOutcome.Consumed(BackLayer.CHILD_STACK)
        }
        if (chain.navStack?.pop() == true) {
            return BackOutcome.Consumed(BackLayer.NAV_STACK)
        }

        if (chain.rootExitPolicy == RootExitPolicy.SINGLE_SHOT && rootExitDispatched) {
            return BackOutcome.Ignored
        }

        chain.onRootExit?.invoke()
        rootExitDispatched = true
        return BackOutcome.RootExit
    }

    /** Reset root-exit gate for scenarios that re-enter root state. */
    fun resetRootExitGate() {
        rootExitDispatched = false
    }

    /** True when root exit has already been fired under single-shot policy. */
    val isRootExitDispatched: Boolean
        get() = rootExitDispatched
}

/** Mutable providers for each back layer used by [BackOrchestrator]. */
data class BackChain(
    val overlay: BackPopper? = null,
    val childStack: BackPopper? = null,
    val navStack: BackPopper? = null,
    val onRootExit: (() -> Unit)? = null,
    val rootExitPolicy: RootExitPolicy = RootExitPolicy.SINGLE_SHOT,
)

/** Strategy for resolving root-level back behavior. */
enum class RootExitPolicy {
    /** Fire root exit only once until [BackOrchestrator.resetRootExitGate] is called. */
    SINGLE_SHOT,

    /** Fire root exit every time lower layers cannot consume back. */
    ALWAYS,
}

/** Priority layers in deterministic back resolution. */
enum class BackLayer {
    OVERLAY,
    CHILD_STACK,
    NAV_STACK,
}

/** Result of a single [BackOrchestrator.onBackPressed] dispatch. */
sealed interface BackOutcome {
    data class Consumed(val layer: BackLayer) : BackOutcome
    data object RootExit : BackOutcome
    data object Ignored : BackOutcome
}

/** Pop contract used by each back layer. */
fun interface BackPopper {
    fun pop(): Boolean
}
