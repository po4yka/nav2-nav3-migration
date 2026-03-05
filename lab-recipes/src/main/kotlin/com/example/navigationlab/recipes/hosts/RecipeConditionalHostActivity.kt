package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.AdvancedDeepHomeScreen
import com.example.navigationlab.recipes.content.AdvancedDeepTargetScreen
import com.example.navigationlab.recipes.content.GateHomeScreen
import com.example.navigationlab.recipes.content.GateLoginScreen
import com.example.navigationlab.recipes.content.GateProfileScreen
import com.example.navigationlab.recipes.helpers.ConditionalNavigator
import com.example.navigationlab.recipes.helpers.DefaultTransitions
import com.example.navigationlab.recipes.helpers.NavStateIndicator
import com.example.navigationlab.recipes.keys.AdvancedDeepHome
import com.example.navigationlab.recipes.keys.AdvancedDeepTarget
import com.example.navigationlab.recipes.keys.GateHome
import com.example.navigationlab.recipes.keys.GateLogin
import com.example.navigationlab.recipes.keys.GateProfile

/**
 * Host for R18 (Conditional navigation / auth gate) and R19 (Advanced deep links with synthetic backstack).
 */
class RecipeConditionalHostActivity : AppCompatActivity() {

    internal var navigateProfileAction: (() -> Unit)? = null
    internal var loginAction: (() -> Unit)? = null
    internal var logoutAction: (() -> Unit)? = null
    internal var navigateAdvancedTargetAction: (() -> Unit)? = null
    internal var popBackAction: (() -> Boolean)? = null
    internal var currentRouteProvider: (() -> Any?)? = null
    internal var backStackDepthProvider: (() -> Int)? = null
    internal var loggedInProvider: (() -> Boolean)? = null
    internal var advancedNameProvider: (() -> String?)? = null
    internal var advancedLocationProvider: (() -> String?)? = null

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
            getString(R.string.topology_recipe_conditional),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                when (caseCode) {
                    "R18" -> ConditionalContent(
                        host = this@RecipeConditionalHostActivity,
                        onExit = { finish() },
                    )
                    "R19" -> AdvancedDeepLinkContent(
                        host = this@RecipeConditionalHostActivity,
                        onExit = { finish() },
                    )
                }
            }
        }
    }

    fun navigateToProfile() {
        navigateProfileAction?.invoke()
    }

    fun login() {
        loginAction?.invoke()
    }

    fun logout() {
        logoutAction?.invoke()
    }

    fun navigateToAdvancedTarget() {
        navigateAdvancedTargetAction?.invoke()
    }

    fun popBack(): Boolean = popBackAction?.invoke() ?: false

    val currentRouteName: String?
        get() = currentRouteProvider?.invoke()?.let { it::class.simpleName }

    val backStackDepth: Int
        get() = backStackDepthProvider?.invoke() ?: 0

    val isLoggedIn: Boolean
        get() = loggedInProvider?.invoke() ?: false

    val advancedTargetName: String?
        get() = advancedNameProvider?.invoke()

    val advancedTargetLocation: String?
        get() = advancedLocationProvider?.invoke()

    companion object {
        private const val TAG = "RecipeConditionalHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"
        const val ACTION_ADVANCED_DEEP = "com.example.navigationlab.recipes.action.ADVANCED_DEEP"
        const val KEY_NAME = "advanced_deep_name"
        const val KEY_LOCATION = "advanced_deep_location"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeConditionalHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }

        fun createAdvancedDeepIntent(context: Context, name: String, location: String): Intent =
            Intent(context, RecipeConditionalHostActivity::class.java).apply {
                action = ACTION_ADVANCED_DEEP
                putExtra(KEY_NAME, name)
                putExtra(KEY_LOCATION, location)
                putExtra(EXTRA_CASE_ID, "R19")
            }
    }
}

