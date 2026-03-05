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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.HomeScreen
import com.example.navigationlab.recipes.content.Person
import com.example.navigationlab.recipes.content.PersonDetailsScreen
import com.example.navigationlab.recipes.helpers.DefaultTransitions
import com.example.navigationlab.recipes.helpers.ResultEffect
import com.example.navigationlab.recipes.helpers.ResultEventBus
import com.example.navigationlab.recipes.helpers.rememberResultStore
import com.example.navigationlab.recipes.keys.ResultHome
import com.example.navigationlab.recipes.keys.ResultPersonDetailsForm

/**
 * Host for R07 (Results/Event) and R08 (Results/State).
 * R07 uses ResultEventBus (Channel-based event results).
 * R08 uses ResultStore (State-based saveable results).
 */
class RecipeResultsHostActivity : AppCompatActivity() {

    internal var navigateToFormAction: (() -> Unit)? = null
    internal var submitPersonAction: ((String, String) -> Unit)? = null
    internal var popBackAction: (() -> Boolean)? = null
    internal var currentRouteProvider: (() -> Any?)? = null
    internal var backStackDepthProvider: (() -> Int)? = null
    internal var currentPersonProvider: (() -> Person?)? = null

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
            getString(R.string.topology_recipe_results),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                when (caseCode) {
                    "R07" -> ResultEventContent(
                        host = this@RecipeResultsHostActivity,
                        onExit = { finish() },
                    )
                    "R08" -> ResultStateContent(
                        host = this@RecipeResultsHostActivity,
                        onExit = { finish() },
                    )
                }
            }
        }
    }

    fun openPersonDetailsForm() {
        navigateToFormAction?.invoke()
    }

    fun submitPerson(name: String, favoriteColor: String) {
        submitPersonAction?.invoke(name, favoriteColor)
    }

    fun popBack(): Boolean = popBackAction?.invoke() ?: false

    val currentRouteName: String?
        get() = currentRouteProvider?.invoke()?.let { it::class.simpleName }

    val backStackDepth: Int
        get() = backStackDepthProvider?.invoke() ?: 0

    val currentPerson: Person?
        get() = currentPersonProvider?.invoke()

    companion object {
        private const val TAG = "RecipeResultsHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeResultsHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

@Composable
private fun ResultEventContent(host: RecipeResultsHostActivity, onExit: () -> Unit) {
    val resultBus = remember { ResultEventBus() }
    var latestPerson by remember { mutableStateOf<Person?>(null) }

    Scaffold { paddingValues ->
        val backStack = rememberNavBackStack(ResultHome)
        host.navigateToFormAction = {
            backStack.add(ResultPersonDetailsForm())
            NavLogger.push("RecipeResultsHost", "ResultPersonDetailsForm", backStack.size)
        }
        host.submitPersonAction = { name, favoriteColor ->
            val person = Person(name = name, favoriteColor = favoriteColor)
            resultBus.sendResult<Person>(result = person)
            latestPerson = person
            NavLogger.result("RecipeResultsHost", "EventBus", "Person")
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
                NavLogger.pop("RecipeResultsHost", "ResultPersonDetailsForm", backStack.size)
            } else {
                onExit()
            }
        }
        host.popBackAction = {
            if (backStack.size > 1) {
                val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                backStack.removeLastOrNull()
                NavLogger.back("RecipeResultsHost", from, backStack.size)
                true
            } else {
                false
            }
        }
        host.currentRouteProvider = { backStack.lastOrNull() }
        host.backStackDepthProvider = { backStack.size }
        host.currentPersonProvider = { latestPerson }

        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(paddingValues),
            onBack = {
                if (backStack.size > 1) {
                    val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                    backStack.removeLastOrNull()
                    NavLogger.back("RecipeResultsHost", from, backStack.size)
                } else {
                    onExit()
                }
            },
            transitionSpec = DefaultTransitions.slideForward(),
            popTransitionSpec = DefaultTransitions.slideBack(),
            predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
            entryProvider = entryProvider {
                entry<ResultHome> {
                    var person by remember { mutableStateOf<Person?>(null) }
                    ResultEffect<Person>(resultBus) { p ->
                        person = p
                        latestPerson = p
                    }
                    HomeScreen(
                        person = person,
                        onNext = {
                            host.navigateToFormAction?.invoke()
                        },
                    )
                }
                entry<ResultPersonDetailsForm> {
                    PersonDetailsScreen(
                        onSubmit = { person ->
                            host.submitPersonAction?.invoke(person.name, person.favoriteColor)
                        },
                    )
                }
            },
        )
    }
}

@Composable
private fun ResultStateContent(host: RecipeResultsHostActivity, onExit: () -> Unit) {
    val resultStore = rememberResultStore()
    var latestPerson by remember { mutableStateOf<Person?>(null) }

    Scaffold { paddingValues ->
        val backStack = rememberNavBackStack(ResultHome)
        host.navigateToFormAction = {
            backStack.add(ResultPersonDetailsForm())
            NavLogger.push("RecipeResultsHost", "ResultPersonDetailsForm", backStack.size)
        }
        host.submitPersonAction = { name, favoriteColor ->
            val person = Person(name = name, favoriteColor = favoriteColor)
            resultStore.setResult<Person>(result = person)
            latestPerson = person
            NavLogger.result("RecipeResultsHost", "StateStore", "Person")
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
                NavLogger.pop("RecipeResultsHost", "ResultPersonDetailsForm", backStack.size)
            } else {
                onExit()
            }
        }
        host.popBackAction = {
            if (backStack.size > 1) {
                val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                backStack.removeLastOrNull()
                NavLogger.back("RecipeResultsHost", from, backStack.size)
                true
            } else {
                false
            }
        }
        host.currentRouteProvider = { backStack.lastOrNull() }
        host.backStackDepthProvider = { backStack.size }
        host.currentPersonProvider = { latestPerson }

        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(paddingValues),
            onBack = {
                if (backStack.size > 1) {
                    val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                    backStack.removeLastOrNull()
                    NavLogger.back("RecipeResultsHost", from, backStack.size)
                } else {
                    onExit()
                }
            },
            transitionSpec = DefaultTransitions.slideForward(),
            popTransitionSpec = DefaultTransitions.slideBack(),
            predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
            entryProvider = entryProvider {
                entry<ResultHome> {
                    val person = resultStore.getResultState<Person?>()
                    latestPerson = person
                    HomeScreen(
                        person = person,
                        onNext = {
                            host.navigateToFormAction?.invoke()
                        },
                    )
                }
                entry<ResultPersonDetailsForm> {
                    PersonDetailsScreen(
                        onSubmit = { person ->
                            host.submitPersonAction?.invoke(person.name, person.favoriteColor)
                        },
                    )
                }
            },
        )
    }
}
