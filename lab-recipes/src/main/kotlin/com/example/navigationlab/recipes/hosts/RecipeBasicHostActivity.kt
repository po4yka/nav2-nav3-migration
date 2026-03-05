package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.ContentBlue
import com.example.navigationlab.recipes.content.ContentGreen
import com.example.navigationlab.recipes.helpers.DefaultTransitions
import com.example.navigationlab.recipes.helpers.NavStateIndicator
import com.example.navigationlab.recipes.keys.BasicRouteA
import com.example.navigationlab.recipes.keys.BasicRouteB
import com.example.navigationlab.recipes.keys.DslRouteA
import com.example.navigationlab.recipes.keys.DslRouteB
import com.example.navigationlab.recipes.keys.SaveableRouteA
import com.example.navigationlab.recipes.keys.SaveableRouteB

/**
 * Host for R01 (Basic), R02 (BasicSaveable), R03 (BasicDsl).
 * Switches between mutableStateListOf / rememberNavBackStack / DSL entry provider.
 */
class RecipeBasicHostActivity : AppCompatActivity() {

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
            getString(R.string.topology_recipe_basic),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                when (caseCode) {
                    "R01" -> {
                        // R01: Basic Nav3 with mutableStateListOf
                        // No typed transitions: backStack is List<Any>, transition specs require typed Scene<T>
                        val backStack = remember { mutableStateListOf<Any>(BasicRouteA) }
                        Box(Modifier.fillMaxSize()) {
                        NavDisplay(
                            backStack = backStack,
                            onBack = {
                                if (backStack.size > 1) {
                                    val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                                    backStack.removeLastOrNull()
                                    NavLogger.back(TAG, from, backStack.size)
                                } else {
                                    this@RecipeBasicHostActivity.finish()
                                }
                            },
                            entryProvider = { key ->
                                when (key) {
                                    is BasicRouteA -> NavEntry(key) {
                                        ContentGreen("Welcome to Nav3") {
                                            Button(onClick = dropUnlessResumed {
                                                backStack.add(BasicRouteB("123"))
                                                NavLogger.push(TAG, "BasicRouteB", backStack.size)
                                            }) { Text("Click to navigate") }
                                        }
                                    }
                                    is BasicRouteB -> NavEntry(key) {
                                        ContentBlue("Route id: ${key.id}")
                                    }
                                    else -> error("Unknown route: $key")
                                }
                            },
                        )
                        NavStateIndicator(
                            backStackSize = backStack.size,
                            currentRoute = backStack.lastOrNull()?.let { it::class.simpleName ?: "?" } ?: "?",
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                        }
                    }
                    "R02" -> {
                        // R02: BasicSaveable with rememberNavBackStack
                        val backStack = rememberNavBackStack(SaveableRouteA)
                        Box(Modifier.fillMaxSize()) {
                        NavDisplay(
                            backStack = backStack,
                            onBack = {
                                if (backStack.size > 1) {
                                    val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                                    backStack.removeLastOrNull()
                                    NavLogger.back(TAG, from, backStack.size)
                                } else {
                                    this@RecipeBasicHostActivity.finish()
                                }
                            },
                            transitionSpec = DefaultTransitions.slideForward(),
                            popTransitionSpec = DefaultTransitions.slideBack(),
                            predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
                            entryProvider = { key ->
                                when (key) {
                                    is SaveableRouteA -> NavEntry(key) {
                                        ContentGreen("Welcome to Nav3") {
                                            Button(onClick = dropUnlessResumed {
                                                backStack.add(SaveableRouteB("123"))
                                                NavLogger.push(TAG, "SaveableRouteB", backStack.size)
                                            }) { Text("Click to navigate") }
                                        }
                                    }
                                    is SaveableRouteB -> NavEntry(key) {
                                        ContentBlue("Route id: ${key.id}")
                                    }
                                    else -> error("Unknown route: $key")
                                }
                            },
                        )
                        NavStateIndicator(
                            backStackSize = backStack.size,
                            currentRoute = backStack.lastOrNull()?.let { it::class.simpleName ?: "?" } ?: "?",
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                        }
                    }
                    "R03" -> {
                        // R03: BasicDsl with entryProvider DSL
                        val backStack = rememberNavBackStack(DslRouteA)
                        Box(Modifier.fillMaxSize()) {
                        NavDisplay(
                            backStack = backStack,
                            onBack = {
                                if (backStack.size > 1) {
                                    val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                                    backStack.removeLastOrNull()
                                    NavLogger.back(TAG, from, backStack.size)
                                } else {
                                    this@RecipeBasicHostActivity.finish()
                                }
                            },
                            transitionSpec = DefaultTransitions.slideForward(),
                            popTransitionSpec = DefaultTransitions.slideBack(),
                            predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
                            entryProvider = entryProvider {
                                entry<DslRouteA> {
                                    ContentGreen("Welcome to Nav3") {
                                        Button(onClick = dropUnlessResumed {
                                            backStack.add(DslRouteB("123"))
                                            NavLogger.push(TAG, "DslRouteB", backStack.size)
                                        }) { Text("Click to navigate") }
                                    }
                                }
                                entry<DslRouteB> { key ->
                                    ContentBlue("Route id: ${key.id}")
                                }
                            },
                        )
                        NavStateIndicator(
                            backStackSize = backStack.size,
                            currentRoute = backStack.lastOrNull()?.let { it::class.simpleName ?: "?" } ?: "?",
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "RecipeBasicHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeBasicHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}
