package com.example.navigationlab.recipes.hosts

import android.content.Intent
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
            startActivity(
                RecipeDeepLinkHostActivity.createDeepLinkIntent(
                    context = this,
                    param = param,
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
            )
        }
    }

    companion object {
        private const val CUSTOM_URI_SCHEME = "recipes"
        private const val AUTHORITY_TARGET = "target"
        private const val PARAM_KEY = "param"
    }
}
