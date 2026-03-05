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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.ItemDetailScreen
import com.example.navigationlab.recipes.content.ItemExtraScreen
import com.example.navigationlab.recipes.content.ItemListScreen
import com.example.navigationlab.recipes.helpers.DefaultTransitions
import com.example.navigationlab.recipes.keys.ItemDetail
import com.example.navigationlab.recipes.keys.ItemExtra
import com.example.navigationlab.recipes.keys.ItemList

/**
 * Host for R17: Adaptive list-detail layout.
 * Uses ListDetailSceneStrategy from material3-adaptive to render multi-pane layout
 * based on WindowSizeClass.
 */
class RecipeAdaptiveHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        findViewById<TextView>(R.id.tvTopologyLabel).text = getString(
            R.string.topology_label_with_case_mode,
            getString(R.string.topology_recipe_adaptive),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                AdaptiveContent(
                    onExit = { finish() },
                )
            }
        }
    }

    companion object {
        private const val TAG = "RecipeAdaptiveHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeAdaptiveHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AdaptiveContent(onExit: () -> Unit) {
    val backStack = rememberNavBackStack(ItemList)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

    Scaffold { paddingValues ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(paddingValues),
            onBack = {
                if (backStack.size > 1) {
                    val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                    backStack.removeLastOrNull()
                    NavLogger.back("RecipeAdaptiveHost", from, backStack.size)
                } else {
                    onExit()
                }
            },
            transitionSpec = DefaultTransitions.crossFade(),
            popTransitionSpec = DefaultTransitions.crossFadeBack(),
            predictivePopTransitionSpec = DefaultTransitions.predictiveCrossFadeBack(),
            sceneStrategy = listDetailStrategy,
            entryProvider = entryProvider {
                entry<ItemList>(
                    metadata = ListDetailSceneStrategy.listPane(),
                ) {
                    ItemListScreen(
                        onItemClick = { id ->
                            backStack.add(ItemDetail(id))
                            NavLogger.push("RecipeAdaptiveHost", "ItemDetail", backStack.size)
                        },
                    )
                }
                entry<ItemDetail>(
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) { key ->
                    ItemDetailScreen(
                        id = key.id,
                        onExtra = {
                            backStack.add(ItemExtra(key.id))
                            NavLogger.push("RecipeAdaptiveHost", "ItemExtra", backStack.size)
                        },
                    )
                }
                entry<ItemExtra>(
                    metadata = ListDetailSceneStrategy.extraPane(),
                ) { key ->
                    ItemExtraScreen(id = key.id)
                }
            },
        )
    }
}
