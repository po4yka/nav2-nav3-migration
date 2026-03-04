package com.example.navigationlab.host.nav2.hosts

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
import com.example.navigationlab.host.nav2.R
import com.example.navigationlab.host.nav2.compose.Nav2StubScreen

/**
 * T7 host topology: Nav2 route -> Nav3 leaf screen.
 * Nav2 NavHost is the root navigator. One route ("nav3_leaf") renders
 * a Nav3 NavDisplay with its own independent back stack.
 */
class Nav2ToNav3InteropActivity : AppCompatActivity() {

    /** Nav2 controller exposed for scenario step executors. */
    lateinit var navController: NavHostController
        private set

    /** Nav3 leaf back stack -- active when nav3_leaf route is displayed. */
    val nav3LeafBackStack = mutableStateListOf<Nav3LeafKey>(Nav3LeafKey.LeafHome)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav2_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T7: Nav2->Nav3 Interop - $caseCode"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                val controller = rememberNavController()
                navController = controller

                NavHost(
                    navController = controller,
                    startDestination = ROUTE_HOME,
                ) {
                    composable(ROUTE_HOME) {
                        Nav2StubScreen("Home", COLORS[0])
                    }
                    composable(ROUTE_SCREEN_A) {
                        Nav2StubScreen("Screen A", COLORS[1])
                    }
                    composable(ROUTE_NAV3_LEAF) {
                        NavDisplay(
                            backStack = nav3LeafBackStack,
                            onBack = { nav3LeafBackStack.removeLastOrNull() },
                            entryProvider = { key ->
                                when (key) {
                                    is Nav3LeafKey.LeafHome -> NavEntry(key) {
                                        Nav3LeafStubScreen("Nav3 Leaf Home", COLORS[2])
                                    }
                                    is Nav3LeafKey.LeafDetail -> NavEntry(key) {
                                        Nav3LeafStubScreen("Nav3 Leaf Detail", COLORS[3])
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    /** Navigate to a Nav2 route. */
    fun navigateTo(route: String, singleTop: Boolean = false) {
        navController.navigate(route) {
            launchSingleTop = singleTop
        }
    }

    /** Pop the Nav2 back stack. */
    fun popNav2Back(): Boolean = navController.popBackStack()

    /** Navigate within the Nav3 leaf. */
    fun navigateNav3Leaf(key: Nav3LeafKey) {
        nav3LeafBackStack.add(key)
    }

    /** Pop the Nav3 leaf back stack. */
    fun popNav3LeafBack(): Boolean {
        if (nav3LeafBackStack.size <= 1) return false
        nav3LeafBackStack.removeLastOrNull()
        return true
    }

    /** Nav2 back stack depth. */
    val nav2BackStackDepth: Int
        get() = navController.currentBackStack.value.size

    /** Nav3 leaf back stack depth. */
    val nav3LeafBackStackDepth: Int
        get() = nav3LeafBackStack.size

    companion object {
        private const val TAG = "T7Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        const val ROUTE_HOME = "home"
        const val ROUTE_SCREEN_A = "screen_a"
        const val ROUTE_NAV3_LEAF = "nav3_leaf"

        val COLORS = listOf(
            Color(0xFF6200EE), // Purple
            Color(0xFF03DAC5), // Teal
            Color(0xFFBB86FC), // Light purple
            Color(0xFF018786), // Dark teal
            Color(0xFFCF6679), // Pink
            Color(0xFF3700B3), // Deep purple
        )

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, Nav2ToNav3InteropActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

/** Navigation keys for the Nav3 leaf sub-graph within T7. */
sealed interface Nav3LeafKey {
    data object LeafHome : Nav3LeafKey
    data object LeafDetail : Nav3LeafKey
}

/** Stub screen for Nav3 leaf destinations. */
@androidx.compose.runtime.Composable
private fun Nav3LeafStubScreen(label: String, color: Color) {
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
