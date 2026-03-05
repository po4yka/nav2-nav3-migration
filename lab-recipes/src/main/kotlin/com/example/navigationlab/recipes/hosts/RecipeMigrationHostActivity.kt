package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.MigScreenA
import com.example.navigationlab.recipes.content.MigScreenA1
import com.example.navigationlab.recipes.content.MigScreenB
import com.example.navigationlab.recipes.content.MigScreenB1
import com.example.navigationlab.recipes.content.MigScreenC
import com.example.navigationlab.recipes.helpers.DefaultTransitions
import com.example.navigationlab.recipes.helpers.NavigationState
import com.example.navigationlab.recipes.helpers.Navigator
import com.example.navigationlab.recipes.helpers.rememberNavigationState
import com.example.navigationlab.recipes.keys.MIG_BEGIN_TOP_LEVEL_ROUTES
import com.example.navigationlab.recipes.keys.MIG_END_TOP_LEVEL_ROUTES
import com.example.navigationlab.recipes.keys.MigBeginBaseRouteA
import com.example.navigationlab.recipes.keys.MigBeginBaseRouteB
import com.example.navigationlab.recipes.keys.MigBeginBaseRouteC
import com.example.navigationlab.recipes.keys.MigBeginRouteA
import com.example.navigationlab.recipes.keys.MigBeginRouteA1
import com.example.navigationlab.recipes.keys.MigBeginRouteB
import com.example.navigationlab.recipes.keys.MigBeginRouteB1
import com.example.navigationlab.recipes.keys.MigBeginRouteC
import com.example.navigationlab.recipes.keys.MigBeginRouteD
import com.example.navigationlab.recipes.keys.MigEndRouteA
import com.example.navigationlab.recipes.keys.MigEndRouteA1
import com.example.navigationlab.recipes.keys.MigEndRouteB
import com.example.navigationlab.recipes.keys.MigEndRouteB1
import com.example.navigationlab.recipes.keys.MigEndRouteC
import com.example.navigationlab.recipes.keys.MigEndRouteD
import kotlin.reflect.KClass

/**
 * Host for R05 (Migration Begin / Nav2) and R06 (Migration End / Nav3).
 * R05 uses Nav2 NavHost with bottom navigation.
 * R06 uses Nav3 NavDisplay + NavigationState + Navigator with bottom navigation.
 */
class RecipeMigrationHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }
        val runMode = parseRunModeOrDefault(intent.getStringExtra(EXTRA_RUN_MODE))

        val topology = if (caseCode == "R05") "T2" else "T3"
        findViewById<TextView>(R.id.tvTopologyLabel).text = "$topology: Recipe Migration - $caseCode [$runMode]"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                when (caseCode) {
                    "R05" -> Nav2MigrationBegin()
                    "R06" -> Nav3MigrationEnd(onExit = { finish() })
                }
            }
        }
    }

    companion object {
        private const val TAG = "RecipeMigrationHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeMigrationHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

// -- R05: Nav2 Migration Begin --

@androidx.compose.runtime.Composable
private fun Nav2MigrationBegin() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(bottomBar = {
        NavigationBar {
            MIG_BEGIN_TOP_LEVEL_ROUTES.forEach { (key, value) ->
                val isSelected =
                    currentBackStackEntry?.destination.isRouteInHierarchy(key::class)
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(key, navOptions {
                            popUpTo(route = MigBeginRouteA)
                        })
                    },
                    icon = {
                        Icon(
                            imageVector = value.icon,
                            contentDescription = value.description,
                        )
                    },
                    label = { Text(value.description) },
                )
            }
        }
    }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MigBeginBaseRouteA,
            modifier = Modifier.padding(paddingValues),
        ) {
            nav2FeatureASection(
                onSubRouteClick = { navController.navigate(MigBeginRouteA1) },
                onDialogClick = { navController.navigate(MigBeginRouteD) },
            )
            nav2FeatureBSection(
                onDetailClick = { id -> navController.navigate(MigBeginRouteB1(id)) },
                onDialogClick = { navController.navigate(MigBeginRouteD) },
            )
            nav2FeatureCSection(
                onDialogClick = { navController.navigate(MigBeginRouteD) },
            )
            dialog<MigBeginRouteD> {
                Text(
                    modifier = Modifier.background(Color.White),
                    text = "Route D title (dialog)",
                )
            }
        }
    }
}

