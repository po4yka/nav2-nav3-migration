package com.example.navigationlab.testkit

import androidx.test.runner.AndroidJUnit4
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.effectiveInvariantSpecs
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GhScenarioCatalogCoverageTest {

    @Test
    fun allGCasesAreRegisteredExactlyOnce() {
        val gCaseCodes = allScenarios()
            .filter { it.id.family == CaseFamily.G }
            .map { it.id.code }

        val expected = (1..8).map { "G%02d".format(it) }.toSet()

        assertEquals(expected, gCaseCodes.toSet())
        assertEquals(expected.size, gCaseCodes.size)
    }

    @Test
    fun allHCasesAreRegisteredExactlyOnce() {
        val hCaseCodes = allScenarios()
            .filter { it.id.family == CaseFamily.H }
            .map { it.id.code }

        val expected = (1..5).map { "H%02d".format(it) }.toSet()

        assertEquals(expected, hCaseCodes.toSet())
        assertEquals(expected.size, hCaseCodes.size)
    }

    @Test
    fun ghScenariosContainExecutableMetadata() {
        val ghScenarios = allScenarios().filter {
            it.id.family == CaseFamily.G || it.id.family == CaseFamily.H
        }

        assertEquals(13, ghScenarios.size)
        ghScenarios.forEach { scenario ->
            assertTrue("${scenario.id.code} should define at least one step", scenario.steps.isNotEmpty())
            assertTrue(
                "${scenario.id.code} steps should have expected events",
                scenario.steps.all { it.expectedEvents.isNotEmpty() },
            )
            assertTrue(
                "${scenario.id.code} steps should define typed actions",
                scenario.steps.all { it.action.observedEvents.isNotEmpty() },
            )
            assertTrue(
                "${scenario.id.code} should define typed invariants",
                scenario.effectiveInvariantSpecs.isNotEmpty(),
            )
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
    ).flatten()
}
