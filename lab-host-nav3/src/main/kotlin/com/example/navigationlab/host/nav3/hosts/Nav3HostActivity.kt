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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.R
import com.example.navigationlab.host.nav3.compose.Nav3StubScreen

/**
 * T3 host topology: Activity(XML) -> ComposeView -> Nav3 NavDisplay.
 * XML layout provides the activity shell; ComposeView hosts the Nav3 NavDisplay
 * with typed key destinations.
 */
class Nav3HostActivity : AppCompatActivity() {

    /** Back stack exposed for scenario step executors. */
    val backStack = mutableStateListOf<Nav3Key>(Nav3Key.Home)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav3_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T3: Nav3 Host - $caseCode"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                        backStack.removeLastOrNull()
                        NavLogger.back(TAG, from, backStack.size)
                    },
                    entryProvider = { key ->
                        when (key) {
                            is Nav3Key.Home -> NavEntry(key) {
                                Nav3StubScreen("Home", COLORS[0])
                            }
                            is Nav3Key.ScreenA -> NavEntry(key) {
                                Nav3StubScreen("Screen A", COLORS[1])
                            }
                            is Nav3Key.ScreenB -> NavEntry(key) {
                                Nav3StubScreen("Screen B", COLORS[2])
                            }
                            is Nav3Key.ScreenC -> NavEntry(key) {
                                Nav3StubScreen("Screen C", COLORS[3])
                            }
                        }
                    },
                )
            }
        }
    }

    /** Navigate to a key. Host topology modules use this to execute scenario steps. */
    fun navigateTo(key: Nav3Key) {
        backStack.add(key)
        NavLogger.push(TAG, key::class.simpleName ?: "?", backStack.size)
    }

    /** Pop the current destination off the back stack. */
    fun popBack(): Boolean {
        if (backStack.size <= 1) return false
        val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
        backStack.removeLastOrNull()
        NavLogger.pop(TAG, from, backStack.size)
        return true
    }

    /** Current back stack depth. */
    val backStackDepth: Int
        get() = backStack.size

    companion object {
        private const val TAG = "T3Host"
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
            Intent(context, Nav3HostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}
