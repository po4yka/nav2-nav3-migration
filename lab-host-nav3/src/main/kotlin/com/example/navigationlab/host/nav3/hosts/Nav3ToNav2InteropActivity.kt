package com.example.navigationlab.host.nav3.hosts

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
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

    /** Pending Nav2 leaf route to restore after recreation (G02/G08). */
    private var pendingLeafRestoreRoute: String? = null

    /** Last route requested by UI-source navigation in the Nav2 leaf (H04). */
    var lastUiLeafNavigationRoute: String? = null
        private set

    /** Last route requested by deeplink-source navigation in the Nav2 leaf (H04). */
    var lastDeeplinkLeafNavigationRoute: String? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav3_host)

        restoreParentBackStack(savedInstanceState)
        pendingLeafRestoreRoute = savedInstanceState?.getString(STATE_PENDING_LEAF_RESTORE_ROUTE)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_t8),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
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
                            is Nav3Key.DialogModal -> NavEntry(key) {
                                ParentDialogModalContent(
                                    onDismiss = { this@Nav3ToNav2InteropActivity.popNav3Back() },
                                )
                            }
                            is Nav3Key.PopupOverlay -> NavEntry(key) {
                                ParentPopupOverlayContent(
                                    onDismiss = { this@Nav3ToNav2InteropActivity.popNav3Back() },
                                )
                            }
                            is Nav2LeafKey -> NavEntry(key) {
                                val controller = rememberNavController()
                                nav2LeafController = controller
                                nav2LeafDepthValue = 1
                                LaunchedEffect(controller, pendingLeafRestoreRoute) {
                                    val restoreRoute = pendingLeafRestoreRoute ?: return@LaunchedEffect
                                    if (controller.currentBackStackEntry?.destination?.route != LEAF_ROUTE_HOME) {
                                        pendingLeafRestoreRoute = null
                                        return@LaunchedEffect
                                    }
                                    controller.navigate(restoreRoute)
                                    nav2LeafDepthValue += 1
                                    pendingLeafRestoreRoute = null
                                }
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
                                    dialog(LEAF_ROUTE_DIALOG) {
                                        LeafDialogModalContent(
                                            onDismiss = { this@Nav3ToNav2InteropActivity.popNav2LeafBack() },
                                        )
                                    }
                                    dialog(LEAF_ROUTE_SHEET) {
                                        LeafSheetModalContent(
                                            onDismiss = { this@Nav3ToNav2InteropActivity.popNav2LeafBack() },
                                        )
                                    }
                                    dialog(
                                        route = LEAF_ROUTE_FULL_SCREEN_DIALOG,
                                        dialogProperties = DialogProperties(
                                            usePlatformDefaultWidth = false,
                                            decorFitsSystemWindows = false,
                                        ),
                                    ) {
                                        LeafFullScreenDialogModalContent(
                                            onDismiss = { this@Nav3ToNav2InteropActivity.popNav2LeafBack() },
                                        )
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

    /** Navigate Nav2 leaf route attributed to UI source (H04). */
    fun navigateNav2LeafFromUi(route: String) {
        lastUiLeafNavigationRoute = route
        navigateNav2Leaf(route)
    }

    /** Navigate Nav2 leaf route attributed to deeplink source (H04). */
    fun navigateNav2LeafFromDeeplink(route: String) {
        lastDeeplinkLeafNavigationRoute = route
        navigateNav2Leaf(route)
    }

    /**
     * Resolve concurrent UI/deeplink updates with deterministic ordering policy:
     * UI request first, deeplink request second (deeplink wins as top-most).
     */
    fun resolveConcurrentLeafNavigation(uiRoute: String, deeplinkRoute: String): String {
        navigateNav2LeafFromUi(uiRoute)
        navigateNav2LeafFromDeeplink(deeplinkRoute)
        return deeplinkRoute
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

    /** Current Nav2 leaf route (null if leaf not active). */
    val currentLeafRoute: String?
        get() = nav2LeafController?.currentBackStackEntry?.destination?.route

    /** Open parent dialog-style Nav3 modal entry. */
    fun openParentDialog() {
        navigateTo(Nav3Key.DialogModal)
    }

    /** Open parent popup-style Nav3 overlay entry. */
    fun openParentPopup() {
        navigateTo(Nav3Key.PopupOverlay)
    }

    /** Dismiss parent modal/popup entry if present. */
    fun dismissParentModalOrPopup(): Boolean {
        if (!isParentDialogVisible && !isParentPopupVisible) return false
        return popNav3Back()
    }

    /** Whether parent dialog-style entry is visible. */
    val isParentDialogVisible: Boolean
        get() = backStack.lastOrNull() is Nav3Key.DialogModal

    /** Whether parent popup-style entry is visible. */
    val isParentPopupVisible: Boolean
        get() = backStack.lastOrNull() is Nav3Key.PopupOverlay

    /** Open child dialog-style Nav2 route inside leaf. */
    fun openLeafDialog() {
        navigateNav2Leaf(LEAF_ROUTE_DIALOG)
    }

    /** Open child sheet-style Nav2 route inside leaf. */
    fun openLeafSheet() {
        navigateNav2Leaf(LEAF_ROUTE_SHEET)
    }

    /** Open child fullscreen dialog Nav2 route inside leaf. */
    fun openLeafFullScreenDialog() {
        navigateNav2Leaf(LEAF_ROUTE_FULL_SCREEN_DIALOG)
    }

    /** Dismiss child modal route when visible. */
    fun dismissLeafModal(): Boolean {
        if (!isLeafDialogVisible && !isLeafSheetVisible && !isLeafFullScreenDialogVisible) return false
        return popNav2LeafBack()
    }

    /** Whether child dialog-style route is visible. */
    val isLeafDialogVisible: Boolean
        get() = currentLeafRoute == LEAF_ROUTE_DIALOG

    /** Whether child sheet-style route is visible. */
    val isLeafSheetVisible: Boolean
        get() = currentLeafRoute == LEAF_ROUTE_SHEET

    /** Whether child fullscreen dialog route is visible. */
    val isLeafFullScreenDialogVisible: Boolean
        get() = currentLeafRoute == LEAF_ROUTE_FULL_SCREEN_DIALOG

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(
            STATE_PARENT_BACK_STACK_KEYS,
            ArrayList(backStack.mapNotNull(::keyToToken)),
        )
        val route = currentLeafRoute
        if (route != null && route != LEAF_ROUTE_HOME) {
            outState.putString(STATE_PENDING_LEAF_RESTORE_ROUTE, route)
        }
    }

    private fun restoreParentBackStack(savedState: Bundle?) {
        val tokens = savedState?.getStringArrayList(STATE_PARENT_BACK_STACK_KEYS) ?: return
        if (tokens.isEmpty()) return
        val restored = tokens.mapNotNull(::tokenToKey)
        if (restored.isEmpty()) return
        backStack.clear()
        backStack.addAll(restored)
    }

    private fun keyToToken(key: Any): String? = when (key) {
        is Nav3Key.Home -> TOKEN_HOME
        is Nav3Key.ScreenA -> TOKEN_SCREEN_A
        is Nav3Key.DialogModal -> TOKEN_DIALOG
        is Nav3Key.PopupOverlay -> TOKEN_POPUP
        is Nav2LeafKey -> TOKEN_NAV2_LEAF
        else -> null
    }

    private fun tokenToKey(token: String): Any? = when (token) {
        TOKEN_HOME -> Nav3Key.Home
        TOKEN_SCREEN_A -> Nav3Key.ScreenA
        TOKEN_DIALOG -> Nav3Key.DialogModal
        TOKEN_POPUP -> Nav3Key.PopupOverlay
        TOKEN_NAV2_LEAF -> Nav2LeafKey
        else -> null
    }

    companion object {
        private const val TAG = "T8Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        const val LEAF_ROUTE_HOME = "leaf_home"
        const val LEAF_ROUTE_DETAIL = "leaf_detail"
        const val LEAF_ROUTE_DIALOG = "leaf_dialog"
        const val LEAF_ROUTE_SHEET = "leaf_sheet"
        const val LEAF_ROUTE_FULL_SCREEN_DIALOG = "leaf_full_screen_dialog"

        private const val STATE_PARENT_BACK_STACK_KEYS = "state_parent_back_stack_keys"
        private const val STATE_PENDING_LEAF_RESTORE_ROUTE = "pending_leaf_restore_route"

        private const val TOKEN_HOME = "home"
        private const val TOKEN_SCREEN_A = "screen_a"
        private const val TOKEN_DIALOG = "dialog"
        private const val TOKEN_POPUP = "popup"
        private const val TOKEN_NAV2_LEAF = "nav2_leaf"

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

@androidx.compose.runtime.Composable
private fun ParentDialogModalContent(onDismiss: () -> Unit) {
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
                    text = "Parent Nav3 Dialog",
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
private fun ParentPopupOverlayContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.TopEnd)
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Parent Popup",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun LeafDialogModalContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Leaf Nav2 Dialog",
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
private fun LeafSheetModalContent(onDismiss: () -> Unit) {
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
                    text = "Leaf Nav2 Sheet",
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
private fun LeafFullScreenDialogModalContent(onDismiss: () -> Unit) {
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
                    text = "Leaf Nav2 Fullscreen Dialog",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}
