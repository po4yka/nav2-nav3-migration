package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.TabAlphaDetailScreen
import com.example.navigationlab.recipes.content.TabAlphaEditScreen
import com.example.navigationlab.recipes.content.TabAlphaScreen
import com.example.navigationlab.recipes.content.TabBetaDetailScreen
import com.example.navigationlab.recipes.content.TabBetaScreen
import com.example.navigationlab.recipes.content.TabGammaDetailScreen
import com.example.navigationlab.recipes.content.TabGammaScreen
import com.example.navigationlab.recipes.helpers.AppState
import com.example.navigationlab.recipes.helpers.DefaultTransitions
import com.example.navigationlab.recipes.helpers.NavStateIndicator
import com.example.navigationlab.recipes.helpers.RecipeViewModel
import com.example.navigationlab.recipes.helpers.TopLevelDestination
import com.example.navigationlab.recipes.helpers.rememberAppState
import com.example.navigationlab.recipes.helpers.rememberResultStore
import com.example.navigationlab.recipes.keys.TabAlpha
import com.example.navigationlab.recipes.keys.TabAlphaDetail
import com.example.navigationlab.recipes.keys.TabAlphaEdit
import com.example.navigationlab.recipes.keys.TabBeta
import com.example.navigationlab.recipes.keys.TabBetaDetail
import com.example.navigationlab.recipes.keys.TabGamma
import com.example.navigationlab.recipes.keys.TabGammaDetail

/**
 * Host for R09-R12: Multi-tab app with AppState orchestrator.
 * R09: Multi-stack tab history (LifoUniqueQueue)
 * R10: Bottom bar visibility control (HIDE / SAME_AS_PARENT)
 * R11: ViewModel preservation in Nav3 entries
 * R12: Result consumption with LaunchedEffect
 */
class RecipeAppStateHostActivity : AppCompatActivity() {

