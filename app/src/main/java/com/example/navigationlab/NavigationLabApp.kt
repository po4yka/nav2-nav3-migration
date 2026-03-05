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

private const val TOPOLOGY_LABEL_ID_NAME = "tvTopologyLabel"

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
    val topologyLabel = findOptionalViewByName(root, TOPOLOGY_LABEL_ID_NAME)
    val topologyLabelPadding = topologyLabel?.capturePadding()

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
        val systemBarInsets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )

        view.updatePadding(
            left = rootPadding.left + systemBarInsets.left,
            top = if (topologyLabel == null) rootPadding.top + systemBarInsets.top else rootPadding.top,
            right = rootPadding.right + systemBarInsets.right,
            bottom = rootPadding.bottom + systemBarInsets.bottom,
        )

        if (topologyLabel != null && topologyLabelPadding != null) {
            topologyLabel.updatePadding(top = topologyLabelPadding.top + systemBarInsets.top)
        }

        windowInsets
    }
    ViewCompat.requestApplyInsets(root)
}

private fun Activity.findContentRoot(): View? {
    val content = findViewById<ViewGroup>(android.R.id.content) ?: return null
    return content.getChildAt(0)
}

private fun Activity.findOptionalViewByName(root: View, viewIdName: String): View? {
    val id = resources.getIdentifier(viewIdName, "id", packageName)
    if (id == 0) return null
    return root.findViewById(id)
}

private fun View.capturePadding(): ViewPadding = ViewPadding(
    left = paddingLeft,
    top = paddingTop,
    right = paddingRight,
    bottom = paddingBottom,
)

private data class ViewPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)
