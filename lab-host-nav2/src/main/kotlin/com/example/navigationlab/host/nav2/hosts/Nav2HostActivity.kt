package com.example.navigationlab.host.nav2.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
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

    /** Tracked Nav2 depth without restricted NavController APIs. */
    private var nav2BackStackDepthValue: Int = 1

    /** Whether a non-saveable argument injection path was explicitly detected. */
    var nonSaveableArgumentDetected: Boolean = false
        private set

    /** Resolved argument value used after saveability checks. */
    var resolvedArgumentValue: String = DEFAULT_ARGUMENT_FALLBACK
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav2_host)
        nonSaveableArgumentDetected = savedInstanceState?.getBoolean(STATE_NON_SAVEABLE_ARG_DETECTED, false) ?: false
        resolvedArgumentValue = savedInstanceState?.getString(STATE_RESOLVED_ARGUMENT_VALUE)
            ?: DEFAULT_ARGUMENT_FALLBACK

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_t2),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView.setContent {
            MaterialTheme {
                val controller = rememberNavController()
                navController = controller
                nav2BackStackDepthValue = 1

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
                    dialog(ROUTE_RESULT_DIALOG) {
                        DialogStubContent(
                            onDismiss = { popBack() },
                        )
                    }
                    dialog(ROUTE_BOTTOM_SHEET) {
                        BottomSheetStubContent(
                            onDismiss = { popBack() },
                        )
                    }
                    dialog(
                        route = ROUTE_FULL_SCREEN_DIALOG,
                        dialogProperties = DialogProperties(
                            usePlatformDefaultWidth = false,
                            decorFitsSystemWindows = false,
                        ),
                    ) {
                        FullScreenDialogStubContent(
                            onDismiss = { popBack() },
                        )
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_NON_SAVEABLE_ARG_DETECTED, nonSaveableArgumentDetected)
        outState.putString(STATE_RESOLVED_ARGUMENT_VALUE, resolvedArgumentValue)
    }

    /** Navigate to a route. Host topology modules use this to execute scenario steps. */
    fun navigateTo(route: String, singleTop: Boolean = false) {
        val previousRoute = navController.currentBackStackEntry?.destination?.route
        navController.navigate(route) {
            launchSingleTop = singleTop
        }
        if (!(singleTop && previousRoute == route)) {
            nav2BackStackDepthValue += 1
        }
        NavLogger.push(TAG, route, nav2BackStackDepthValue)
    }

    /** Navigate to a route, clearing the back stack to root. */
    fun navigateClearingTo(route: String) {
        navController.navigate(route) {
            popUpTo(ROUTE_HOME) { inclusive = false }
            launchSingleTop = true
        }
        nav2BackStackDepthValue = if (route == ROUTE_HOME) 1 else 2
        NavLogger.push(TAG, route, nav2BackStackDepthValue)
    }

    /** Pop the current destination off the back stack. */
    fun popBack(): Boolean {
        val from = navController.currentBackStackEntry?.destination?.route ?: "?"
        val result = navController.popBackStack()
        if (result) {
            if (nav2BackStackDepthValue > 1) nav2BackStackDepthValue -= 1
            NavLogger.pop(TAG, from, nav2BackStackDepthValue)
        }
        return result
    }

    /** Current back stack depth (approximated via back queue size). */
    val backStackDepth: Int
        get() = nav2BackStackDepthValue

    /** Current route at top of the Nav2 graph. */
    val currentRoute: String?
        get() = navController.currentBackStackEntry?.destination?.route

    /** Open sheet-style modal route. */
    fun openBottomSheet() {
        navigateTo(ROUTE_BOTTOM_SHEET)
    }

    /** Open dialog-style modal route. */
    fun openDialog() {
        navigateTo(ROUTE_RESULT_DIALOG)
    }

    /** Open fullscreen modal route. */
    fun openFullScreenDialog() {
        navigateTo(ROUTE_FULL_SCREEN_DIALOG)
    }

    /** Dismiss current modal route if one is visible. */
    fun dismissModal(): Boolean {
        if (!isBottomSheetVisible && !isDialogVisible && !isFullScreenDialogVisible) return false
        return popBack()
    }

    /** Whether sheet-style modal route is currently visible. */
    val isBottomSheetVisible: Boolean
        get() = currentRoute == ROUTE_BOTTOM_SHEET

    /** Whether dialog-style modal route is currently visible. */
    val isDialogVisible: Boolean
        get() = currentRoute == ROUTE_RESULT_DIALOG

    /** Whether fullscreen modal route is currently visible. */
    val isFullScreenDialogVisible: Boolean
        get() = currentRoute == ROUTE_FULL_SCREEN_DIALOG

    /**
     * Inject synthetic argument payload and return whether it is saveable.
     * Non-saveable payloads are detected explicitly and resolved to [fallbackValue].
     */
    fun injectSyntheticArgument(payload: Any?, fallbackValue: String = DEFAULT_ARGUMENT_FALLBACK): Boolean {
        val saveable = payload == null || payload is java.io.Serializable || payload is android.os.Parcelable
        return if (saveable) {
            nonSaveableArgumentDetected = false
            resolvedArgumentValue = payload?.toString().orEmpty()
            true
        } else {
            nonSaveableArgumentDetected = true
            resolvedArgumentValue = fallbackValue
            false
        }
    }

    /** Inject lambda-like payload used by G05 to exercise explicit non-saveable fallback path. */
    fun injectNonSaveableArgumentSurrogate(fallbackValue: String = DEFAULT_ARGUMENT_FALLBACK): Boolean =
        injectSyntheticArgument(payload = ({ /* non-saveable */ }), fallbackValue = fallbackValue)

    companion object {
        private const val TAG = "T2Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"
        private const val STATE_NON_SAVEABLE_ARG_DETECTED = "state_non_saveable_arg_detected"
        private const val STATE_RESOLVED_ARGUMENT_VALUE = "state_resolved_argument_value"

        const val DEFAULT_ARGUMENT_FALLBACK = "fallback_arg"

        const val ROUTE_HOME = "home"
        const val ROUTE_SCREEN_A = "screen_a"
        const val ROUTE_SCREEN_B = "screen_b"
        const val ROUTE_SCREEN_C = "screen_c"
        const val ROUTE_RESULT_DIALOG = "result_dialog"
        const val ROUTE_BOTTOM_SHEET = "bottom_sheet"
        const val ROUTE_FULL_SCREEN_DIALOG = "full_screen_dialog"

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

@androidx.compose.runtime.Composable
private fun DialogStubContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Nav2 Dialog",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun BottomSheetStubContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nav2 Sheet",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Dismiss Sheet")
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun FullScreenDialogStubContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.94f), shape = RoundedCornerShape(20.dp))
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nav2 Fullscreen Dialog",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Transparent backdrop preserved",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss) {
                    Text("Close Dialog")
                }
            }
        }
    }
}
