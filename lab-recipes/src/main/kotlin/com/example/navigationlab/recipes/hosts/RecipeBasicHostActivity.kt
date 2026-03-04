package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.ContentBlue
import com.example.navigationlab.recipes.content.ContentGreen
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

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T3: Recipe Basic - $caseCode"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                when (caseCode) {
                    "R01" -> {
                        // R01: Basic Nav3 with mutableStateListOf
                        val backStack = remember { mutableStateListOf<Any>(BasicRouteA) }
                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            entryProvider = { key ->
                                when (key) {
                                    is BasicRouteA -> NavEntry(key) {
                                        ContentGreen("Welcome to Nav3") {
                                            Button(onClick = dropUnlessResumed {
                                                backStack.add(BasicRouteB("123"))
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
                    }
                    "R02" -> {
                        // R02: BasicSaveable with rememberNavBackStack
                        val backStack = rememberNavBackStack(SaveableRouteA)
                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            entryProvider = { key ->
                                when (key) {
                                    is SaveableRouteA -> NavEntry(key) {
                                        ContentGreen("Welcome to Nav3") {
                                            Button(onClick = dropUnlessResumed {
                                                backStack.add(SaveableRouteB("123"))
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
                    }
                    "R03" -> {
                        // R03: BasicDsl with entryProvider DSL
                        val backStack = rememberNavBackStack(DslRouteA)
                        NavDisplay(
                            backStack = backStack,
                            onBack = { backStack.removeLastOrNull() },
                            entryProvider = entryProvider {
                                entry<DslRouteA> {
                                    ContentGreen("Welcome to Nav3") {
                                        Button(onClick = dropUnlessResumed {
                                            backStack.add(DslRouteB("123"))
                                        }) { Text("Click to navigate") }
                                    }
                                }
                                entry<DslRouteB> { key ->
                                    ContentBlue("Route id: ${key.id}")
                                }
                            },
                        )
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
