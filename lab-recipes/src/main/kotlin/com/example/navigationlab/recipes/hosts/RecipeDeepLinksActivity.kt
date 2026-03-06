package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Trampoline activity for R13: Deep link bridging.
 * Parses custom URI scheme, creates intent for RecipeDeepLinkHostActivity, finishes self.
 *
 * URI format: recipes://target?param=X
 */
class RecipeDeepLinksActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: return

        if (uri.scheme == CUSTOM_URI_SCHEME && uri.authority == AUTHORITY_TARGET) {
            val param = uri.getQueryParameter(PARAM_KEY) ?: "default"
            val runMode = intent.getStringExtra(RecipeDeepLinkHostActivity.EXTRA_RUN_MODE)
            startActivity(
                RecipeDeepLinkHostActivity.createDeepLinkIntent(
                    context = this,
                    param = param,
                    runMode = runMode,
                ),
            )
        }
    }

    companion object {
        private const val CUSTOM_URI_SCHEME = "recipes"
        private const val AUTHORITY_TARGET = "target"
        private const val PARAM_KEY = "param"
        const val DEFAULT_IN_APP_PARAM = "hello"

        /** Explicit in-app launcher path for R13 that still exercises trampoline parsing. */
        fun createInAppIntent(context: Context, param: String, runMode: String): Intent =
            Intent(context, RecipeDeepLinksActivity::class.java).apply {
                data = Uri.Builder()
                    .scheme(CUSTOM_URI_SCHEME)
                    .authority(AUTHORITY_TARGET)
                    .appendQueryParameter(PARAM_KEY, param)
                    .build()
                putExtra(RecipeDeepLinkHostActivity.EXTRA_RUN_MODE, runMode)
            }
    }
}
