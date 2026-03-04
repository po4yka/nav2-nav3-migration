package com.example.navigationlab.host.fragment.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import com.example.navigationlab.contracts.LabCaseId
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_nav2_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T6: Fragment->Compose->Nav2 - $caseCode"

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainFragmentContainer, ComposeNav2Fragment.newInstance(), TAG_COMPOSE_FRAGMENT)
                .commit()
        }
    }

    /** The hosted ComposeNav2Fragment, if available. */
    private val composeFragment: ComposeNav2Fragment?
        get() = supportFragmentManager.findFragmentByTag(TAG_COMPOSE_FRAGMENT) as? ComposeNav2Fragment

    // --- Nav2 navigation (delegated to fragment's NavHostController) ---

    /** Navigate to a route within the fragment's Nav2 graph. */
    fun navigateNav2(route: String) {
        composeFragment?.navHostController?.navigate(route)
    }

    /** Pop the fragment's Nav2 back stack. */
    fun popNav2Back(): Boolean = composeFragment?.navHostController?.popBackStack() ?: false

    /** Nav2 back stack depth inside the fragment. */
    val nav2BackStackDepth: Int
        get() = composeFragment?.navHostController?.currentBackStack?.value?.size ?: 0

    // --- Activity-level overlay (B06) ---

    /** Add a fragment to the activity-level overlay container. */
    fun showOverlayFragment(fragment: LabStubFragment) {
        val overlay = findViewById<FrameLayout>(R.id.activityOverlayContainer)
        overlay.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .add(R.id.activityOverlayContainer, fragment)
            .addToBackStack("activity_overlay")
            .commit()
    }

    /** Whether the activity-level overlay is visible. */
    val isOverlayVisible: Boolean
        get() = findViewById<FrameLayout>(R.id.activityOverlayContainer).visibility == View.VISIBLE

    /** Activity-level overlay back stack depth. */
    val overlayBackStackDepth: Int
        get() = supportFragmentManager.backStackEntryCount

    /** Dismiss the activity-level overlay. */
    fun dismissOverlay() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
        findViewById<FrameLayout>(R.id.activityOverlayContainer).visibility = View.GONE
    }

    // --- Dialog result (B07) ---

    /** Last result returned by the Nav2 dialog inside the fragment. */
    val lastDialogResult: String?
        get() = composeFragment?.lastDialogResult

    companion object {
        private const val TAG = "T6Host"
        private const val TAG_COMPOSE_FRAGMENT = "compose_nav2_fragment"
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
