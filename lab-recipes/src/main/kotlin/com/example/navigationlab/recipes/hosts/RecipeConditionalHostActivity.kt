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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T3: Recipe Conditional - $caseCode"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                when (caseCode) {
                    "R18" -> ConditionalContent()
                    "R19" -> AdvancedDeepLinkContent()
                }
            }
        }
    }

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
private fun ConditionalContent() {
    val backStack = rememberNavBackStack(GateHome)
    val navigator = remember { ConditionalNavigator(backStack) }

    Scaffold { paddingValues ->
        Box(Modifier.padding(paddingValues).fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.goBack() },
                transitionSpec = DefaultTransitions.slideForward(),
                popTransitionSpec = DefaultTransitions.slideBack(),
                predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
                entryProvider = entryProvider {
                    entry<GateHome> {
                        GateHomeScreen(
                            onProfile = { navigator.navigate(GateProfile) },
                        )
                    }
                    entry<GateProfile> {
                        GateProfileScreen(
                            onLogout = { navigator.onLogout() },
                        )
                    }
                    entry<GateLogin> {
                        GateLoginScreen(
                            onLogin = { navigator.onLoginSuccess() },
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
private fun AdvancedDeepLinkContent() {
    val backStack = rememberNavBackStack(AdvancedDeepHome)
    val context = LocalContext.current

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
            // Synthetic backstack: home is already the start, add target on top
            backStack.add(AdvancedDeepTarget(name = name, location = location))
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
                onBack = { backStack.removeLastOrNull() },
                transitionSpec = DefaultTransitions.slideForward(),
                popTransitionSpec = DefaultTransitions.slideBack(),
                predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
                entryProvider = entryProvider {
                    entry<AdvancedDeepHome> {
                        AdvancedDeepHomeScreen(
                            onNavigate = {
                                backStack.add(AdvancedDeepTarget(name = "Manual", location = "Local"))
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
