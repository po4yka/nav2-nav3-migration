package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
import com.example.navigationlab.contracts.parseRunModeOrDefault
import com.example.navigationlab.recipes.R
import com.example.navigationlab.recipes.helpers.DefaultTransitions
import com.example.navigationlab.recipes.helpers.MyCustomFragment
import com.example.navigationlab.recipes.keys.InteropFragmentRoute
import com.example.navigationlab.recipes.keys.InteropViewRoute

/**
 * Host for R04 (Interop): AndroidFragment + AndroidView within Nav3 NavDisplay.
 * Requires FragmentActivity as base class for AndroidFragment<T> support.
 */
class RecipeInteropHostActivity : FragmentActivity() {

    private var navigateToViewAction: (() -> Unit)? = null
    private var popBackAction: (() -> Boolean)? = null
    private var currentRouteProvider: (() -> Any?)? = null
    private var backStackDepthProvider: (() -> Int)? = null

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
            getString(R.string.topology_recipe_interop),
            caseCode,
            runMode,
        )

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                val backStack = rememberNavBackStack(InteropFragmentRoute)
                navigateToViewAction = {
                    backStack.add(InteropViewRoute("123"))
                    NavLogger.push("RecipeInteropHost", "InteropViewRoute", backStack.size)
                }
                popBackAction = {
                    if (backStack.size > 1) {
                        val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                        backStack.removeLastOrNull()
                        NavLogger.back("RecipeInteropHost", from, backStack.size)
                        true
                    } else {
                        false
                    }
                }
                currentRouteProvider = { backStack.lastOrNull() }
                backStackDepthProvider = { backStack.size }

                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        if (backStack.size > 1) {
                            val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                            backStack.removeLastOrNull()
                            NavLogger.back("RecipeInteropHost", from, backStack.size)
                        } else {
                            finish()
                        }
                    },
                    transitionSpec = DefaultTransitions.slideForward(),
                    popTransitionSpec = DefaultTransitions.slideBack(),
                    predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
                    entryProvider = entryProvider {
                        entry<InteropFragmentRoute> {
                            Column(Modifier.fillMaxSize().wrapContentSize()) {
                                AndroidFragment<MyCustomFragment>()
                                Button(onClick = dropUnlessResumed {
                                    navigateToViewAction?.invoke()
                                }) {
                                    Text(stringResource(R.string.interop_go_to_view))
                                }
                            }
                        }
                        entry<InteropViewRoute> { key ->
                            AndroidView(
                                modifier = Modifier.fillMaxSize().wrapContentSize(),
                                factory = { context ->
                                    TextView(context).apply {
                                        text = context.getString(R.string.interop_view_with_key, key.id)
                                    }
                                },
                            )
                        }
                    },
                )
            }
        }
    }

    fun navigateToView() {
        navigateToViewAction?.invoke()
    }

    fun popBack(): Boolean = popBackAction?.invoke() ?: false

    val backStackDepth: Int
        get() = backStackDepthProvider?.invoke() ?: 0

    val currentRouteName: String?
        get() = currentRouteProvider?.invoke()?.let { it::class.simpleName }

    val currentViewRouteId: String?
        get() = (currentRouteProvider?.invoke() as? InteropViewRoute)?.id

    companion object {
        private const val TAG = "RecipeInteropHost"
        const val EXTRA_CASE_ID = "case_id"
        const val EXTRA_RUN_MODE = "run_mode"

        fun createIntent(context: Context, caseId: LabCaseId, runMode: String): Intent =
            Intent(context, RecipeInteropHostActivity::class.java).apply {
                putExtra(EXTRA_CASE_ID, caseId.code)
                putExtra(EXTRA_RUN_MODE, runMode)
            }
    }
}
