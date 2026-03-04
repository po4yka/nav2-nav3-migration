package com.example.navigationlab.host.fragment.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.fragment.R
import com.example.navigationlab.host.fragment.compose.DualStubScreen
import com.example.navigationlab.host.fragment.fragments.LabStubFragment

/**
 * T4 host topology: Activity(XML) -> ComposeView + overlay FrameLayout (dual containers).
 * Reproduces the production pattern where an Activity has both a ComposeView
 * for main navigation content and a FrameLayout overlay for popup fragments.
 *
 * Used for A02 (late container inflation fallback) and A03 (dual-container visibility race).
 */
class DualHostActivity : AppCompatActivity() {

    /** Current label shown in the Compose base container. */
    var baseLabel by mutableStateOf("Home")
        private set

    /** Current color index for the Compose base container. */
    var baseColorIndex by mutableStateOf(0)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dual_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T4: Dual Container - $caseCode"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                DualStubScreen(label = baseLabel, color = COLORS[baseColorIndex])
            }
        }
    }

    /** Update the Compose base container content. */
    fun setBaseContent(label: String, colorIndex: Int) {
        baseLabel = label
        baseColorIndex = colorIndex.coerceIn(0, COLORS.lastIndex)
    }

    /** Show the overlay FrameLayout and add a fragment to it. */
    fun showOverlayFragment(fragment: LabStubFragment) {
        val overlay = findViewById<FrameLayout>(R.id.overlayContainer)
        overlay.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .add(R.id.overlayContainer, fragment)
            .addToBackStack("overlay")
            .commit()
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
        get() = findViewById<FrameLayout>(R.id.overlayContainer).visibility == View.VISIBLE

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
        get() = findViewById<ComposeView>(R.id.composeView).visibility == View.VISIBLE

    companion object {
        private const val TAG = "T4Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        /** Predefined colors for fake screens. */
        val COLORS = listOf(
            Color(0xFF6200EE), // Purple
            Color(0xFF03DAC5), // Teal
            Color(0xFFBB86FC), // Light purple
            Color(0xFF018786), // Dark teal
            Color(0xFFCF6679), // Pink
            Color(0xFF3700B3), // Deep purple
        )

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, DualHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}