private fun NavGraphBuilder.nav2FeatureASection(
    onSubRouteClick: () -> Unit,
    onDialogClick: () -> Unit,
) {
    navigation<MigBeginBaseRouteA>(startDestination = MigBeginRouteA) {
        composable<MigBeginRouteA> { MigScreenA(onSubRouteClick, onDialogClick) }
        composable<MigBeginRouteA1> { MigScreenA1() }
    }
}

private fun NavGraphBuilder.nav2FeatureBSection(
    onDetailClick: (id: String) -> Unit,
    onDialogClick: () -> Unit,
) {
    navigation<MigBeginBaseRouteB>(startDestination = MigBeginRouteB) {
        composable<MigBeginRouteB> { MigScreenB(onDetailClick, onDialogClick) }
        composable<MigBeginRouteB1> { key ->
            MigScreenB1(id = key.toRoute<MigBeginRouteB1>().id)
        }
    }
}

private fun NavGraphBuilder.nav2FeatureCSection(
    onDialogClick: () -> Unit,
) {
    navigation<MigBeginBaseRouteC>(startDestination = MigBeginRouteC) {
        composable<MigBeginRouteC> { MigScreenC(onDialogClick) }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any { it.hasRoute(route) } ?: false

// -- R06: Nav3 Migration End --

@androidx.compose.runtime.Composable
private fun Nav3MigrationEnd(onExit: () -> Unit) {
    val navigationState = rememberNavigationState(
        startRoute = MigEndRouteA,
        topLevelRoutes = MIG_END_TOP_LEVEL_ROUTES.keys,
    )

    val navigator = remember { Navigator(navigationState) }

    val entryProvider = entryProvider {
        nav3FeatureASection(
            onSubRouteClick = { navigator.navigate(MigEndRouteA1) },
            onDialogClick = { navigator.navigate(MigEndRouteD) },
        )
        nav3FeatureBSection(
            onDetailClick = { id -> navigator.navigate(MigEndRouteB1(id)) },
            onDialogClick = { navigator.navigate(MigEndRouteD) },
        )
        nav3FeatureCSection(
            onDialogClick = { navigator.navigate(MigEndRouteD) },
        )
        entry<MigEndRouteD> {
            Text(
                modifier = Modifier.background(Color.White),
                text = "Route D title (dialog)",
            )
        }
    }

    Scaffold(bottomBar = {
        NavigationBar {
            MIG_END_TOP_LEVEL_ROUTES.forEach { (key, value) ->
                val isSelected = key == navigationState.topLevelRoute
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { navigator.navigate(key) },
                    icon = {
                        Icon(
                            imageVector = value.icon,
                            contentDescription = value.description,
                        )
                    },
                    label = { Text(value.description) },
                )
            }
        }
    }) { paddingValues ->
        NavDisplay(
            entries = navigationState.toDecoratedEntries(entryProvider),
            onBack = { navigator.goBack(onAtRoot = onExit) },
            modifier = Modifier.padding(paddingValues),
            transitionSpec = DefaultTransitions.crossFade(),
            popTransitionSpec = DefaultTransitions.crossFadeBack(),
            predictivePopTransitionSpec = DefaultTransitions.predictiveCrossFadeBack(),
        )
    }
}

private fun EntryProviderScope<NavKey>.nav3FeatureASection(
    onSubRouteClick: () -> Unit,
    onDialogClick: () -> Unit,
) {
    entry<MigEndRouteA> { MigScreenA(onSubRouteClick, onDialogClick) }
    entry<MigEndRouteA1> { MigScreenA1() }
}

private fun EntryProviderScope<NavKey>.nav3FeatureBSection(
    onDetailClick: (id: String) -> Unit,
    onDialogClick: () -> Unit,
) {
    entry<MigEndRouteB> { MigScreenB(onDetailClick, onDialogClick) }
    entry<MigEndRouteB1> { key -> MigScreenB1(id = key.id) }
}

private fun EntryProviderScope<NavKey>.nav3FeatureCSection(
    onDialogClick: () -> Unit,
) {
    entry<MigEndRouteC> { MigScreenC(onDialogClick) }
}
