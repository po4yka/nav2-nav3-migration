package com.example.navigationlab.testkit

import androidx.test.runner.AndroidJUnit4
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModalScenarioCatalogCoverageTest {

    @Test
    fun modalExpansionCasesAreRegisteredExactlyOnce() {
        val allCodes = allScenarios().map { it.id.code }
        val expectedCodes = setOf(
            "B13", "B14", "B15", "B16",
            "D10", "D11", "D12", "D13", "D14", "D15",
            "E09",
            "G08",
            "R20", "R21", "R22", "R23", "R24", "R25",
        )

        val found = allCodes.filter { it in expectedCodes }

        assertEquals(expectedCodes, found.toSet())
        assertEquals(expectedCodes.size, found.size)
    }

    @Test
    fun modalExpansionCasesContainExecutableMetadata() {
        val expectedCodes = setOf(
            "B13", "B14", "B15", "B16",
            "D10", "D11", "D12", "D13", "D14", "D15",
            "E09",
            "G08",
            "R20", "R21", "R22", "R23", "R24", "R25",
        )

        val scenarios = allScenarios().filter { it.id.code in expectedCodes }
        assertEquals(expectedCodes.size, scenarios.size)

        scenarios.forEach { scenario ->
            assertTrue("${scenario.id.code} should define steps", scenario.steps.isNotEmpty())
            assertTrue(
                "${scenario.id.code} steps should define expected events",
                scenario.steps.all { it.expectedEvents.isNotEmpty() },
            )
            assertTrue("${scenario.id.code} should define invariants", scenario.invariants.isNotEmpty())
        }
    }

    private fun allScenarios(): List<LabScenario> = listOf(
        FragmentHostProvider.scenarios,
        DualHostProvider.scenarios,
        FragmentNav2HostProvider.scenarios,
        FragmentNav3HostProvider.scenarios,
        ComposeToXmlBridgeProvider.scenarios,
        XmlToComposeBridgeProvider.scenarios,
        Nav2HostProvider.scenarios,
        Nav2ToNav3InteropProvider.scenarios,
        Nav3HostProvider.scenarios,
        Nav3ToNav2InteropProvider.scenarios,
        Nav3FragmentIslandProvider.scenarios,
        Nav3NestedChainProvider.scenarios,
        XmlInComposeBridgeProvider.scenarios,
        RecipeBasicProvider.scenarios,
        RecipeInteropProvider.scenarios,
        RecipeMigrationProvider.scenarios,
        RecipeResultsProvider.scenarios,
        RecipeAppStateProvider.scenarios,
        RecipeDeepLinkProvider.scenarios,
        RecipeTransitionProvider.scenarios,
        RecipeAdaptiveProvider.scenarios,
        RecipeConditionalProvider.scenarios,
        RecipeModalMatrixProvider.scenarios,
    ).flatten()
}
