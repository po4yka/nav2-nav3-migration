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
import androidx.compose.ui.graphics.Color
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.host.fragment.R
import com.example.navigationlab.host.fragment.fragments.ComposeNav2Fragment
import com.example.navigationlab.host.fragment.fragments.LabStubFragment

/**
 * T6 host topology: Fragment host -> ComposeView -> internal Nav2.
 * Hosts [ComposeNav2Fragment] in the main container. The fragment contains
 * a ComposeView with a Nav2 NavHost for internal navigation.
 *
 * Also provides an activity-level overlay container for B06 (fragment
 * transactions triggered from Nav2 context) and dialog result access for B07.
 */
class FragmentNav2HostActivity : AppCompatActivity() {

    private var restoreOverlayRequested: Boolean = false
    private var pendingOverlayFragment: LabStubFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_nav2_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_t6_nav2),
            caseCode,
            runMode,
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainFragmentContainer, ComposeNav2Fragment.newInstance(), TAG_COMPOSE_FRAGMENT)
                .commit()
        }

        restoreOverlayRequested = savedInstanceState?.getBoolean(STATE_OVERLAY_VISIBLE, false) == true
        if (restoreOverlayRequested) {
            findViewById<FrameLayout>(R.id.activityOverlayContainer).visibility = View.VISIBLE
        }
        syncOverlayVisibilityWithBackStack()
    }

    override fun onResume() {
        super.onResume()
        flushPendingOverlayIfNeeded()
        syncOverlayVisibilityWithBackStack()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_OVERLAY_VISIBLE, isOverlayVisible)
    }

    /** The hosted ComposeNav2Fragment, if available. */
    private val composeFragment: ComposeNav2Fragment?
        get() = supportFragmentManager.findFragmentByTag(TAG_COMPOSE_FRAGMENT) as? ComposeNav2Fragment

    // --- Nav2 navigation (delegated to fragment's NavHostController) ---

    /** Navigate to a route within the fragment's Nav2 graph. */
    fun navigateNav2(route: String) {
        val fragment = composeFragment ?: return
        fragment.navigateTo(route)
        NavLogger.push(TAG, route, fragment.navBackStackDepth)
    }

    /** Open D-family sheet-style route in the fragment Nav2 graph. */
    fun openBottomSheet() = navigateNav2(ComposeNav2Fragment.ROUTE_BOTTOM_SHEET)

    /** Open D-family dialog route in the fragment Nav2 graph. */
    fun openDialog() = navigateNav2(ComposeNav2Fragment.ROUTE_RESULT_DIALOG)

    /** Open D-family fullscreen dialog route in the fragment Nav2 graph. */
    fun openFullScreenDialog() = navigateNav2(ComposeNav2Fragment.ROUTE_FULL_SCREEN_DIALOG)

    /** Pop the fragment's Nav2 back stack. */
    fun popNav2Back(): Boolean {
        val fragment = composeFragment ?: return false
        val from = currentNav2Route ?: "?"
        val result = fragment.popBack()
        if (result) NavLogger.pop(TAG, from, fragment.navBackStackDepth)
        return result
    }

    /** Nav2 back stack depth inside the fragment. */
    val nav2BackStackDepth: Int
        get() = composeFragment?.navBackStackDepth ?: 0

    /** Current route at top of the fragment's Nav2 graph. */
    val currentNav2Route: String?
        get() = composeFragment?.navHostController?.currentBackStackEntry?.destination?.route

    /** Whether the sheet-style route is currently top-most. */
    val isBottomSheetVisible: Boolean
        get() = currentNav2Route == ComposeNav2Fragment.ROUTE_BOTTOM_SHEET

    /** Whether the dialog route is currently top-most. */
    val isDialogVisible: Boolean
        get() = currentNav2Route == ComposeNav2Fragment.ROUTE_RESULT_DIALOG

    /** Whether the fullscreen dialog route is currently top-most. */
    val isFullScreenDialogVisible: Boolean
        get() = currentNav2Route == ComposeNav2Fragment.ROUTE_FULL_SCREEN_DIALOG

    // --- Activity-level overlay (B06) ---

    /** Add a fragment to the activity-level overlay container. */
    fun showOverlayFragment(fragment: LabStubFragment) {
        val overlay = findViewById<FrameLayout>(R.id.activityOverlayContainer)
        overlay.visibility = View.VISIBLE
        if (supportFragmentManager.isStateSaved) {
            pendingOverlayFragment = fragment
            return
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.activityOverlayContainer, fragment)
            .addToBackStack("activity_overlay")
            .commit()
    }

    /** Whether the activity-level overlay is visible. */
    val isOverlayVisible: Boolean
        get() = findViewById<FrameLayout>(R.id.activityOverlayContainer).isVisible

    /** Activity-level overlay back stack depth. */
    val overlayBackStackDepth: Int
        get() = supportFragmentManager.backStackEntryCount

    /** Dismiss the activity-level overlay. */
    fun dismissOverlay() {
        restoreOverlayRequested = false
        if (supportFragmentManager.backStackEntryCount > 0) {
            runCatching { supportFragmentManager.popBackStack() }
        }
        findViewById<FrameLayout>(R.id.activityOverlayContainer).visibility = View.GONE
    }

    private fun syncOverlayVisibilityWithBackStack() {
        val overlay = findViewById<FrameLayout>(R.id.activityOverlayContainer)
        val shouldBeVisible = supportFragmentManager.backStackEntryCount > 0 || restoreOverlayRequested
        overlay.visibility = if (shouldBeVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }
        if (supportFragmentManager.backStackEntryCount > 0) {
            restoreOverlayRequested = false
        }
    }

    private fun flushPendingOverlayIfNeeded() {
        val fragment = pendingOverlayFragment ?: return
        if (supportFragmentManager.isStateSaved) return
        pendingOverlayFragment = null
        showOverlayFragment(fragment)
    }

    // --- Dialog result (B07) ---

    /** Last result returned by the Nav2 dialog inside the fragment. */
    val lastDialogResult: String?
        get() = composeFragment?.lastDialogResult

    /** Confirm dialog route and return result in the hosted fragment Nav2 graph. */
    fun confirmDialogResult(): Boolean {
        val fragment = composeFragment ?: return false
        return fragment.confirmDialogAndReturnResult()
    }

    companion object {
        private const val TAG = "T6Host"
        private const val TAG_COMPOSE_FRAGMENT = "compose_nav2_fragment"
        private const val STATE_OVERLAY_VISIBLE = "overlay_visible"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        val COLORS = listOf(
            Color(0xFF6200EE), // Purple
            Color(0xFF03DAC5), // Teal
            Color(0xFFBB86FC), // Light purple
            Color(0xFF018786), // Dark teal
            Color(0xFFCF6679), // Pink
            Color(0xFF3700B3), // Deep purple
        )

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, FragmentNav2HostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}
