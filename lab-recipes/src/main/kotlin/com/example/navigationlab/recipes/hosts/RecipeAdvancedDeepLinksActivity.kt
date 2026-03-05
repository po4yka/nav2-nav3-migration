package com.example.navigationlab.recipes.hosts

import android.content.Intent
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
            startActivity(
                RecipeConditionalHostActivity.createAdvancedDeepIntent(
                    context = this,
                    name = name,
                    location = location,
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
        }
    }

    companion object {
        private const val CUSTOM_URI_SCHEME = "recipes"
        private const val AUTHORITY_ADVANCED = "advanced"
        private const val PARAM_NAME = "name"
        private const val PARAM_LOCATION = "location"
    }
}