@Composable
private fun ConditionalContent(host: RecipeConditionalHostActivity, onExit: () -> Unit) {
    val backStack = rememberNavBackStack(GateHome)
    val navigator = remember { ConditionalNavigator(backStack) }
    host.navigateProfileAction = { navigator.navigate(GateProfile) }
    host.loginAction = { navigator.onLoginSuccess() }
    host.logoutAction = { navigator.onLogout() }
    host.navigateAdvancedTargetAction = null
    host.popBackAction = {
        if (backStack.size > 1) {
            navigator.goBack(onAtRoot = onExit)
            true
        } else {
            false
        }
    }
    host.currentRouteProvider = { backStack.lastOrNull() }
    host.backStackDepthProvider = { backStack.size }
    host.loggedInProvider = { navigator.isLoggedIn }
    host.advancedNameProvider = null
    host.advancedLocationProvider = null

    Scaffold { paddingValues ->
        Box(Modifier.padding(paddingValues).fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.goBack(onAtRoot = onExit) },
                transitionSpec = DefaultTransitions.slideForward(),
                popTransitionSpec = DefaultTransitions.slideBack(),
                predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
                entryProvider = entryProvider {
                    entry<GateHome> {
                        GateHomeScreen(
                            onProfile = { host.navigateProfileAction?.invoke() },
                        )
                    }
                    entry<GateProfile> {
                        GateProfileScreen(
                            onLogout = { navigator.onLogout() },
                        )
                    }
                    entry<GateLogin> {
                        GateLoginScreen(
                            onLogin = { host.loginAction?.invoke() },
                        )
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

@Composable
private fun AdvancedDeepLinkContent(host: RecipeConditionalHostActivity, onExit: () -> Unit) {
    val backStack = rememberNavBackStack(AdvancedDeepHome)
    val context = LocalContext.current
    var latestName by rememberSaveable { mutableStateOf<String?>(null) }
    var latestLocation by rememberSaveable { mutableStateOf<String?>(null) }

    host.navigateProfileAction = null
    host.loginAction = null
    host.logoutAction = null
    host.navigateAdvancedTargetAction = {
        val name = "Manual"
        val location = "Local"
        backStack.add(AdvancedDeepTarget(name = name, location = location))
        latestName = name
        latestLocation = location
        NavLogger.push("RecipeConditionalHost", "AdvancedDeepTarget", backStack.size)
    }
    host.popBackAction = {
        if (backStack.size > 1) {
            val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
            backStack.removeLastOrNull()
            NavLogger.back("RecipeConditionalHost", from, backStack.size)
            true
        } else {
            false
        }
    }
    host.currentRouteProvider = { backStack.lastOrNull() }
    host.backStackDepthProvider = { backStack.size }
    host.loggedInProvider = null
    host.advancedNameProvider = {
        (backStack.lastOrNull() as? AdvancedDeepTarget)?.name ?: latestName
    }
    host.advancedLocationProvider = {
        (backStack.lastOrNull() as? AdvancedDeepTarget)?.location ?: latestLocation
    }

    // Handle advanced deep link intent once
    var isDeepLinkConsumed by rememberSaveable { mutableStateOf(false) }
    val isDeepLink = (context as? RecipeConditionalHostActivity)?.intent?.action ==
        RecipeConditionalHostActivity.ACTION_ADVANCED_DEEP
    var isProcessing by rememberSaveable { mutableStateOf(isDeepLink && !isDeepLinkConsumed) }

    LaunchedEffect(context) {
        if (isDeepLinkConsumed) return@LaunchedEffect
        val activity = context as? RecipeConditionalHostActivity ?: return@LaunchedEffect
        val intent = activity.intent ?: return@LaunchedEffect

        if (intent.action == RecipeConditionalHostActivity.ACTION_ADVANCED_DEEP) {
            val name = intent.getStringExtra(RecipeConditionalHostActivity.KEY_NAME) ?: return@LaunchedEffect
            val location = intent.getStringExtra(RecipeConditionalHostActivity.KEY_LOCATION) ?: return@LaunchedEffect
            NavLogger.deepLink("RecipeConditionalHost", RecipeConditionalHostActivity.ACTION_ADVANCED_DEEP, mapOf("name" to name, "location" to location))
            // Synthetic backstack: home is already the start, add target on top
            backStack.add(AdvancedDeepTarget(name = name, location = location))
            latestName = name
            latestLocation = location
            NavLogger.push("RecipeConditionalHost", "AdvancedDeepTarget", backStack.size)
            isDeepLinkConsumed = true
        }
        isProcessing = false
    }

    Scaffold { paddingValues ->
        if (isProcessing) {
            Box(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(paddingValues),
                onBack = {
                    if (backStack.size > 1) {
                        val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                        backStack.removeLastOrNull()
                        NavLogger.back("RecipeConditionalHost", from, backStack.size)
                    } else {
                        onExit()
                    }
                },
                transitionSpec = DefaultTransitions.slideForward(),
                popTransitionSpec = DefaultTransitions.slideBack(),
                predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
                entryProvider = entryProvider {
                    entry<AdvancedDeepHome> {
                        AdvancedDeepHomeScreen(
                            onNavigate = {
                                host.navigateAdvancedTargetAction?.invoke()
                            },
                        )
                    }
                    entry<AdvancedDeepTarget> { key ->
                        AdvancedDeepTargetScreen(name = key.name, location = key.location)
                    }
                },
            )
        }
    }
}
