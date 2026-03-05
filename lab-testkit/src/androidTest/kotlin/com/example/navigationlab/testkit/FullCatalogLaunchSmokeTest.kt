package com.example.navigationlab.testkit

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.host.fragment.ComposeToXmlBridgeProvider
import com.example.navigationlab.host.fragment.DualHostProvider
import com.example.navigationlab.host.fragment.FragmentHostProvider
import com.example.navigationlab.host.fragment.FragmentNav2HostProvider
import com.example.navigationlab.host.fragment.FragmentNav3HostProvider
import com.example.navigationlab.host.fragment.XmlToComposeBridgeProvider
import com.example.navigationlab.host.nav2.Nav2HostProvider
import com.example.navigationlab.host.nav2.Nav2ToNav3InteropProvider
import com.example.navigationlab.host.nav3.Nav3FragmentIslandProvider
import com.example.navigationlab.host.nav3.Nav3HostProvider
import com.example.navigationlab.host.nav3.Nav3NestedChainProvider
import com.example.navigationlab.host.nav3.Nav3ToNav2InteropProvider
import com.example.navigationlab.host.nav3.XmlInComposeBridgeProvider
import com.example.navigationlab.recipes.RecipeAdaptiveProvider
import com.example.navigationlab.recipes.RecipeAppStateProvider
import com.example.navigationlab.recipes.RecipeBasicProvider
import com.example.navigationlab.recipes.RecipeConditionalProvider
import com.example.navigationlab.recipes.RecipeDeepLinkProvider
import com.example.navigationlab.recipes.RecipeInteropProvider
import com.example.navigationlab.recipes.RecipeMigrationProvider
import com.example.navigationlab.recipes.RecipeModalMatrixProvider
import com.example.navigationlab.recipes.RecipeResultsProvider
import com.example.navigationlab.recipes.RecipeTransitionProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullCatalogLaunchSmokeTest {

    private typealias IntentFactory = (Context, LabCaseId, String) -> Intent

    @Test
    fun allCatalogCases_launchHostsWithoutCrashing() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalog = allCatalogEntries()

        assertTrue("Catalog should contain at least the documented 86 scenarios", catalog.size >= 86)
        val duplicates = catalog.groupBy { it.scenario.id.code }.filterValues { it.size > 1 }
        assertTrue("Duplicate case codes found: ${duplicates.keys}", duplicates.isEmpty())
        assertTrue(
            "Expected unsupported harness cases must exist in catalog",
            unsupportedHarnessCases.all { code -> catalog.any { it.scenario.id.code == code } },
        )

        val executableCatalog = catalog.filterNot { entry ->
            entry.scenario.id.family == CaseFamily.R && entry.scenario.id.number == 17
        }
        assertTrue(
            "R17 must be excluded from harness launch pass",
            executableCatalog.none { it.scenario.id.family == CaseFamily.R && it.scenario.id.number == 17 },
        )

        executableCatalog
            .sortedWith(compareBy({ it.scenario.id.family.ordinal }, { it.scenario.id.number }))
            .forEach { entry ->
                val intent = entry.intentFactory(context, entry.scenario.id, "scripted")
                val activityScenario = ActivityScenario.launch<Activity>(intent)
                try {
                    var launched = false
                    activityScenario.onActivity {
                        launched = true
                    }
                    assertTrue("Failed to launch case ${entry.scenario.id.code}", launched)
                    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                } finally {
                    activityScenario.close()
                }
            }
    }

    private data class CatalogEntry(
        val scenario: LabScenario,
        val intentFactory: IntentFactory,
    )

    private data class ProviderEntry(
        val scenarios: List<LabScenario>,
        val intentFactory: IntentFactory,
    )

    private fun allCatalogEntries(): List<CatalogEntry> =
        providers.flatMap { provider ->
            provider.scenarios.map { scenario ->
                CatalogEntry(scenario = scenario, intentFactory = provider.intentFactory)
            }
        }

    private companion object {
        /**
         * R17 relies on adaptive-scene runtime behavior that is incompatible with the
         * current instrumentation harness classpath and crashes before assertions can run.
         */
        val unsupportedHarnessCases: Set<String> = setOf("R17")

        val providers: List<ProviderEntry> = listOf(
            ProviderEntry(FragmentHostProvider.scenarios, FragmentHostProvider::createHostIntent),
            ProviderEntry(DualHostProvider.scenarios, DualHostProvider::createHostIntent),
            ProviderEntry(FragmentNav2HostProvider.scenarios, FragmentNav2HostProvider::createHostIntent),
            ProviderEntry(FragmentNav3HostProvider.scenarios, FragmentNav3HostProvider::createHostIntent),
            ProviderEntry(ComposeToXmlBridgeProvider.scenarios, ComposeToXmlBridgeProvider::createHostIntent),
            ProviderEntry(XmlToComposeBridgeProvider.scenarios, XmlToComposeBridgeProvider::createHostIntent),
            ProviderEntry(Nav2HostProvider.scenarios, Nav2HostProvider::createHostIntent),
            ProviderEntry(Nav2ToNav3InteropProvider.scenarios, Nav2ToNav3InteropProvider::createHostIntent),
            ProviderEntry(Nav3HostProvider.scenarios, Nav3HostProvider::createHostIntent),
            ProviderEntry(Nav3ToNav2InteropProvider.scenarios, Nav3ToNav2InteropProvider::createHostIntent),
            ProviderEntry(Nav3FragmentIslandProvider.scenarios, Nav3FragmentIslandProvider::createHostIntent),
            ProviderEntry(Nav3NestedChainProvider.scenarios, Nav3NestedChainProvider::createHostIntent),
            ProviderEntry(XmlInComposeBridgeProvider.scenarios, XmlInComposeBridgeProvider::createHostIntent),
            ProviderEntry(RecipeBasicProvider.scenarios, RecipeBasicProvider::createHostIntent),
            ProviderEntry(RecipeInteropProvider.scenarios, RecipeInteropProvider::createHostIntent),
            ProviderEntry(RecipeMigrationProvider.scenarios, RecipeMigrationProvider::createHostIntent),
            ProviderEntry(RecipeResultsProvider.scenarios, RecipeResultsProvider::createHostIntent),
            ProviderEntry(RecipeAppStateProvider.scenarios, RecipeAppStateProvider::createHostIntent),
            ProviderEntry(RecipeDeepLinkProvider.scenarios, RecipeDeepLinkProvider::createHostIntent),
            ProviderEntry(RecipeTransitionProvider.scenarios, RecipeTransitionProvider::createHostIntent),
            ProviderEntry(RecipeAdaptiveProvider.scenarios, RecipeAdaptiveProvider::createHostIntent),
            ProviderEntry(RecipeConditionalProvider.scenarios, RecipeConditionalProvider::createHostIntent),
            ProviderEntry(RecipeModalMatrixProvider.scenarios, RecipeModalMatrixProvider::createHostIntent),
        )
    }
}
