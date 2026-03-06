package com.example.navigationlab.catalog.wiring

import com.example.navigationlab.catalog.LabScenarioCatalog
import com.example.navigationlab.contracts.LabHostProvider
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

/** All host providers across every module. Single wiring point. */
val allHostProviders: List<LabHostProvider> = listOf(
    // Fragment topologies (T1, T4, T6)
    FragmentHostProvider,
    DualHostProvider,
    FragmentNav2HostProvider,
    FragmentNav3HostProvider,
    ComposeToXmlBridgeProvider,
    XmlToComposeBridgeProvider,
    // Nav2 topologies (T2, T7)
    Nav2HostProvider,
    Nav2ToNav3InteropProvider,
    // Nav3 topologies (T3, T5, T8, interop)
    Nav3HostProvider,
    Nav3ToNav2InteropProvider,
    Nav3FragmentIslandProvider,
    Nav3NestedChainProvider,
    XmlInComposeBridgeProvider,
    // Recipe scenarios (R01-R25)
    RecipeBasicProvider,
    RecipeInteropProvider,
    RecipeMigrationProvider,
    RecipeResultsProvider,
    RecipeAppStateProvider,
    RecipeDeepLinkProvider,
    RecipeTransitionProvider,
    RecipeAdaptiveProvider,
    RecipeConditionalProvider,
    RecipeModalMatrixProvider,
)

/** Pre-wired catalog with all host providers registered. */
fun createWiredCatalog(): LabScenarioCatalog = LabScenarioCatalog(allHostProviders)
