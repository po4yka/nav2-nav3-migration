package com.example.navigationlab

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.navigationlab.di.labModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NavigationLabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(EdgeToEdgeLifecycleCallbacks())
        startKoin {
            androidContext(this@NavigationLabApp)
            modules(labModule)
        }
    }

    private class EdgeToEdgeLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activity.applyEdgeToEdgeIfNeeded()
        }

        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}

/**
 * Edge-to-edge top inset contract for XML-host activities.
 *
 * When a layout provides a dedicated top header, it must expose that header
 * as `R.id.tvTopologyLabel` so status-bar inset is applied to the header
 * instead of the full content root.
 */
private val TOPOLOGY_LABEL_VIEW_ID = R.id.tvTopologyLabel

private fun Activity.applyEdgeToEdgeIfNeeded() {
    (this as? ComponentActivity)?.enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.auto(
            lightScrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT,
        ),
        navigationBarStyle = SystemBarStyle.auto(
            lightScrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT,
        ),
    ) ?: WindowCompat.setDecorFitsSystemWindows(window, false)

    val root = findContentRoot() ?: return
    val rootPadding = root.capturePadding()
    val topologyLabel = root.findViewById<View?>(TOPOLOGY_LABEL_VIEW_ID)
    val topologyLabelPadding = topologyLabel?.capturePadding()

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        val systemBarInsets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )

        applyInsets(
            root = view,
            rootPadding = rootPadding,
            topInsetTarget = topologyLabel,
            topInsetTargetPadding = topologyLabelPadding,
            systemBarInsets = systemBarInsets,
        )

        windowInsets
    }
    ViewCompat.requestApplyInsets(root)
}

private fun Activity.findContentRoot(): View? {
    val content = findViewById<ViewGroup>(android.R.id.content) ?: return null
    return content.getChildAt(0)
}

private fun View.capturePadding(): ViewPadding = ViewPadding(
    left = paddingLeft,
    top = paddingTop,
    right = paddingRight,
    bottom = paddingBottom,
)

internal fun applyInsets(
    root: View,
    rootPadding: ViewPadding,
    topInsetTarget: View?,
    topInsetTargetPadding: ViewPadding?,
    systemBarInsets: androidx.core.graphics.Insets,
) {
    root.updatePadding(
        left = rootPadding.left + systemBarInsets.left,
        top = if (topInsetTarget == null) rootPadding.top + systemBarInsets.top else rootPadding.top,
        right = rootPadding.right + systemBarInsets.right,
        bottom = rootPadding.bottom + systemBarInsets.bottom,
    )

    if (topInsetTarget != null && topInsetTargetPadding != null) {
        topInsetTarget.updatePadding(top = topInsetTargetPadding.top + systemBarInsets.top)
    }
}

internal data class ViewPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)
