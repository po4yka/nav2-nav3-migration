package com.example.navigationlab.engine.casebrowser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.RunMode
import com.example.navigationlab.contracts.TopologyId

/**
 * Main case browser screen that lists all case families and registered scenarios.
 * Entry point of the navigation lab.
 */
@Composable
fun CaseBrowserScreen(
    scenarios: List<LabScenario>,
    onCaseSelected: (LabCaseId, RunMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedRunMode by remember { mutableStateOf(RunMode.MANUAL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFamilyFilter by remember { mutableStateOf<CaseFamily?>(null) }
    var selectedTopologyFilter by remember { mutableStateOf<TopologyId?>(null) }
    var lastSelectedCase by remember { mutableStateOf<LabCaseId?>(null) }

    val filteredScenarios = remember(scenarios, searchQuery, selectedFamilyFilter, selectedTopologyFilter) {
        val normalizedQuery = searchQuery.trim().lowercase()
        scenarios.filter { scenario ->
            val matchesFamily = selectedFamilyFilter == null || scenario.id.family == selectedFamilyFilter
            val matchesTopology = selectedTopologyFilter == null || scenario.topology == selectedTopologyFilter
            val matchesQuery =
                normalizedQuery.isBlank() ||
                    scenario.id.code.lowercase().contains(normalizedQuery) ||
                    scenario.title.lowercase().contains(normalizedQuery) ||
                    scenario.topology.name.lowercase().contains(normalizedQuery)

            matchesFamily && matchesTopology && matchesQuery
        }
    }

    val grouped = remember(filteredScenarios) {
        CaseFamily.entries.associateWith { family ->
            filteredScenarios
                .filter { it.id.family == family }
                .sortedBy { it.id.number }
        }
    }

    fun launchCase(caseId: LabCaseId) {
        lastSelectedCase = caseId
        onCaseSelected(caseId, selectedRunMode)
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Navigation Interop Lab",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search by code, title, topology") },
        )

        Spacer(modifier = Modifier.height(8.dp))

        RunModeSelector(
            selected = selectedRunMode,
            onSelected = { selectedRunMode = it },
        )

        Spacer(modifier = Modifier.height(8.dp))

        FamilyFilterSelector(
            selectedFamily = selectedFamilyFilter,
            onSelected = { selectedFamilyFilter = it },
        )

        Spacer(modifier = Modifier.height(8.dp))

        TopologyFilterSelector(
            selectedTopology = selectedTopologyFilter,
            onSelected = { selectedTopologyFilter = it },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Showing ${filteredScenarios.size} of ${scenarios.size} scenarios",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                enabled = lastSelectedCase != null,
                onClick = {
                    lastSelectedCase?.let { caseId ->
                        onCaseSelected(caseId, selectedRunMode)
                    }
                },
            ) {
                Text(lastSelectedCase?.let { "Rerun ${it.code}" } ?: "Rerun last")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (filteredScenarios.isEmpty()) {
                item(key = "empty_filter_result") {
                    Text(
                        text = "No scenarios match the active search/filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                CaseFamily.entries.forEach { family ->
                    val cases = grouped[family].orEmpty()
                    if (cases.isNotEmpty()) {
                        item(key = "header_${family.prefix}") {
                            FamilyHeader(family)
                        }
                        items(cases, key = { it.id.code }) { scenario ->
                            CaseRow(
                                scenario = scenario,
                                onClick = { launchCase(scenario.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RunModeSelector(
    selected: RunMode,
    onSelected: (RunMode) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RunMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
        }
    }
}

@Composable
private fun FamilyFilterSelector(
    selectedFamily: CaseFamily?,
    onSelected: (CaseFamily?) -> Unit,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedFamily == null,
            onClick = { onSelected(null) },
            label = { Text("All families") },
        )
        CaseFamily.entries.forEach { family ->
            FilterChip(
                selected = family == selectedFamily,
                onClick = { onSelected(family) },
                label = { Text(family.prefix) },
            )
        }
    }
}

@Composable
private fun TopologyFilterSelector(
    selectedTopology: TopologyId?,
    onSelected: (TopologyId?) -> Unit,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedTopology == null,
            onClick = { onSelected(null) },
            label = { Text("All topologies") },
        )
        TopologyId.entries.forEach { topology ->
            FilterChip(
                selected = topology == selectedTopology,
                onClick = { onSelected(topology) },
                label = { Text(topology.name) },
            )
        }
    }
}

@Composable
private fun FamilyHeader(family: CaseFamily) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        HorizontalDivider()
        Text(
            text = "${family.prefix}: ${family.title}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun CaseRow(
    scenario: LabScenario,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${scenario.id.code} - ${scenario.title}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Topology: ${scenario.topology.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
