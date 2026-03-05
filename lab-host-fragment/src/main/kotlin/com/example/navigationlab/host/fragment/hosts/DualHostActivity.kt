package com.example.navigationlab.host.fragment.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.host.fragment.R
import com.example.navigationlab.host.fragment.compose.DualStubScreen
import com.example.navigationlab.host.fragment.fragments.LabStubFragment

/**
 * T4 host topology: Activity(XML) -> ComposeView + overlay FrameLayout (dual containers).
 * Reproduces the production pattern where an Activity has both a ComposeView
 * for main navigation content and a FrameLayout overlay for popup fragments.
 *
 * Used for A02/A03 (dual-container), A06 (pre-inflation nav), A07 (config change overlay).
 */
class DualHostActivity : AppCompatActivity() {

    /** Current label shown in the Compose base container. */
    var baseLabel by mutableStateOf("Home")
        private set

    /** Current color index for the Compose base container. */
    var baseColorIndex by mutableIntStateOf(0)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dual_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        val deferInflation = intent.getBooleanExtra(EXTRA_DEFER_INFLATION, false)

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_t4),
            caseCode,
            runMode,
        )

        // Restore overlay visibility after config change (A07)
        if (savedInstanceState != null) {
            val wasOverlayVisible = savedInstanceState.getBoolean(STATE_OVERLAY_VISIBLE, false)
            overlayVisibleBeforeConfigChange = wasOverlayVisible
            if (wasOverlayVisible) {
                findViewById<FrameLayout>(R.id.overlayContainer).visibility = View.VISIBLE
            }
            baseLabel = savedInstanceState.getString(STATE_BASE_LABEL, "Home")
            baseColorIndex = savedInstanceState.getInt(STATE_BASE_COLOR_INDEX, 0)
        }

        if (!deferInflation) {
            val composeView = findViewById<ComposeView>(R.id.composeView)
            composeView.setContent {
                MaterialTheme {
                    DualStubScreen(label = baseLabel, color = COLORS[baseColorIndex])
                }
            }
            isHostInflated = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_OVERLAY_VISIBLE, isOverlayVisible)
        outState.putString(STATE_BASE_LABEL, baseLabel)
        outState.putInt(STATE_BASE_COLOR_INDEX, baseColorIndex)
    }

    /** Update the Compose base container content. */
    fun setBaseContent(label: String, colorIndex: Int) {
        baseLabel = label
        baseColorIndex = colorIndex.coerceIn(0, COLORS.lastIndex)
        NavLogger.push(TAG, label, 1)
    }

    /** Show the overlay FrameLayout and add a fragment to it. */
    fun showOverlayFragment(fragment: LabStubFragment) {
        val overlay = findViewById<FrameLayout>(R.id.overlayContainer)
        overlay.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .add(R.id.overlayContainer, fragment)
            .addToBackStack("overlay")
            .commit()
        NavLogger.push(TAG, "overlay", supportFragmentManager.backStackEntryCount + 1)
    }

    /** Make the overlay container visible (without adding content yet). */
    fun showOverlayContainer() {
        findViewById<FrameLayout>(R.id.overlayContainer).visibility = View.VISIBLE
    }

    /** Hide the overlay container (without removing fragments). */
    fun hideOverlayContainer() {
        findViewById<FrameLayout>(R.id.overlayContainer).visibility = View.GONE
    }

    /** Whether the overlay container is currently visible. */
    val isOverlayVisible: Boolean
        get() = findViewById<FrameLayout>(R.id.overlayContainer).isVisible

    /** Current overlay fragment backstack depth. */
    val overlayBackStackDepth: Int
        get() = supportFragmentManager.backStackEntryCount

    /** Remove overlay: pop fragment back stack and hide overlay container. */
    fun dismissOverlay() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
        hideOverlayContainer()
    }

    /** Whether the Compose base container is visible. */
    val isBaseVisible: Boolean
        get() = findViewById<ComposeView>(R.id.composeView).isVisible

    // --- A06: deferred host setup + navigation queue ---

    /** Pending navigation requests queued before host inflation. */
    private val pendingNavigationQueue = mutableListOf<Pair<String, Int>>()

    /** Whether the ComposeView host has been initialized. */
    var isHostInflated: Boolean = false
        private set

    /**
     * Queue a navigation request. If the host is already inflated, applies immediately.
     * Otherwise queues for later execution when [completeHostInflation] is called.
     */
    fun requestNavigation(label: String, colorIndex: Int) {
        if (isHostInflated) {
            setBaseContent(label, colorIndex)
        } else {
            pendingNavigationQueue.add(label to colorIndex)
        }
    }

    /**
     * Complete deferred host inflation: set ComposeView content and drain the
     * pending navigation queue. Call this from step executors to simulate late inflation.
     */
    fun completeHostInflation() {
        if (isHostInflated) return
        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                DualStubScreen(label = baseLabel, color = COLORS[baseColorIndex])
            }
        }
        isHostInflated = true
        // Drain queued navigation requests
        pendingNavigationQueue.forEach { (label, colorIndex) ->
            setBaseContent(label, colorIndex)
        }
        pendingNavigationQueue.clear()
    }

    /** Number of pending (unprocessed) navigation requests. */
    val pendingNavigationCount: Int
        get() = pendingNavigationQueue.size

    // --- A07: overlay state for config change verification ---

    /** Whether overlay was visible before the last config change (saved in onSaveInstanceState). */
    var overlayVisibleBeforeConfigChange: Boolean = false
        private set

    companion object {
        private const val TAG = "T4Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"
        /** Pass true to skip ComposeView setup in onCreate (A06 deferred inflation). */
        const val EXTRA_DEFER_INFLATION = "defer_inflation"
        private const val STATE_OVERLAY_VISIBLE = "overlay_visible"
        private const val STATE_BASE_LABEL = "base_label"
        private const val STATE_BASE_COLOR_INDEX = "base_color_index"

        /** Predefined colors for fake screens. */
        val COLORS = listOf(
            Color(0xFF6200EE), // Purple
            Color(0xFF03DAC5), // Teal
            Color(0xFFBB86FC), // Light purple
            Color(0xFF018786), // Dark teal
            Color(0xFFCF6679), // Pink
            Color(0xFF3700B3), // Deep purple
        )

        fun createIntent(
            context: Context,
            caseId: LabCaseId,
            runMode: String,
            deferInflation: Boolean = false,
        ): Intent =
            Intent(context, DualHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
                putExtra(EXTRA_DEFER_INFLATION, deferInflation)
            }
    }
}
