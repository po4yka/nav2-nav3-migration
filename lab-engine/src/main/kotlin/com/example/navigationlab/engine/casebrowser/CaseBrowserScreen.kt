package com.example.navigationlab.engine.casebrowser

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.navigationlab.contracts.CaseFamily
import com.example.navigationlab.contracts.LabCaseId
import com.example.navigationlab.contracts.LabScenario
import com.example.navigationlab.contracts.RunMode

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
    val grouped = remember(scenarios) {
        CaseFamily.entries.associateWith { family ->
            scenarios.filter { it.id.family == family }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Navigation Interop Lab",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        RunModeSelector(
            selected = selectedRunMode,
            onSelected = { selectedRunMode = it },
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            CaseFamily.entries.forEach { family ->
                item(key = "header_${family.prefix}") {
                    FamilyHeader(family)
                }

                val cases = grouped[family].orEmpty()
                if (cases.isEmpty()) {
                    item(key = "empty_${family.prefix}") {
                        Text(
                            text = "No scenarios registered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                        )
                    }
                } else {
                    items(cases, key = { it.id.code }) { scenario ->
                        CaseRow(
                            scenario = scenario,
                            onClick = { onCaseSelected(scenario.id, selectedRunMode) },
                        )
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
