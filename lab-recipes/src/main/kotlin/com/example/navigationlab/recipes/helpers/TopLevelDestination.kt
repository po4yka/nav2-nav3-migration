package com.example.navigationlab.recipes.helpers

import androidx.navigation3.runtime.NavKey
import com.example.navigationlab.recipes.keys.TabAlpha
import com.example.navigationlab.recipes.keys.TabBeta
import com.example.navigationlab.recipes.keys.TabGamma

internal enum class TopLevelDestination(
    val label: String,
    val startRoute: NavKey,
) {
    UNKNOWN(label = "Unknown", startRoute = TabAlpha),
    ALPHA(label = "Alpha", startRoute = TabAlpha),
    BETA(label = "Beta", startRoute = TabBeta),
    GAMMA(label = "Gamma", startRoute = TabGamma);

    companion object {
        fun getAvailableDestinations(): List<TopLevelDestination> =
            entries.filter { it != UNKNOWN }

        fun fromNavKey(navKey: NavKey): TopLevelDestination =
            entries.find { it.startRoute == navKey } ?: UNKNOWN
    }
}
