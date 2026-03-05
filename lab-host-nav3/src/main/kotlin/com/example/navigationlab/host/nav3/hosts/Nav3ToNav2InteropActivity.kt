package com.example.navigationlab.host.nav3.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.host.nav3.Nav3Key
import com.example.navigationlab.host.nav3.R
import com.example.navigationlab.host.nav3.compose.Nav3StubScreen

/**
 * T8 host topology: Nav3 key -> Nav2 leaf graph.
 * Nav3 NavDisplay is the root navigator. One key (Nav2Leaf) renders
 * a Nav2 NavHost with its own independent nav graph.
 */
class Nav3ToNav2InteropActivity : AppCompatActivity() {

    /** Nav3 root back stack exposed for scenario step executors. */
    val backStack = mutableStateListOf<Any>(Nav3Key.Home)

    /** Nav2 leaf controller -- available when Nav2Leaf key is displayed. */
    var nav2LeafController: NavHostController? = null
        private set

    /** Tracked Nav2 leaf depth without restricted NavController APIs. */
    private var nav2LeafDepthValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav3_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T8: Nav3->Nav2 Interop - $caseCode [$runMode]"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        if (backStack.size > 1) {
                            val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                            backStack.removeLastOrNull()
                            NavLogger.back(TAG, from, backStack.size)
                        } else {
                            finish()
                        }
                    },
                    entryProvider = { key ->
                        when (key) {
                            is Nav3Key.Home -> NavEntry(key) {
                                Nav3StubScreen("Home", COLORS[0])
                            }
                            is Nav3Key.ScreenA -> NavEntry(key) {
                                Nav3StubScreen("Screen A", COLORS[1])
                            }
                            is Nav2LeafKey -> NavEntry(key) {
                                val controller = rememberNavController()
                                nav2LeafController = controller
                                nav2LeafDepthValue = 1
                                NavHost(
                                    navController = controller,
                                    startDestination = LEAF_ROUTE_HOME,
                                ) {
                                    composable(LEAF_ROUTE_HOME) {
                                        Nav2LeafStubScreen("Nav2 Leaf Home", COLORS[2])
                                    }
                                    composable(LEAF_ROUTE_DETAIL) {
                                        Nav2LeafStubScreen("Nav2 Leaf Detail", COLORS[3])
                                    }
                                }
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
        NavLogger.push(TAG, key::class.simpleName ?: "?", backStack.size)
    }

    /** Pop the Nav3 root back stack. */
    fun popNav3Back(): Boolean {
        if (backStack.size <= 1) return false
        val popped = backStack.lastOrNull()
        val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
        backStack.removeLastOrNull()
        if (popped is Nav2LeafKey) {
            nav2LeafController = null
            nav2LeafDepthValue = 0
        }
        NavLogger.pop(TAG, from, backStack.size)
        return true
    }

    /** Navigate within the Nav2 leaf graph. */
    fun navigateNav2Leaf(route: String) {
        val controller = nav2LeafController ?: return
        controller.navigate(route)
        nav2LeafDepthValue += 1
        NavLogger.push(TAG, route, nav2LeafDepthValue)
    }

    /** Pop the Nav2 leaf back stack. */
    fun popNav2LeafBack(): Boolean {
        val controller = nav2LeafController ?: return false
        val result = controller.popBackStack()
        if (result && nav2LeafDepthValue > 1) {
            nav2LeafDepthValue -= 1
        }
        return result
    }

    /** Nav3 root back stack depth. */
    val nav3BackStackDepth: Int
        get() = backStack.size

    /** Nav2 leaf back stack depth (0 if leaf not active). */
    val nav2LeafBackStackDepth: Int
        get() = nav2LeafDepthValue

    companion object {
        private const val TAG = "T8Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        const val LEAF_ROUTE_HOME = "leaf_home"
        const val LEAF_ROUTE_DETAIL = "leaf_detail"

        val COLORS = listOf(
            Color(0xFF6200EE), // Purple
            Color(0xFF03DAC5), // Teal
            Color(0xFFBB86FC), // Light purple
            Color(0xFF018786), // Dark teal
            Color(0xFFCF6679), // Pink
            Color(0xFF3700B3), // Deep purple
        )

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, Nav3ToNav2InteropActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

/** Navigation key for the Nav2 leaf entry within T8. */
data object Nav2LeafKey

/** Stub screen for Nav2 leaf destinations. */
@androidx.compose.runtime.Composable
private fun Nav2LeafStubScreen(label: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}
