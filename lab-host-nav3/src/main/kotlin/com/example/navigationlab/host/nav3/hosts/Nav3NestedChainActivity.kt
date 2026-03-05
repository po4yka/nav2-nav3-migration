package com.example.navigationlab.host.nav3.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
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
import com.example.navigationlab.host.nav3.R
import com.example.navigationlab.host.nav3.compose.Nav3StubScreen

/**
 * B09 stress test: Nav3 -> Nav2 -> Fragment -> Nav2 dialog -> back unwind.
 *
 * Layer 1: Nav3 NavDisplay root (Home, ChainEntry keys).
 * Layer 2: ChainEntry key renders Nav2 NavHost (chain_root, fragment_layer routes).
 * Layer 3: fragment_layer route renders AndroidView(FragmentContainerView) hosting
 *          [ChainStubFragment] which contains ComposeView + Nav2 NavHost with dialog.
 * Layer 4: Nav2 dialog inside the fragment.
 *
 * Back unwind verifies: dialog -> fragment Nav2 -> fragment -> Nav2 chain -> Nav3.
 */
class Nav3NestedChainActivity : AppCompatActivity() {

    /** Nav3 root back stack (Layer 1). */
    val nav3BackStack = mutableStateListOf<Any>(ChainNav3Key.Home)

    /** Nav2 chain controller (Layer 2) -- available when ChainEntry is displayed. */
    var nav2ChainController: NavHostController? = null
        private set

    /** Fragment's Nav2 controller (Layer 3/4) -- available when fragment is hosted. */
    var fragmentNav2Controller: NavHostController? = null
        private set

