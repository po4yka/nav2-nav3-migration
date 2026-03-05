package com.example.navigationlab

import android.app.Application
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.Insets
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class NavigationLabAppEdgeToEdgeTest {

    @Test
    fun applyInsets_withTopologyHeader_appliesTopInsetToHeaderOnly() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val root = LinearLayout(context).apply {
            setPadding(4, 5, 6, 7)
        }
        val header = TextView(context).apply {
            id = R.id.tvTopologyLabel
            setPadding(10, 11, 12, 13)
        }
        root.addView(header)

        applyInsets(
            root = root,
            rootPadding = ViewPadding(4, 5, 6, 7),
            topInsetTarget = header,
            topInsetTargetPadding = ViewPadding(10, 11, 12, 13),
            systemBarInsets = Insets.of(1, 20, 3, 4),
        )

        assertEquals(5, root.paddingLeft)
        assertEquals(5, root.paddingTop)
        assertEquals(9, root.paddingRight)
        assertEquals(11, root.paddingBottom)
        assertEquals(31, header.paddingTop)
    }

    @Test
    fun applyInsets_withoutTopologyHeader_appliesTopInsetToRoot() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val root = LinearLayout(context).apply {
            setPadding(8, 9, 10, 11)
        }

        applyInsets(
            root = root,
            rootPadding = ViewPadding(8, 9, 10, 11),
            topInsetTarget = null,
            topInsetTargetPadding = null,
            systemBarInsets = Insets.of(2, 30, 4, 6),
        )

        assertEquals(10, root.paddingLeft)
        assertEquals(39, root.paddingTop)
        assertEquals(14, root.paddingRight)
        assertEquals(17, root.paddingBottom)
    }
}
