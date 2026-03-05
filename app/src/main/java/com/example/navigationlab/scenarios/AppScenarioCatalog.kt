package com.example.navigationlab.scenarios

import android.content.Context
import android.content.Intent
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

/** Aggregates app-visible scenarios and launch mapping from all host providers. */
object AppScenarioCatalog {

    private typealias IntentFactory = (Context, LabCaseId, String) -> Intent

    private data class ProviderEntry(
        val scenarios: List<LabScenario>,
        val createIntent: IntentFactory,
    )

    private val providers: List<ProviderEntry> = listOf(
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

    val scenarios: List<LabScenario> = providers
        .flatMap { it.scenarios }
        .sortedWith(compareBy({ it.id.family.ordinal }, { it.id.number }))

    val launchByCaseCode: Map<String, IntentFactory> = buildMap {
        providers.forEach { provider ->
            provider.scenarios.forEach { scenario ->
                val previous = put(scenario.id.code, provider.createIntent)
                require(previous == null) {
                    "Duplicate host launch mapping for case ${scenario.id.code}"
                }
            }
        }
    }
}
