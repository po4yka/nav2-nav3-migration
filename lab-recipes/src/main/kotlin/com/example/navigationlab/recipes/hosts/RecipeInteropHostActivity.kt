package com.example.navigationlab.recipes.hosts

import android.annotation.SuppressLint
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.NavLogger
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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_host)

        val caseCode = intent.getStringExtra(EXTRA_CASE_ID) ?: run {
            Log.e(TAG, "No case ID provided")
            finish()
            return
        }

        findViewById<TextView>(R.id.tvTopologyLabel).text = "T5: Recipe Interop - $caseCode"

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                val backStack = rememberNavBackStack(InteropFragmentRoute)

                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        val from = backStack.lastOrNull()?.let { it::class.simpleName } ?: "?"
                        backStack.removeLastOrNull()
                        NavLogger.back("RecipeInteropHost", from, backStack.size)
                    },
                    transitionSpec = DefaultTransitions.slideForward(),
                    popTransitionSpec = DefaultTransitions.slideBack(),
                    predictivePopTransitionSpec = DefaultTransitions.predictiveSlideBack(),
                    entryProvider = entryProvider {
                        entry<InteropFragmentRoute> {
                            Column(Modifier.fillMaxSize().wrapContentSize()) {
                                AndroidFragment<MyCustomFragment>()
                                Button(onClick = dropUnlessResumed {
                                    backStack.add(InteropViewRoute("123"))
                                    NavLogger.push("RecipeInteropHost", "InteropViewRoute", backStack.size)
                                }) {
                                    Text("Go to View")
                                }
                            }
                        }
                        entry<InteropViewRoute> { key ->
                            AndroidView(
                                modifier = Modifier.fillMaxSize().wrapContentSize(),
                                factory = { context ->
                                    TextView(context).apply {
                                        text = "My View with key: ${key.id}"
                                    }
                                },
                            )
                        }
                    },
                )
            }
        }
    }

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
