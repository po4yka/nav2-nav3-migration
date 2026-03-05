package com.example.navigationlab.recipes.helpers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.navigationlab.recipes.keys.GateLogin
import com.example.navigationlab.recipes.keys.GateProfile

/**
 * Navigator with login-gate conditional check.
 * When navigating to a key that requires login and the user is not logged in,
 * redirects to GateLogin with the original target preserved.
 */
class ConditionalNavigator(
    private val backStack: NavBackStack<NavKey>,
) {
    var isLoggedIn by mutableStateOf(false)

    fun navigate(key: NavKey) {
        if (requiresLogin(key) && !isLoggedIn) {
            val redirectCode = when (key) {
                is GateProfile -> "GateProfile"
                else -> null
            }
            backStack.add(GateLogin(redirectToCode = redirectCode))
        } else {
            backStack.add(key)
        }
    }

    fun onLoginSuccess() {
        // Remove login entry from back stack
        val lastEntry = backStack.lastOrNull()
        val redirectCode = if (lastEntry is GateLogin) lastEntry.redirectToCode else null
        backStack.removeLastOrNull()

        isLoggedIn = true

        // Navigate to the original target
        val target = resolveRedirect(redirectCode)
        if (target != null) {
            backStack.add(target)
        }
    }

    fun onLogout() {
        isLoggedIn = false
        // Clear to home
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }

    private fun requiresLogin(key: NavKey): Boolean = when (key) {
        is GateProfile -> GateProfile.requiresLogin
        else -> false
    }

    private fun resolveRedirect(code: String?): NavKey? = when (code) {
        "GateProfile" -> GateProfile
        else -> null
    }
}
