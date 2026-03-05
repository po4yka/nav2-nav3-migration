package com.example.navigationlab.recipes.helpers

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene

/**
 * Reusable transition specs for NavDisplay.
 * Extracted from RecipeTransitionHostActivity (R14) for consistency across all hosts.
 */
object DefaultTransitions {

    /** Horizontal slide-in from right (forward navigation). */
    fun <T : Any> slideForward(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
        slideInHorizontally(tween(300)) { it } togetherWith
            slideOutHorizontally(tween(300)) { -it }
    }

    /** Horizontal slide-in from left (pop/back navigation). */
    fun <T : Any> slideBack(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
        slideInHorizontally(tween(300)) { -it } togetherWith
            slideOutHorizontally(tween(300)) { it }
    }

    /** Predictive back slide (interactive gesture). */
    fun <T : Any> predictiveSlideBack(): AnimatedContentTransitionScope<Scene<T>>.(Int) -> ContentTransform = { _ ->
        slideInHorizontally(tween(300)) { fullWidth -> -fullWidth } togetherWith
            slideOutHorizontally(tween(300)) { fullWidth -> fullWidth }
    }

    /** Cross-fade for lateral/tab switches. */
    fun <T : Any> crossFade(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
        fadeIn(tween(250)) togetherWith fadeOut(tween(250))
    }

    /** Cross-fade for pop during lateral/tab switches. */
    fun <T : Any> crossFadeBack(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
        fadeIn(tween(250)) togetherWith fadeOut(tween(250))
    }

    /** Predictive cross-fade back. */
    fun <T : Any> predictiveCrossFadeBack(): AnimatedContentTransitionScope<Scene<T>>.(Int) -> ContentTransform = { _ ->
        fadeIn(tween(250)) togetherWith fadeOut(tween(250))
    }
}
