package com.example.navigationlab.host.nav3.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.R
import com.example.navigationlab.host.nav3.compose.Nav3StubScreen

/**
 * T5 host topology: Nav3 root -> LegacyIslandEntry -> AndroidView(FragmentContainerView).
 * Nav3 NavDisplay is the root navigator. The [LegacyIslandKey] renders a
 * [FragmentContainerView] via [AndroidView], hosting fragments managed by [supportFragmentManager].
 *
 * Used for B05 (Nav3 root + legacy island fragment host).
 */
class Nav3FragmentIslandActivity : AppCompatActivity() {

    /** Nav3 root back stack exposed for scenario step executors. */
    val backStack = mutableStateListOf<Any>(Nav3Key.Home)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav3_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T5: Nav3->Fragment Island - $caseCode"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is Nav3Key.Home -> NavEntry(key) {
                                Nav3StubScreen("Home", COLORS[0])
                            }
                            is Nav3Key.ScreenA -> NavEntry(key) {
                                Nav3StubScreen("Screen A", COLORS[1])
                            }
                            is LegacyIslandKey -> NavEntry(key) {
                                AndroidView(
                                    factory = { ctx ->
                                        FragmentContainerView(ctx).apply {
                                            id = R.id.legacyFragmentContainer
                                        }
                                    },
                                )
                            }
                            else -> NavEntry(key) {
                                Nav3StubScreen("Unknown", COLORS[4])
                            }
                        }
                    },
                )
            }
        }
    }

    /** Navigate to a Nav3 key on the root back stack. */
    fun navigateTo(key: Any) {
        backStack.add(key)
    }

    /** Pop the Nav3 root back stack. */
    fun popNav3Back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeLastOrNull()
        return true
    }

    /** Add a fragment to the legacy island container. */
    fun showIslandFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.legacyFragmentContainer, fragment)
            .addToBackStack("island")
            .commit()
    }

    /** Pop the fragment island back stack. */
    fun popIslandFragmentBack(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return true
        }
        return false
    }

    /** Nav3 root back stack depth. */
    val nav3BackStackDepth: Int
        get() = backStack.size

    /** Fragment island back stack depth. */
    val islandBackStackDepth: Int
        get() = supportFragmentManager.backStackEntryCount

    companion object {
        private const val TAG = "T5Host"
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
            Intent(context, Nav3FragmentIslandActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

/** Navigation key for the legacy fragment island entry within T5. */
data object LegacyIslandKey
