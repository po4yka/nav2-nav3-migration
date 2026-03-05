package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.content.DialogContent
import com.example.navigationlab.recipes.content.SheetContent
import com.example.navigationlab.recipes.content.TransitionFadeScreen
import com.example.navigationlab.recipes.content.TransitionHomeScreen
import com.example.navigationlab.recipes.content.TransitionSlideScreen
import com.example.navigationlab.recipes.helpers.BottomSheetSceneStrategy
import com.example.navigationlab.recipes.helpers.DefaultTransitions
import com.example.navigationlab.recipes.helpers.NavStateIndicator
import com.example.navigationlab.recipes.keys.DialogRoute
import com.example.navigationlab.recipes.keys.SheetRoute
import com.example.navigationlab.recipes.keys.TransitionFade
import com.example.navigationlab.recipes.keys.TransitionHome
import com.example.navigationlab.recipes.keys.TransitionSlide

/**
 * Host for R14 (Custom transitions), R15 (Dialog destination), R16 (Bottom sheet destination).
 * Demonstrates transitionSpec, DialogSceneStrategy, and custom BottomSheetSceneStrategy.
 */
class RecipeTransitionHostActivity : AppCompatActivity() {

    internal var openSlideAction: (() -> Unit)? = null
    internal var openFadeAction: (() -> Unit)? = null
    internal var openDialogAction: (() -> Unit)? = null
    internal var openSheetAction: (() -> Unit)? = null
    internal var popBackAction: (() -> Boolean)? = null
    internal var currentRouteProvider: (() -> Any?)? = null
    internal var backStackDepthProvider: (() -> Int)? = null

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
            getString(R.string.topology_recipe_transition),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                TransitionContent(
                    host = this@RecipeTransitionHostActivity,
                    onExit = { finish() },
                )
            }
        }
    }

    fun openSlideTransition() {
        openSlideAction?.invoke()
    }

    fun openFadeTransition() {
        openFadeAction?.invoke()
    }

    fun openDialog() {
        openDialogAction?.invoke()
    }

    fun openBottomSheet() {
        openSheetAction?.invoke()
    }

    fun popBack(): Boolean = popBackAction?.invoke() ?: false

    val currentRouteName: String?
        get() = currentRouteProvider?.invoke()?.let { it::class.simpleName }

    val backStackDepth: Int
        get() = backStackDepthProvider?.invoke() ?: 0

    val isDialogVisible: Boolean
        get() = currentRouteProvider?.invoke() is DialogRoute

    val isBottomSheetVisible: Boolean
        get() = currentRouteProvider?.invoke() is SheetRoute

    companion object {
        private const val TAG = "RecipeTransitionHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeTransitionHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}

@Composable
private fun TransitionContent(host: RecipeTransitionHostActivity, onExit: () -> Unit) {
    val backStack = rememberNavBackStack(TransitionHome)
    host.openSlideAction = {
        backStack.add(TransitionSlide("slide-1"))
        NavLogger.push("RecipeTransitionHost", "TransitionSlide", backStack.size)
    }
    host.openFadeAction = {
        backStack.add(TransitionFade("fade-1"))
        NavLogger.push("RecipeTransitionHost", "TransitionFade", backStack.size)
    }
    host.openDialogAction = {
        backStack.add(DialogRoute("Hello from dialog!"))
        NavLogger.push("RecipeTransitionHost", "DialogRoute", backStack.size)
    }
    host.openSheetAction = {
        backStack.add(SheetRoute("My Sheet"))
        NavLogger.push("RecipeTransitionHost", "SheetRoute", backStack.size)
    }
    host.popBackAction = {
        if (backStack.size > 1) {
            val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
            backStack.removeLastOrNull()
            NavLogger.back("RecipeTransitionHost", from, backStack.size)
            true
        } else {
            false
        }
    }
    host.currentRouteProvider = { backStack.lastOrNull() }
    host.backStackDepthProvider = { backStack.size }

    // Scene strategies
    val bottomSheetStrategy = BottomSheetSceneStrategy()
    val dialogStrategy = DialogSceneStrategy<NavKey>()

    Scaffold { paddingValues ->
        Box(Modifier.padding(paddingValues).fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                    backStack.removeLastOrNull()
                    NavLogger.back("RecipeTransitionHost", from, backStack.size)
                } else {
                    onExit()
                }
            },
            transitionSpec = DefaultTransitions.slideForward(),
            popTransitionSpec = DefaultTransitions.slideBack(),
            predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
            sceneStrategy = bottomSheetStrategy then dialogStrategy,
            entryProvider = entryProvider {
                entry<TransitionHome> {
                    TransitionHomeScreen(
                        onSlide = { host.openSlideAction?.invoke() },
                        onFade = { host.openFadeAction?.invoke() },
                        onDialog = { host.openDialogAction?.invoke() },
                        onSheet = { host.openSheetAction?.invoke() },
                    )
                }
                entry<TransitionSlide> { key ->
                    TransitionSlideScreen(
                        label = key.label,
                        onNext = {
                            backStack.add(TransitionFade("fade-from-slide"))
                            NavLogger.push("RecipeTransitionHost", "TransitionFade", backStack.size)
                        },
                    )
                }
                entry<TransitionFade>(
                    // Per-entry metadata override: fade transition instead of slide
                    metadata = NavDisplay.transitionSpec {
                        fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                    } + NavDisplay.popTransitionSpec {
                        fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                    } + NavDisplay.predictivePopTransitionSpec { _ ->
                        fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                    },
                ) { key ->
                    TransitionFadeScreen(label = key.label)
                }
                entry<DialogRoute>(
                    metadata = DialogSceneStrategy.dialog(),
                ) { key ->
                    DialogContent(
                        message = key.message,
                        onDismiss = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            } else {
                                onExit()
                            }
                        },
                    )
                }
                entry<SheetRoute>(
                    metadata = BottomSheetSceneStrategy.bottomSheet(),
                ) { key ->
                    SheetContent(
                        title = key.title,
                        onDismiss = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            } else {
                                onExit()
                            }
                        },
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
