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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
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
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_t3),
            caseCode,
            runMode,
        )

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
                            is Nav3Key.ScreenB -> NavEntry(key) {
                                Nav3StubScreen("Screen B", COLORS[2])
                            }
                            is Nav3Key.ScreenC -> NavEntry(key) {
                                Nav3StubScreen("Screen C", COLORS[3])
                            }
                            is Nav3Key.DialogModal -> NavEntry(key) {
                                Nav3DialogModalContent(
                                    onDismiss = { this@Nav3HostActivity.popBack() },
                                )
                            }
                            is Nav3Key.SheetModal -> NavEntry(key) {
                                Nav3SheetModalContent(
                                    onDismiss = { this@Nav3HostActivity.popBack() },
                                )
                            }
                            is Nav3Key.PopupOverlay -> NavEntry(key) {
                                Nav3PopupOverlayContent(
                                    onDismiss = { this@Nav3HostActivity.popBack() },
                                )
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

    /** Open dialog-style Nav3 modal. */
    fun openDialogModal() {
        navigateTo(Nav3Key.DialogModal)
    }

    /** Open sheet-style Nav3 modal. */
    fun openSheetModal() {
        navigateTo(Nav3Key.SheetModal)
    }

    /** Dismiss active modal/popup entry if present. */
    fun dismissModalOrPopup(): Boolean {
        if (!isDialogModalVisible && !isSheetModalVisible && !isPopupVisible) return false
        return popBack()
    }

    /** Whether dialog-style Nav3 modal is currently top-most. */
    val isDialogModalVisible: Boolean
        get() = backStack.lastOrNull() is Nav3Key.DialogModal

    /** Whether sheet-style Nav3 modal is currently top-most. */
    val isSheetModalVisible: Boolean
        get() = backStack.lastOrNull() is Nav3Key.SheetModal

    /** Whether popup-style Nav3 overlay is currently top-most. */
    val isPopupVisible: Boolean
        get() = backStack.lastOrNull() is Nav3Key.PopupOverlay

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

@androidx.compose.runtime.Composable
private fun Nav3DialogModalContent(onDismiss: () -> Unit) {
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
                    text = "Nav3 Dialog Modal",
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
private fun Nav3SheetModalContent(onDismiss: () -> Unit) {
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
                    text = "Nav3 Sheet Modal",
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
private fun Nav3PopupOverlayContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
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
                    text = "Nav3 Popup Overlay",
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