    /** Last dialog result from the fragment's Nav2 dialog (Layer 4). */
    var lastDialogResult: String? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav3_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = "B09: Nav3->Nav2->Fragment->Dialog - $caseCode [$runMode]"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                NavDisplay(
                    backStack = nav3BackStack,
                    onBack = {
                        if (nav3BackStack.size > 1) {
                            val from = nav3BackStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                            nav3BackStack.removeLastOrNull()
                            NavLogger.back(TAG, from, nav3BackStack.size)
                        } else {
                            finish()
                        }
                    },
                    entryProvider = { key ->
                        when (key) {
                            is ChainNav3Key.Home -> NavEntry(key) {
                                Nav3StubScreen("Home", COLORS[0])
                            }
                            is ChainNav3Key.ChainEntry -> NavEntry(key) {
                                // Layer 2: Nav2 NavHost
                                val controller = rememberNavController()
                                nav2ChainController = controller
                                Nav2ChainLayer(controller)
                            }
                            else -> NavEntry(key) {
                                Nav3StubScreen("Unknown", COLORS[5])
                            }
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun Nav2ChainLayer(controller: NavHostController) {
        NavHost(
            navController = controller,
            startDestination = CHAIN_ROUTE_ROOT,
        ) {
            composable(CHAIN_ROUTE_ROOT) {
                Nav3StubScreen("Nav2 Chain Root", COLORS[1])
            }
            composable(CHAIN_ROUTE_FRAGMENT) {
                // Layer 3: Fragment island with its own ComposeView + Nav2
                AndroidView(
                    factory = { context ->
                        FragmentContainerView(context).apply {
                            id = View.generateViewId()
                            post {
                                val fragment = ChainStubFragment.newInstance { navCtrl, result ->
                                    fragmentNav2Controller = navCtrl
                                    if (result != null) lastDialogResult = result
                                }
                                supportFragmentManager.beginTransaction()
                                    .replace(id, fragment, TAG_CHAIN_FRAGMENT)
                                    .commit()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    // --- Navigation helpers ---

    /** Navigate to the chain entry on Nav3 (Layer 1 -> Layer 2). */
    fun enterChain() {
        nav3BackStack.add(ChainNav3Key.ChainEntry)
        NavLogger.push(TAG, "ChainEntry", nav3BackStack.size)
    }

    /** Navigate within Nav2 chain (Layer 2). */
    fun navigateNav2Chain(route: String) {
        nav2ChainController?.navigate(route)
        NavLogger.push(TAG, route, nav2ChainController?.currentBackStack?.value?.size ?: 0)
    }

    /** Navigate within fragment's Nav2 (Layer 3/4). */
    fun navigateFragmentNav2(route: String) {
        fragmentNav2Controller?.navigate(route)
        NavLogger.push(TAG, route, fragmentNav2Controller?.currentBackStack?.value?.size ?: 0)
    }

    /** Pop Nav3 root back stack. */
    fun popNav3Back(): Boolean {
        if (nav3BackStack.size <= 1) return false
        val from = nav3BackStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
        nav3BackStack.removeLastOrNull()
        NavLogger.pop(TAG, from, nav3BackStack.size)
        return true
    }

    /** Pop Nav2 chain back stack. */
    fun popNav2ChainBack(): Boolean = nav2ChainController?.popBackStack() ?: false

    /** Pop fragment's Nav2 back stack. */
    fun popFragmentNav2Back(): Boolean = fragmentNav2Controller?.popBackStack() ?: false

    val nav3Depth: Int get() = nav3BackStack.size
    val nav2ChainDepth: Int get() = nav2ChainController?.currentBackStack?.value?.size ?: 0

    companion object {
        private const val TAG = "B09Host"
        private const val TAG_CHAIN_FRAGMENT = "chain_stub_fragment"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        const val CHAIN_ROUTE_ROOT = "chain_root"
        const val CHAIN_ROUTE_FRAGMENT = "fragment_layer"

        const val FRAG_ROUTE_HOME = "frag_home"
        const val FRAG_ROUTE_DIALOG = "frag_dialog"

        val COLORS = listOf(
            Color(0xFF6200EE), // Purple - Nav3 Home
            Color(0xFF03DAC5), // Teal - Nav2 Chain Root
            Color(0xFFBB86FC), // Light purple - Fragment
            Color(0xFF018786), // Dark teal
            Color(0xFFCF6679), // Pink
            Color(0xFF3700B3), // Deep purple
        )

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, Nav3NestedChainActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

/** Nav3 keys for B09 nested chain. */
sealed interface ChainNav3Key {
    data object Home : ChainNav3Key
    data object ChainEntry : ChainNav3Key
}

/**
 * Fragment for Layer 3 of B09 chain: hosts ComposeView with Nav2 NavHost + dialog.
 * Provides its Nav2 controller and dialog result via a callback.
 */
class ChainStubFragment : androidx.fragment.app.Fragment() {

    private var onStateUpdate: ((NavHostController?, String?) -> Unit)? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    val controller = rememberNavController()
                    onStateUpdate?.invoke(controller, null)

                    NavHost(
                        navController = controller,
                        startDestination = Nav3NestedChainActivity.FRAG_ROUTE_HOME,
                    ) {
                        composable(Nav3NestedChainActivity.FRAG_ROUTE_HOME) {
                            ChainFragmentStubScreen("Fragment Nav2 Home", Nav3NestedChainActivity.COLORS[2])
                        }
                        dialog(Nav3NestedChainActivity.FRAG_ROUTE_DIALOG) {
                            ChainDialogContent(
                                onConfirm = {
                                    onStateUpdate?.invoke(controller, "confirmed")
                                    controller.popBackStack()
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(onStateUpdate: (NavHostController?, String?) -> Unit): ChainStubFragment {
            return ChainStubFragment().also {
                it.onStateUpdate = onStateUpdate
            }
        }
    }
}

@Composable
private fun ChainFragmentStubScreen(label: String, color: Color) {
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

@Composable
private fun ChainDialogContent(onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Chain Dialog (Layer 4)",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onConfirm) {
                Text("Confirm & Return Result")
            }
        }
    }
}
