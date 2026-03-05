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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
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
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_t5),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        when {
                            isIslandModalVisible -> dismissIslandModal()
                            popIslandFragmentBack() -> Unit
                            backStack.size > 1 -> {
                                val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                                backStack.removeLastOrNull()
                                NavLogger.back(TAG, from, backStack.size)
                            }
                            else -> finish()
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
                                    onDismiss = { this@Nav3FragmentIslandActivity.popNav3Back() },
                                )
                            }
                            is Nav3Key.PopupOverlay -> NavEntry(key) {
                                ParentPopupOverlayContent(
                                    onDismiss = { this@Nav3FragmentIslandActivity.popNav3Back() },
                                )
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
        NavLogger.push(TAG, key::class.simpleName ?: "?", backStack.size)
    }

    /** Pop the Nav3 root back stack. */
    fun popNav3Back(): Boolean {
        if (backStack.size <= 1) return false
        val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
        backStack.removeLastOrNull()
        NavLogger.pop(TAG, from, backStack.size)
        return true
    }

    /** Add a fragment to the legacy island container. */
    fun showIslandFragment(fragment: Fragment) {
        if (!isLegacyIslandVisible) return
        if (findViewById<FragmentContainerView?>(R.id.legacyFragmentContainer) == null) return
        supportFragmentManager.beginTransaction()
            .replace(R.id.legacyFragmentContainer, fragment)
            .addToBackStack("island")
            .commit()
    }

    /** Open the legacy fragment island key in Nav3 stack. */
    fun openLegacyIsland() {
        navigateTo(LegacyIslandKey)
    }

    /** Open parent dialog-style Nav3 modal while island is active. */
    fun openParentDialog() {
        navigateTo(Nav3Key.DialogModal)
    }

    /** Open parent popup-style Nav3 overlay while island is active. */
    fun openParentPopup() {
        navigateTo(Nav3Key.PopupOverlay)
    }

    /** Dismiss parent modal/popup entry if present. */
    fun dismissParentModalOrPopup(): Boolean {
        if (!isParentDialogVisible && !isParentPopupVisible) return false
        return popNav3Back()
    }

    /** Show a DialogFragment-based island modal. */
    fun showIslandModal() {
        if (!isLegacyIslandVisible || isIslandModalVisible) return
        IslandModalDialogFragment.newInstance("Island Modal")
            .show(supportFragmentManager, ISLAND_MODAL_TAG)
    }

    /** Dismiss the DialogFragment-based island modal if visible. */
    fun dismissIslandModal(): Boolean {
        val dialog = supportFragmentManager.findFragmentByTag(ISLAND_MODAL_TAG) as? IslandModalDialogFragment
        dialog ?: return false
        dialog.dismissAllowingStateLoss()
        return true
    }

    /** Pop the fragment island back stack. */
    fun popIslandFragmentBack(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            NavLogger.pop(TAG, "IslandFragment", supportFragmentManager.backStackEntryCount - 1)
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

    /** Whether legacy island key is top-most in Nav3 back stack. */
    val isLegacyIslandVisible: Boolean
        get() = backStack.lastOrNull() is LegacyIslandKey

    /** Whether parent dialog-style modal is top-most. */
    val isParentDialogVisible: Boolean
        get() = backStack.lastOrNull() is Nav3Key.DialogModal

    /** Whether parent popup-style overlay is top-most. */
    val isParentPopupVisible: Boolean
        get() = backStack.lastOrNull() is Nav3Key.PopupOverlay

    /** Whether DialogFragment-based island modal is visible. */
    val isIslandModalVisible: Boolean
        get() = (supportFragmentManager.findFragmentByTag(ISLAND_MODAL_TAG) as? IslandModalDialogFragment)
            ?.dialog
            ?.isShowing == true

    companion object {
        private const val TAG = "T5Host"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"
        private const val ISLAND_MODAL_TAG = "island_modal"

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
