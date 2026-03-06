package com.example.navigationlab

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.navigationlab.recipes.hosts.RecipeConditionalHostActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class RecipeConditionalHostValidationTest {

    @Test
    fun unsupportedCase_finishesActivityEarly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, RecipeConditionalHostActivity::class.java).apply {
            putExtra(RecipeConditionalHostActivity.EXTRA_CASE_ID, "R99")
            putExtra(RecipeConditionalHostActivity.EXTRA_RUN_MODE, "MANUAL")
        }

        val activity = Robolectric.buildActivity(RecipeConditionalHostActivity::class.java, intent)
            .setup()
            .get()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun supportedCase_remainsActive() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, RecipeConditionalHostActivity::class.java).apply {
            putExtra(RecipeConditionalHostActivity.EXTRA_CASE_ID, "R18")
            putExtra(RecipeConditionalHostActivity.EXTRA_RUN_MODE, "MANUAL")
        }

        val activity = Robolectric.buildActivity(RecipeConditionalHostActivity::class.java, intent)
            .setup()
            .get()

        assertFalse(activity.isFinishing)
    }
}
