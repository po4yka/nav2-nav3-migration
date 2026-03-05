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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
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

    /** Tracked Nav2 depth without restricted NavController APIs. */
    private var nav2BackStackDepthValue: Int = 1

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
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_t7),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
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
                    dialog(ROUTE_RESULT_DIALOG) {
                        ParentDialogStub(
                            onDismiss = { this@Nav2ToNav3InteropActivity.popNav2Back() },
                        )
                    }
                    composable(ROUTE_NAV3_LEAF) {
                        NavDisplay(
                            backStack = nav3LeafBackStack,
                            onBack = {
                                if (nav3LeafBackStack.size > 1) {
                                    val from = nav3LeafBackStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                                    nav3LeafBackStack.removeLastOrNull()
                                    NavLogger.back(TAG, from, nav3LeafBackStack.size)
                                } else {
                                    this@Nav2ToNav3InteropActivity.popNav2Back()
                                }
                            },
                            entryProvider = { key ->
                                when (key) {
                                    is Nav3LeafKey.LeafHome -> NavEntry(key) {
                                        Nav3LeafStubScreen("Nav3 Leaf Home", COLORS[2])
                                    }
                                    is Nav3LeafKey.LeafDetail -> NavEntry(key) {
                                        Nav3LeafStubScreen("Nav3 Leaf Detail", COLORS[3])
                                    }
                                    is Nav3LeafKey.LeafDialogModal -> NavEntry(key) {
                                        Nav3LeafDialogModal(
                                            onDismiss = { this@Nav2ToNav3InteropActivity.popNav3LeafBack() },
                                        )
                                    }
                                    is Nav3LeafKey.LeafSheetModal -> NavEntry(key) {
                                        Nav3LeafSheetModal(
                                            onDismiss = { this@Nav2ToNav3InteropActivity.popNav3LeafBack() },
                                        )
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
        val previousRoute = navController.currentBackStackEntry?.destination?.route
        navController.navigate(route) {
            launchSingleTop = singleTop
        }
        if (!(singleTop && previousRoute == route)) {
            nav2BackStackDepthValue += 1
        }
        NavLogger.push(TAG, route, nav2BackStackDepthValue)
    }

    /** Pop the Nav2 back stack. */
    fun popNav2Back(): Boolean {
        val from = navController.currentBackStackEntry?.destination?.route ?: "?"
        val result = navController.popBackStack()
        if (result) {
            if (nav2BackStackDepthValue > 1) nav2BackStackDepthValue -= 1
            NavLogger.pop(TAG, from, nav2BackStackDepthValue)
        }
        return result
    }

    /** Navigate within the Nav3 leaf. */
    fun navigateNav3Leaf(key: Nav3LeafKey) {
        nav3LeafBackStack.add(key)
        NavLogger.push(TAG, key::class.simpleName ?: "?", nav3LeafBackStack.size)
    }

    /** Pop the Nav3 leaf back stack. */
    fun popNav3LeafBack(): Boolean {
        if (nav3LeafBackStack.size <= 1) return false
        val from = nav3LeafBackStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
        nav3LeafBackStack.removeLastOrNull()
        NavLogger.pop(TAG, from, nav3LeafBackStack.size)
        return true
    }

    /** Nav2 back stack depth. */
    val nav2BackStackDepth: Int
        get() = nav2BackStackDepthValue

    /** Current Nav2 route. */
    val currentNav2Route: String?
        get() = navController.currentBackStackEntry?.destination?.route

    /** Open parent Nav2 dialog route. */
    fun openParentDialog() {
        navigateTo(ROUTE_RESULT_DIALOG)
    }

    /** Dismiss parent Nav2 dialog if currently visible. */
    fun dismissParentDialog(): Boolean {
        if (!isParentDialogVisible) return false
        return popNav2Back()
    }

    /** Whether parent Nav2 dialog is currently visible. */
    val isParentDialogVisible: Boolean
        get() = currentNav2Route == ROUTE_RESULT_DIALOG

    /** Nav3 leaf back stack depth. */
    val nav3LeafBackStackDepth: Int
        get() = nav3LeafBackStack.size

    /** Open dialog-style modal key inside Nav3 leaf. */
    fun openLeafDialogModal() {
        navigateNav3Leaf(Nav3LeafKey.LeafDialogModal)
    }

    /** Open sheet-style modal key inside Nav3 leaf. */
    fun openLeafSheetModal() {
        navigateNav3Leaf(Nav3LeafKey.LeafSheetModal)
    }

    /** Dismiss current modal key inside Nav3 leaf. */
    fun dismissLeafModal(): Boolean {
        if (!isLeafModalVisible) return false
        return popNav3LeafBack()
    }

    /** Whether Nav3 leaf dialog modal is visible. */
    val isLeafDialogVisible: Boolean
        get() = nav3LeafBackStack.lastOrNull() is Nav3LeafKey.LeafDialogModal

    /** Whether Nav3 leaf sheet modal is visible. */
    val isLeafSheetVisible: Boolean
        get() = nav3LeafBackStack.lastOrNull() is Nav3LeafKey.LeafSheetModal

    /** Whether any Nav3 leaf modal is visible. */
    val isLeafModalVisible: Boolean
        get() = isLeafDialogVisible || isLeafSheetVisible

    companion object {
        private const val TAG = "T7Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        const val ROUTE_HOME = "home"
        const val ROUTE_SCREEN_A = "screen_a"
        const val ROUTE_NAV3_LEAF = "nav3_leaf"
        const val ROUTE_RESULT_DIALOG = "result_dialog"

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
    data object LeafDialogModal : Nav3LeafKey
    data object LeafSheetModal : Nav3LeafKey
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

@androidx.compose.runtime.Composable
private fun ParentDialogStub(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Parent Nav2 Dialog",
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
private fun Nav3LeafDialogModal(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Nav3 Leaf Dialog",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun Nav3LeafSheetModal(onDismiss: () -> Unit) {
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
                    text = "Nav3 Leaf Sheet",
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
