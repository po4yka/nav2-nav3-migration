package com.example.navigationlab.host.nav2.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.host.nav2.R
import com.example.navigationlab.host.nav2.compose.Nav2StubScreen

/**
 * T2 host topology: Activity(XML) -> ComposeView -> Nav2 NavHost.
 * XML layout provides the activity shell; ComposeView hosts the Nav2 NavHost
 * with composable route destinations.
 */
class Nav2HostActivity : AppCompatActivity() {

    /** NavController exposed for scenario step executors. */
    lateinit var navController: NavHostController
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav2_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T2: Nav2 Host - $caseCode"

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
                    composable(ROUTE_SCREEN_B) {
                        Nav2StubScreen("Screen B", COLORS[2])
                    }
                    composable(ROUTE_SCREEN_C) {
                        Nav2StubScreen("Screen C", COLORS[3])
                    }
                }
            }
        }
    }

    /** Navigate to a route. Host topology modules use this to execute scenario steps. */
    fun navigateTo(route: String, singleTop: Boolean = false) {
        navController.navigate(route) {
            launchSingleTop = singleTop
        }
    }

    /** Navigate to a route, clearing the back stack to root. */
    fun navigateClearingTo(route: String) {
        navController.navigate(route) {
            popUpTo(ROUTE_HOME) { inclusive = false }
            launchSingleTop = true
        }
    }

    /** Pop the current destination off the back stack. */
    fun popBack(): Boolean = navController.popBackStack()

    /** Current back stack depth (approximated via back queue size). */
    val backStackDepth: Int
        get() = navController.currentBackStack.value.size

    companion object {
        private const val TAG = "T2Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        const val ROUTE_HOME = "home"
        const val ROUTE_SCREEN_A = "screen_a"
        const val ROUTE_SCREEN_B = "screen_b"
        const val ROUTE_SCREEN_C = "screen_c"

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
            Intent(context, Nav2HostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}