    internal var selectTopLevelAction: ((TopLevelDestination) -> Unit)? = null
    internal var navigateAction: ((NavKey) -> Unit)? = null
    internal var backAction: (() -> Unit)? = null
    internal var submitAlphaEditResultAction: ((String) -> Unit)? = null
    internal var currentTopLevelProvider: (() -> TopLevelDestination)? = null
    internal var currentRouteProvider: (() -> NavKey?)? = null
    internal var currentStackDepthProvider: (() -> Int)? = null
    internal var shouldShowNavigationProvider: (() -> Boolean)? = null
    internal var pendingEditResultProvider: (() -> String?)? = null
    internal var gammaViewModelResultProvider: (() -> String?)? = null

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
            getString(R.string.topology_recipe_appstate),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                AppStateContent(
                    host = this@RecipeAppStateHostActivity,
                    onExit = { finish() },
                )
            }
        }
    }

    fun selectTopLevelAlpha() {
        selectTopLevelAction?.invoke(TopLevelDestination.ALPHA)
    }

    fun selectTopLevelBeta() {
        selectTopLevelAction?.invoke(TopLevelDestination.BETA)
    }

    fun selectTopLevelGamma() {
        selectTopLevelAction?.invoke(TopLevelDestination.GAMMA)
    }

    fun navigate(route: NavKey) {
        navigateAction?.invoke(route)
    }

    fun back() {
        backAction?.invoke()
    }

    fun submitAlphaEditResult(value: String) {
        submitAlphaEditResultAction?.invoke(value)
    }

    val currentTopLevelLabel: String?
        get() = currentTopLevelProvider?.invoke()?.label

    val currentRoute: NavKey?
        get() = currentRouteProvider?.invoke()

    val currentStackDepth: Int
        get() = currentStackDepthProvider?.invoke() ?: 0

    val isBottomNavigationVisible: Boolean
        get() = shouldShowNavigationProvider?.invoke() ?: true

    val pendingEditResult: String?
        get() = pendingEditResultProvider?.invoke()

    val gammaViewModelResult: String?
        get() = gammaViewModelResultProvider?.invoke()

    companion object {
        private const val TAG = "RecipeAppStateHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeAppStateHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

private const val EDIT_RESULT_KEY = "edit_result"

private val TAB_ICONS = mapOf(
    TopLevelDestination.ALPHA to Icons.Default.Home,
    TopLevelDestination.BETA to Icons.Default.Favorite,
    TopLevelDestination.GAMMA to Icons.Default.Star,
)

@Composable
private fun AppStateContent(host: RecipeAppStateHostActivity, onExit: () -> Unit) {
    val appState = rememberAppState()
    val resultStore = rememberResultStore()
    var latestGammaViewModelResult by remember { mutableStateOf<String?>(null) }

    host.selectTopLevelAction = { destination -> appState.onSelectTopLevelDestination(destination) }
    host.navigateAction = { route -> appState.navigate(route) }
    host.backAction = { appState.onBack(onExit) }
    host.submitAlphaEditResultAction = { result ->
        resultStore.setResult(EDIT_RESULT_KEY, result)
        appState.onBack(onExit)
    }
    host.currentTopLevelProvider = { appState.currentTopLevelDestination }
    host.currentRouteProvider = {
        appState.navigationState.backStacks[appState.navigationState.topLevelRoute]?.lastOrNull()
    }
    host.currentStackDepthProvider = {
        appState.navigationState.backStacks[appState.navigationState.topLevelRoute]?.size ?: 0
    }
    host.shouldShowNavigationProvider = { appState.shouldShowNavigation }
    host.pendingEditResultProvider = { resultStore.getResultState<String?>(EDIT_RESULT_KEY) }
    host.gammaViewModelResultProvider = { latestGammaViewModelResult }

    val entryProvider = entryProvider {
        // Tab Alpha
        entry<TabAlpha> {
            TabAlphaScreen(
                onDetail = { appState.navigate(TabAlphaDetail(from = "Alpha")) },
            )
        }
        entry<TabAlphaDetail> { key ->
            val editResult = resultStore.getResultState<String?>(EDIT_RESULT_KEY)
            var consumedResult by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(editResult) {
                if (editResult != null) {
                    consumedResult = editResult
                    resultStore.removeResult<String>(EDIT_RESULT_KEY)
                }
            }

            TabAlphaDetailScreen(
                from = key.from,
                onEdit = { appState.navigate(TabAlphaEdit(from = consumedResult ?: key.from)) },
                result = consumedResult,
            )
        }
        entry<TabAlphaEdit> { key ->
            TabAlphaEditScreen(
                from = key.from,
                onDone = { result ->
                    resultStore.setResult(EDIT_RESULT_KEY, result)
                    appState.onBack {}
                },
            )
        }

        // Tab Beta
        entry<TabBeta> {
            TabBetaScreen(
                onDetail = { appState.navigate(TabBetaDetail) },
            )
        }
        entry<TabBetaDetail> {
            TabBetaDetailScreen()
        }

        // Tab Gamma
        entry<TabGamma> {
            TabGammaScreen(
                onDetail = { appState.navigate(TabGammaDetail(result = "test")) },
            )
        }
        entry<TabGammaDetail> { key ->
            val vm = viewModel<RecipeViewModel> { RecipeViewModel(key) }
            latestGammaViewModelResult = vm.result
            TabGammaDetailScreen(result = vm.result)
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = appState.shouldShowNavigation,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar {
                    appState.topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = destination == appState.currentTopLevelDestination,
                            onClick = { appState.onSelectTopLevelDestination(destination) },
                            icon = {
                                Icon(
                                    imageVector = TAB_ICONS[destination] ?: Icons.Default.Home,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        val decoratedEntries = appState.navigationState.backStacks.mapValues { (_, stack) ->
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator(),
            )
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = entryProvider,
            )
        }

        val currentTopLevel = appState.navigationState.topLevelRoute
        val startRoute = appState.navigationState.startRoute
        val topLevelRoutesInUse = if (currentTopLevel == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, currentTopLevel)
        }
        val entries = topLevelRoutesInUse.flatMap { decoratedEntries[it] ?: emptyList() }

        Box(Modifier.padding(paddingValues).fillMaxSize()) {
            NavDisplay(
                entries = entries,
                onBack = { appState.onBack(onExit) },
                transitionSpec = DefaultTransitions.crossFade(),
                popTransitionSpec = DefaultTransitions.crossFadeBack(),
                predictivePopTransitionSpec = DefaultTransitions.predictiveCrossFadeBack(),
            )
            NavStateIndicator(
                backStackSize = entries.size,
                currentRoute = appState.currentTopLevelDestination.label,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}
