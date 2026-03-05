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
import com.example.navigationlab.host.fragment.fragments.ComposeNav3Fragment
import com.example.navigationlab.host.fragment.fragments.LabStubFragment
import com.example.navigationlab.host.fragment.fragments.Nav3ModalKey

/**
 * Host activity for B08 scenario: Fragment -> ComposeView -> Nav3 NavDisplay.
 * Uses T6 topology pattern (Fragment hosts ComposeView with nav framework)
 * but with Nav3 instead of Nav2.
 *
 * Hosts [ComposeNav3Fragment] in the main container. The fragment contains
 * a ComposeView with a Nav3 NavDisplay including a modal entry for result passing.
 */
class FragmentNav3HostActivity : AppCompatActivity() {

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
            getString(R.string.topology_t6_nav3),
            caseCode,
            runMode,
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainFragmentContainer, ComposeNav3Fragment.newInstance(), TAG_COMPOSE_FRAGMENT)
                .commit()
        }
    }

    /** The hosted ComposeNav3Fragment, if available. */
    private val composeFragment: ComposeNav3Fragment?
        get() = supportFragmentManager.findFragmentByTag(TAG_COMPOSE_FRAGMENT) as? ComposeNav3Fragment

    // --- Nav3 navigation (delegated to fragment's back stack) ---

    /** Navigate to a Nav3 key within the fragment. */
    fun navigateNav3(key: Any) {
        composeFragment?.navigateTo(key)
        NavLogger.push(TAG, key::class.simpleName ?: "?", nav3BackStackDepth)
    }

    /** Navigate to Screen A. */
    fun navigateToScreenA() {
        navigateNav3(Nav3ModalKey.ScreenA)
    }

    /** Open the Nav3 modal entry. */
    fun openModal() {
        navigateNav3(Nav3ModalKey.ResultModal)
    }

    /** Pop the fragment's Nav3 back stack. */
    fun popNav3Back(): Boolean {
        val from = composeFragment?.backStack?.lastOrNull()?.let { it::class.simpleName } ?: "?"
        val result = composeFragment?.popBack() ?: false
        if (result) NavLogger.pop(TAG, from, nav3BackStackDepth)
        return result
    }

    /** Nav3 back stack depth inside the fragment. */
    val nav3BackStackDepth: Int
        get() = composeFragment?.backStack?.size ?: 0

    /** Last result returned by the Nav3 modal entry. */
    val lastModalResult: String?
        get() = composeFragment?.lastModalResult

    /** Confirm active modal entry and return result in the hosted fragment Nav3 stack. */
    fun confirmModalResult(): Boolean {
        val fragment = composeFragment ?: return false
        return fragment.confirmModalAndReturnResult()
    }

    /** Whether Nav3 modal key is currently top-most in fragment Nav3 stack. */
    val isModalVisible: Boolean
        get() = composeFragment?.backStack?.lastOrNull() is Nav3ModalKey.ResultModal

    // --- Activity-level overlay for D05 ---

    /** Add a fragment to the activity-level overlay container. */
    fun showOverlayFragment(fragment: LabStubFragment) {
        val overlay = findViewById<FrameLayout>(R.id.activityOverlayContainer)
        overlay.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .add(R.id.activityOverlayContainer, fragment)
            .addToBackStack("activity_overlay")
            .commitAllowingStateLoss()
    }

    /** Whether the activity-level overlay is visible. */
    val isOverlayVisible: Boolean
        get() = findViewById<FrameLayout>(R.id.activityOverlayContainer).isVisible

    /** Activity-level overlay back stack depth. */
    val overlayBackStackDepth: Int
        get() = supportFragmentManager.backStackEntryCount

    /** Dismiss the activity-level overlay. */
    fun dismissOverlay() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            runCatching { supportFragmentManager.popBackStack() }
        }
        findViewById<FrameLayout>(R.id.activityOverlayContainer).visibility = View.GONE
    }

    companion object {
        private const val TAG = "B08Host"
        private const val TAG_COMPOSE_FRAGMENT = "compose_nav3_fragment"
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
            Intent(context, FragmentNav3HostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}
