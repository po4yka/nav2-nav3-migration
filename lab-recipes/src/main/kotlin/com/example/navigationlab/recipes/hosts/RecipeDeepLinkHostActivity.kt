package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.DeepLinkHomeScreen
import com.example.navigationlab.recipes.content.DeepLinkTargetScreen
import com.example.navigationlab.recipes.keys.DeepLinkHome
import com.example.navigationlab.recipes.keys.DeepLinkTarget

/**
 * Host for R13: Deep link bridging to Nav3.
 * Reads deep link intent extras and navigates accordingly.
 */
class RecipeDeepLinkHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T3: Recipe Deep Link - $caseCode"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                DeepLinkContent()
            }
        }
    }

    companion object {
        private const val TAG = "RecipeDeepLinkHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"
        const val ACTION_SHOW_TARGET = "com.example.navigationlab.recipes.action.SHOW_TARGET"
        const val KEY_PARAM = "deep_link_param"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeDeepLinkHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }

        fun createDeepLinkIntent(context: Context, param: String): Intent =
            Intent(context, RecipeDeepLinkHostActivity::class.java).apply {
                action = ACTION_SHOW_TARGET
                putExtra(KEY_PARAM, param)
                putExtra(EXTRA_CASE_ID, "R13")
            }
    }
}

@Composable
private fun DeepLinkContent() {
    val backStack = rememberNavBackStack(DeepLinkHome)
    val context = LocalContext.current

    // Handle deep link intent once
    var isDeepLinkConsumed = rememberSaveable { false }
    LaunchedEffect(context) {
        if (isDeepLinkConsumed) return@LaunchedEffect
        val activity = context as? RecipeDeepLinkHostActivity ?: return@LaunchedEffect
        val intent = activity.intent ?: return@LaunchedEffect

        if (intent.action == RecipeDeepLinkHostActivity.ACTION_SHOW_TARGET) {
            val param = intent.getStringExtra(RecipeDeepLinkHostActivity.KEY_PARAM) ?: return@LaunchedEffect
            backStack.add(DeepLinkTarget(param = param))
            isDeepLinkConsumed = true
        }
    }

    Scaffold { paddingValues ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(paddingValues),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<DeepLinkHome> {
                    DeepLinkHomeScreen(
                        onNavigate = { backStack.add(DeepLinkTarget(param = "manual")) },
                    )
                }
                entry<DeepLinkTarget> { key ->
                    DeepLinkTargetScreen(param = key.param)
                }
            },
        )
    }
}
