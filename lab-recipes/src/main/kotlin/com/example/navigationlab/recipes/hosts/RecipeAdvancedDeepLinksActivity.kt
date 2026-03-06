package com.example.navigationlab.recipes.hosts

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Trampoline activity for R19: Advanced deep links with synthetic backstack.
 * Parses custom URI scheme, creates intent for RecipeConditionalHostActivity, finishes self.
 *
 * URI format: recipes://advanced?name=X&location=Y
 */
class RecipeAdvancedDeepLinksActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: return

        if (uri.scheme == CUSTOM_URI_SCHEME && uri.authority == AUTHORITY_ADVANCED) {
            val name = uri.getQueryParameter(PARAM_NAME) ?: "default"
            val location = uri.getQueryParameter(PARAM_LOCATION) ?: "unknown"
            val runMode = intent.getStringExtra(RecipeConditionalHostActivity.EXTRA_RUN_MODE)
            startActivity(
                RecipeConditionalHostActivity.createAdvancedDeepIntent(
                    context = this,
                    name = name,
                    location = location,
                    runMode = runMode,
                ),
            )
        }
    }

    companion object {
        private const val CUSTOM_URI_SCHEME = "recipes"
        private const val AUTHORITY_ADVANCED = "advanced"
        private const val PARAM_NAME = "name"
        private const val PARAM_LOCATION = "location"
        const val DEFAULT_IN_APP_NAME = "Alice"
        const val DEFAULT_IN_APP_LOCATION = "NYC"

        /** Explicit in-app launcher path for R19 that still exercises trampoline parsing. */
        fun createInAppIntent(context: Context, name: String, location: String, runMode: String): Intent =
            Intent(context, RecipeAdvancedDeepLinksActivity::class.java).apply {
                data = Uri.Builder()
                    .scheme(CUSTOM_URI_SCHEME)
                    .authority(AUTHORITY_ADVANCED)
                    .appendQueryParameter(PARAM_NAME, name)
                    .appendQueryParameter(PARAM_LOCATION, location)
                    .build()
                putExtra(RecipeConditionalHostActivity.EXTRA_RUN_MODE, runMode)
            }
    }
}
