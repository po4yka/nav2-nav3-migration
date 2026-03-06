package com.example.navigationlab

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.recipes.RecipeConditionalProvider
import com.example.navigationlab.recipes.RecipeDeepLinkProvider
import com.example.navigationlab.recipes.hosts.RecipeAdvancedDeepLinksActivity
import com.example.navigationlab.recipes.hosts.RecipeConditionalHostActivity
import com.example.navigationlab.recipes.hosts.RecipeDeepLinkHostActivity
import com.example.navigationlab.recipes.hosts.RecipeDeepLinksActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class RecipeDeepLinkInAppInvocationTest {

    @Test
    fun r13Provider_usesTrampolineIntentWithExpectedPayload() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = RecipeDeepLinkProvider.createHostIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, 13),
            runMode = "SCRIPTED",
        )

        assertEquals(RecipeDeepLinksActivity::class.java.name, intent.component?.className)
        assertEquals("recipes", intent.data?.scheme)
        assertEquals("target", intent.data?.host)
        assertEquals("hello", intent.data?.getQueryParameter("param"))
        assertEquals("SCRIPTED", intent.getStringExtra(RecipeDeepLinkHostActivity.EXTRA_RUN_MODE))
    }

    @Test
    fun r19Provider_usesAdvancedTrampolineIntentWithExpectedPayload() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = RecipeConditionalProvider.createHostIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, 19),
            runMode = "STRESS",
        )

        assertEquals(RecipeAdvancedDeepLinksActivity::class.java.name, intent.component?.className)
        assertEquals("recipes", intent.data?.scheme)
        assertEquals("advanced", intent.data?.host)
        assertEquals("Alice", intent.data?.getQueryParameter("name"))
        assertEquals("NYC", intent.data?.getQueryParameter("location"))
        assertEquals("STRESS", intent.getStringExtra(RecipeConditionalHostActivity.EXTRA_RUN_MODE))
    }

    @Test
    fun r18Provider_stillUsesDirectConditionalHostIntent() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = RecipeConditionalProvider.createHostIntent(
            context = context,
            caseId = LabCaseId(CaseFamily.R, 18),
            runMode = "MANUAL",
        )

        assertEquals(RecipeConditionalHostActivity::class.java.name, intent.component?.className)
        assertEquals("R18", intent.getStringExtra(RecipeConditionalHostActivity.EXTRA_CASE_ID))
        assertEquals("MANUAL", intent.getStringExtra(RecipeConditionalHostActivity.EXTRA_RUN_MODE))
    }
}
